package com.kjantz.imageencoder;

import com.kjantz.util.BlendMode;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Simple helper class, that converts the input image to pixelflut format.
 * The file format is a text based line format beeing
 * x y 0xAARRGGBB
 */
class PIWriter {
  /**
   *
   * @param img input image that shall be converted
   * @param outStream the resulting output stream
   * @param alpha if true the pixel value output will be of format 0xAARRGGBB, otherwise 0xRRGGBB
   */
  static void saveImage(@NotNull BufferedImage img, @NotNull OutputStream outStream, boolean alpha, BlendMode blendMode) {
    PrintWriter w = new PrintWriter(new OutputStreamWriter(outStream));
    for (int x = 0; x < img.getWidth(); x++) {
      for (int y = 0; y < img.getHeight(); y++) {
        if (blendMode == BlendMode.IGNORE && (img.getRGB(x, y) == 0 || img.getRGB(x, y) == 0x00000000)) continue;
        if (alpha) {
          w.print(String.format("%d %d %s\n", x, y, Integer.toHexString(img.getRGB(x,y))));
        }
        else {
          w.print(String.format("%d %d %s\n", x, y, Integer.toHexString(img.getRGB(x,y) & 0xFFFFFF)));
        }
      }
    }
    w.flush();
    w.close();
  }

}
