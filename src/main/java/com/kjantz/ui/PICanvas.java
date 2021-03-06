package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class PICanvas extends Canvas {
  private final int outputX;
  private final int outputY;

  private double pixelWidthX, pixelWidthY;
  private GraphicsContext gc;
  private SimpleObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(Color.WHITE);

  public PICanvas(double width, double height, int outputX, int outputY) {
    super(width, height);
    this.outputX = outputX;
    this.outputY = outputY;
    this.pixelWidthX = width / outputX;
    this.pixelWidthY = height / outputY;
    this.gc = getGraphicsContext2D();
    EventHandler<MouseEvent> mouseEventEventHandler = t -> {

      int x = (int) (t.getX() / pixelWidthX);
      int y = (int) (t.getY() / pixelWidthY);
      setRGB(x, y, t.getButton() == MouseButton.PRIMARY ? colorProperty.getValue() : Color.BLACK);
    };
    addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventEventHandler);
    addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventEventHandler);
  }

  public void clear(Color background) {
    gc.setFill(background);
    gc.fillRect(0, 0, getWidth(), getHeight());
  }

  public void setColor(Color color) {
    gc.setFill(color);
  }

  public void setImageProcessor(ImageProcessor processor) {
    for (int x = 0; x < processor.getOutputImage().getWidth(); x++) {
      for (int y = 0; y < processor.getOutputImage().getHeight(); y++) {
        int px = processor.getOutputImage().getRGB(x,y);
        setRGB(x, y, px);
      }
    }
  }

  /**
   * sets a pixel at given PI coordinates, the pixel will be of the same color as the last successful set color
   * @param x coordinate
   * @param y coordinate
   */
  public void setPixel(int x, int y) {
    gc.fillRect(x*pixelWidthX, y*pixelWidthY, pixelWidthX, pixelWidthY);
  }

  /**
   * sets a pixel in the given color at the given coordinates
   * @param x coordinate
   * @param y coordinate
   * @param color color of the pixel in 0xRRGGBB
   */
  public void setRGB(int x, int y, int color) {
    gc.setFill(Color.rgb(((color >> 16) & 0xFF), ((color >> 8) & 0xFF), (color & 0xFF)));
    setPixel(x, y);
  }

  /**
   * sets a pixel in the given color at the given coordinates
   * @param x coordinate
   * @param y coordinate
   * @param color color of the pixel
   */
  public void setRGB(int x, int y, Color color) {
    gc.setFill(color);
    setPixel(x, y);
  }

  public SimpleObjectProperty<Color> getColorProperty() {
    return colorProperty;
  }

}
