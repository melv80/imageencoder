package com.kjantz.imageencoder;

import com.sun.istack.internal.NotNull;

import java.awt.image.BufferedImage;
import java.io.*;

class PIWriter {
  public static void saveImage(@NotNull BufferedImage img, @NotNull OutputStream outStream, boolean alpha) throws IOException {
    PrintWriter w = new PrintWriter(new OutputStreamWriter(outStream));
    for (int x = 0; x < img.getWidth(); x++) {
      for (int y = 0; y < img.getHeight(); y++) {
        if (alpha) {
          w.println(String.format("%d %d %s", x, y, Integer.toHexString(img.getRGB(x,y))));
        }
        else {
          w.println(String.format("%d %d %s", x, y, Integer.toHexString(img.getRGB(x,y) & 0xFFFFFF)));
        }
      }
    }
    w.flush();
    w.close();
  }
}
