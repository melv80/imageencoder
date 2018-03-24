package com.kjantz.imageencoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public enum Format {
  PNG("png") {
    public void saveImage(BufferedImage img, OutputStream out) throws IOException {
      ImageIO.write(img, "png", out);
    }
  },
  PI("pi") {
    public void saveImage(BufferedImage img, OutputStream out) throws IOException {
      PIWriter.saveImage(img, out, false);
    }
  };

  private final String formatExtension;

  Format(String formatExtension) {
    this.formatExtension = formatExtension;
  }

  public abstract void saveImage(BufferedImage img, OutputStream out) throws IOException;

  public String getFormatExtension() {
    return formatExtension;
  }
}
