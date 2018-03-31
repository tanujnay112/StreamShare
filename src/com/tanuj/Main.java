package com.tanuj;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Base64;

public class Main implements WebcamMotionListener {

  static String location = "";
  final static String channelName = "slides";
  static Dimension length;
  static Webcam w;

  public static void publish(byte[] b){
    Jedis jedis = new Jedis(location);

    jedis.publish(channelName, Base64.getEncoder().encodeToString(b));
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
    System.out.println("STARTING");
    Main m = new Main();
    while(true){

    }
  }

  public static void frameChange() {
    System.out.println("CHANGED");
    ByteBuffer bb = w.getImageBytes();
    byte[] bytes = new byte[length.height*length.width];
    synchronized(bb){
      bb.rewind();
      bb.get(bytes);
      bb.rewind();
      publish(bytes);
    }
  }

  public void motionDetected(WebcamMotionEvent webcamMotionEvent) {
    System.out.println("WHAT THE");
    frameChange();
  }
}
