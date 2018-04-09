package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PICanvas extends Canvas {
  private final int outputX;
  private final int outputY;

  private double pixelWidthX, pixelWidthY;
  private GraphicsContext gc;

  public PICanvas(double width, double height, int outputX, int outputY) {
    super(width, height);
    this.outputX = outputX;
    this.outputY = outputY;
    this.pixelWidthX = width / outputX;
    this.pixelWidthY = height / outputY;
    this.gc = getGraphicsContext2D();
  }

  public void setColor(Color color) {
    gc.setFill(color);
  }

  public void setImageProcessor(ImageProcessor processor) {
    for (int x = 0; x < processor.getOutputImage().getWidth(); x++) {
      for (int y = 0; y < processor.getOutputImage().getHeight(); y++) {
        int px = processor.getOutputImage().getRGB(x,y);
        Color color = Color.rgb(((px >> 16) & 0xFF), ((px >> 8) & 0xFF), (px & 0xFF));
        gc.setFill(color);
        gc.fillRect(x*pixelWidthX, y*pixelWidthY, pixelWidthX, pixelWidthY);

      }

    }
  }
}
