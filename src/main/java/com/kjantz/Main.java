package com.kjantz;

import com.kjantz.imageencoder.Format;
import com.kjantz.imageencoder.ImageOutputter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class Main {
  public static void main(String[] args) throws IOException {
    ImageOutputter p = new ImageOutputter(64, 64);
    p.loadImage(new File("J:\\Entwicklung\\imageencoder\\src\\main\\resources\\test.jpg"));
    p.saveImage(null, Format.PNG);
    p.saveImage(null, Format.PI);
    p.sentToSocket("localhost", 8181, Format.PI);
  }
}
