package com.kjantz;

import com.kjantz.imageencoder.OutputFormat;
import com.kjantz.imageencoder.ImageProcessor;

import java.io.File;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    ImageProcessor p = new ImageProcessor(64, 64);
    p.loadImage(new File("c:\\users\\kristianj\\ideaprojects\\imageencoder\\src\\main\\resources\\test.jpg"));
    p.saveImage(null, OutputFormat.PNG);
    p.saveImage(null, OutputFormat.PI);
    p.sentToSocket("localhost", 8181, OutputFormat.PI);
    Thread.sleep(1000);
  }
}
