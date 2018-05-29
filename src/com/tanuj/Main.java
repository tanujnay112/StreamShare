package com.tanuj;

import com.github.sarxos.webcam.*;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends JApplet implements WebcamMotionListener {

    static String location = "";
    final static String channelName = "slides";
    static Dimension length;
    static Webcam w;

    static WebcamPanel display;
    final static boolean RELAY = false;

    static BlockingQueue queue;
    static ExecutorService threadPool;

    static int timestamp = 0;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void publish(String s) {
        Jedis jedis = new Jedis(location);
        //String s = Base64.getEncoder().encodeToString(b);
        //System.out.println(s);
        //s = String.format("%d;%d;%s", d.height, d.width, s);
        //System.exit(0);
        System.out.println("publishing");
        jedis.publish(channelName, s);
    }

    public Main() {
        WebcamMotionDetector detector = new WebcamMotionDetector(w);
        detector.setInterval(100);
        detector.addMotionListener(this);
        detector.setAreaThreshold(0.3);
        detector.start();
        length = w.getViewSize();
    }

    public static void main(String[] args) {

        threadPool = Executors.newFixedThreadPool(2);

        location = args[0];
        w = Webcam.getDefault();
        Dimension[] nonStandardResolutions =
            new Dimension[] { WebcamResolution.HD.getSize(), };
        w.setCustomViewSizes(nonStandardResolutions);
        w.setViewSize(WebcamResolution.HD.getSize());
        length = w.getViewSize();
        if (RELAY) {
          display = new WebcamPanel(w, false);
          ImageDisplay disp = new ImageDisplay(length.width, length.height, w);
        }
        Main m = new Main();
        while (true) {

        }
    }

    public void frameChange(int time, BufferedImage image) {

        //LOG.info("New image from {}", webcam);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
          ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
          //might be an issue
          Iterator<ImageWriter> writers =
              ImageIO.getImageWritersByFormatName("jpg");
          if (!writers.hasNext()) {
            System.out.println("NO WRITERS");
          }
          ImageWriter writer =
              (ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();
          writer.setOutput(ios);
          ImageWriteParam param = writer.getDefaultWriteParam();
          param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
          param.setCompressionQuality(0.7f);
          writer.write(null, new IIOImage(image, null, null), param);
          ios.close();
          baos.close();
          writer.dispose();
        } catch (IOException e) {
          //LOG.error(e.getMessage(), e);
          e.printStackTrace();
        }
        String base64 = null;
        try {
          base64 =
              new String(Base64.getEncoder().encode(baos.toByteArray()), "UTF8");
        } catch (UnsupportedEncodingException e) {
          //LOG.error(e.getMessage(), e);
        }

        //Map<String, Object> message = new HashMap<String, Object>();
        //message.put("type", "image");
        //message.put("webcam", webcam.getName());
        //message.put("image", base64);
        System.out.println("Before submit");
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("time", time);
        m.put("image", base64);
        String message = null;
        try {
          message = MAPPER.writeValueAsString(m);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
        final String finalMessage = message;
        threadPool.submit(new Runnable() {
          public void run() {
            publish(finalMessage);
          }
        });
        //publish(finalBase6);
    }

    static synchronized int increaseTime() {
    return ++timestamp;
  }

    public void motionDetected(WebcamMotionEvent webcamMotionEvent) {
        System.out.println("Motion detected");
        threadPool.submit(new Runnable() {
          public void run() {
            frameChange(increaseTime(), w.getImage());
          }
        });
        //frameChange(w.getImage());
    }

    private static void updateFrame(BufferedImage bytes) {
        if (!RELAY) {
          return;
        }
        //display.setImage(bytes);
    }
}
