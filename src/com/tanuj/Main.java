package com.tanuj;

import com.github.sarxos.webcam.*;
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
import java.util.Base64;
import java.util.Iterator;
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

  public static void publish(String s){
    Jedis jedis = new Jedis(location);
    //String s = Base64.getEncoder().encodeToString(b);
    //System.out.println(s);
    //s = String.format("%d;%d;%s", d.height, d.width, s);
    //System.exit(0);
    System.out.println("publishing");
    jedis.publish(channelName, s);
  }

  public Main(){
    WebcamMotionDetector detector = new WebcamMotionDetector(w);
    detector.setInterval(100);
    detector.addMotionListener(this);
    detector.setAreaThreshold(0.3);
    detector.start();
    length = w.getViewSize();
  }


  public static void main(String[] args) {

    queue = new ArrayBlockingQueue(2);

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
    while(true){

    }
  }

    public void frameChange(BufferedImage image) {

        //LOG.info("New image from {}", webcam);

    System.out.println("hello");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
      //might be an issue
      Iterator<ImageWriter> writers =
          ImageIO.getImageWritersByFormatName("jpg");
      if(!writers.hasNext()){
        System.out.println("NO WRITERS");
      }
      ImageWriter writer = (ImageWriter)
          ImageIO.getImageWritersByFormatName("jpg").next();
      writer.setOutput(ios);
      ImageWriteParam param = writer.getDefaultWriteParam();
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      param.setCompressionQuality(0.7f);
      writer.write(null,
          new IIOImage(image, null, null), param);
      ios.close();
      baos.close();
      writer.dispose();
    } catch (IOException e) {
      //LOG.error(e.getMessage(), e);
      e.printStackTrace();
    }
        String base64 = null;
        try {
          base64 = new String(Base64.getEncoder().encode(baos.toByteArray()), "UTF8");
        } catch (UnsupportedEncodingException e) {
          //LOG.error(e.getMessage(), e);
        }

    //Map<String, Object> message = new HashMap<String, Object>();
    //message.put("type", "image");
    //message.put("webcam", webcam.getName());
    //message.put("image", base64);
    System.out.println("Before submit");
    final String finalBase6 = base64;
    threadPool.submit(new Runnable() {
      public void run() {
        publish(finalBase6);
      }
    });
    //publish(finalBase6);
  }

    /*public static void frameChange() {
    System.out.println("CHANGED");
    ByteBuffer bb = w.getImageBytes();
    System.out.printf("height: %d, width: %d\n", length.height, length.width);
    byte[] bytes = new byte[length.height*length.width*3];
    updateFrame(w.getImage());
    synchronized(bb){
      bb.rewind();
      bb.get(bytes);
      bb.rewind();
      publish(bytes, length);
    }
    }*/

  public void motionDetected(WebcamMotionEvent webcamMotionEvent) {
    System.out.println("WHAT THE");
    threadPool.submit(new Runnable() {
      public void run() {
        frameChange(w.getImage());
      }
    });
    //frameChange(w.getImage());
  }
    private static void updateFrame(BufferedImage bytes){
        if(!RELAY){
            return;
        }
        //display.setImage(bytes);
    }
}
