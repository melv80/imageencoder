package com.kjantz.imageencoder;

import com.kjantz.util.BlendMode;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Available Outputformats of the image processor.
 */
public enum OutputFormat {
  /**
   * Simple PNG file format.
   */
  PNG("png") {
    public void saveImage(@NotNull BufferedImage img, @NotNull OutputStream out, BlendMode blendMode) throws IOException {
      ImageIO.write(img, "png", out);
    }
  },

  /**
   * Pixelflut file format.
   */
  PI("pi") {
    public void saveImage(@NotNull BufferedImage img, @NotNull OutputStream out, BlendMode blendMode) throws IOException {
      PIWriter.saveImage(img, out, false, blendMode);
    }
  };

  private final String formatExtension;

  OutputFormat(String formatExtension) {
    this.formatExtension = formatExtension;
  }

  public abstract void saveImage(@NotNull BufferedImage img, @NotNull OutputStream out, BlendMode blendMode) throws IOException;

  public String getFormatExtension() {
    return formatExtension;
  }
}
