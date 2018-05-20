package com.tanuj;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

/**
 * Created by Tanuj on 3/31/18.
 */
public class ImageDisplay extends JFrame{
  JLabel pic;
  public ImageDisplay(int width, int height){
    setLayout(new FlowLayout());
    this.setSize(width,height);
    pic = new JLabel();
    add(pic);
    this.setVisible(true);
  }

  public ImageDisplay(int width, int height, Webcam w){
    setLayout(new FlowLayout());
    this.setSize(width, height);
    WebcamPanel view = new WebcamPanel(w, false);
    add(view);
    view.start();
    this.setVisible(true);
  }

  public void setImage(BufferedImage img){
    pic.setIcon(new ImageIcon(img));
    pic.updateUI();
  }


}
