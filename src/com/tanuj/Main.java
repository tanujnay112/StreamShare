package com.tanuj;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;

import java.awt.image.BufferedImage;

public class Main {

    public static void main(String[] args) {
	// write your code here
      WebcamMotionDetector detector = new WebcamMotionDetector(
          Webcam.getDefault(), 20, 100);
      detector.setInterval(10);
      detector.start();
      while(true){
        if(detector.isMotion()){
          frameChange();
        }
      }
    }

  public static void frameChange() {

  }
}
