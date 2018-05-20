package com.tanuj;

import com.github.sarxos.webcam.*;
import redis.clients.jedis.Jedis;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Base64;
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
    Dimension[] nonStandardResolutions = new Dimension[] {
        WebcamResolution.HD.getSize(),
    };
    w.setCustomViewSizes(nonStandardResolutions);
    w.setViewSize(WebcamResolution.HD.getSize());
    length = w.getViewSize();
    if(RELAY){
      display = new WebcamPanel(w, false);
      ImageDisplay disp = new ImageDisplay(length.width, length.height, w);
    }
    System.out.println("STARTING");
    Main m = new Main();
    while(true){

    }
  }

  public void frameChange(BufferedImage image) {

    //LOG.info("New image from {}", webcam);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "JPG", baos);
    } catch (IOException e) {
      //LOG.error(e.getMessage(), e);
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

    publish(base64);
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
  }

  private static void updateFrame(BufferedImage bytes){
    if(!RELAY){
      return;
    }
    //display.setImage(bytes);
  }
}
