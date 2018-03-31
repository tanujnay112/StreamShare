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
  public ImageDisplay(){
    setLayout(new FlowLayout());
    this.setSize(640,480);
    pic = new JLabel();
    add(pic);
    this.setVisible(true);
  }

  public void setImage(BufferedImage img){
    pic.setIcon(new ImageIcon(img));
    pic.updateUI();
  }


}
