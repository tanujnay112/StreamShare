package com.tanuj;

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

  public void setImage(BufferedImage img){
    pic.setIcon(new ImageIcon(img));
    pic.updateUI();
  }


}
