package com.tanuj;

import com.github.sarxos.webcam.*;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Base64;

public class Main implements WebcamMotionListener {

  static String location = "";
  final static String channelName = "slides";
  static Dimension length;
  static Webcam w;

  static ImageDisplay display;
  final static boolean RELAY = true;

  public static void publish(byte[] b, Dimension d){
    Jedis jedis = new Jedis(location);
    String s = Base64.getEncoder().encodeToString(b);
    s = String.format("%d;%d;%s", d.height, d.width, s);
    jedis.publish(channelName, s);
  }

  public Main(){
    WebcamMotionDetector detector = new WebcamMotionDetector(w);
    detector.setInterval(100);
    detector.addMotionListener(this);
    detector.start();
    length = w.getViewSize();
  }

  public static void main(String[] args) {
    location = args[0];
    w = Webcam.getDefault();
    Dimension[] nonStandardResolutions = new Dimension[] {
        WebcamResolution.HD.getSize(),
    };
    w.setCustomViewSizes(nonStandardResolutions);
    w.setViewSize(WebcamResolution.HD.getSize());
    length = w.getViewSize();
    if(RELAY){
      display = new ImageDisplay(length.width, length.height);
    }
    System.out.println("STARTING");
    Main m = new Main();
    while(true){

    }
  }

  public static void frameChange() {
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
  }

  public void motionDetected(WebcamMotionEvent webcamMotionEvent) {
    System.out.println("WHAT THE");
    frameChange();
  }

  private static void updateFrame(BufferedImage bytes){
    if(!RELAY){
      return;
    }
    display.setImage(bytes);
  }
}
