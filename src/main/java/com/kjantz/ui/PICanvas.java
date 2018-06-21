package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.util.Async;
import com.kjantz.util.Constants;
import com.kjantz.util.Util;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class PICanvas extends Canvas {
    private final int outputX;
    private final int outputY;

    private double pixelWidthX, pixelWidthY;
    private GraphicsContext gc;
    private SimpleObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(Color.WHITE);
    private volatile Optional<RGBPixelCallback> pixelCB = Optional.empty();
    private final int[][] lastFrame;

    public PICanvas(double width, double height, int outputX, int outputY) {
        super(width, height);
        this.outputX = outputX;
        this.outputY = outputY;
        this.pixelWidthX = width / outputX;
        this.pixelWidthY = height / outputY;
        this.gc = getGraphicsContext2D();
        lastFrame = new int[outputX][outputY];
        EventHandler<MouseEvent> mouseEventEventHandler = t -> {

            int x = (int) (t.getX() / pixelWidthX);
            int y = (int) (t.getY() / pixelWidthY);
            setRGB(x, y, t.getButton() == MouseButton.PRIMARY ? colorProperty.getValue() : Color.BLACK, true);
        };
        addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventEventHandler);
        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventEventHandler);
    }

    public void setPixelCB(RGBPixelCallback pixelCB) {
        this.pixelCB = Optional.of(pixelCB);
    }

    public void clear(Color background) {
        for (int x = 0; x < Constants.DEFAULT_X_OUTPUT; x++) {
            for (int y = 0; y < Constants.DEFAULT_Y_OUTPUT; y++) {
                setRGB(x, y, background, true);
            }
        }
    }

    public void setColor(Color color) {
        gc.setFill(color);
    }

    public int[][] getLastFrame() {
        return lastFrame;
    }

    public void setImageProcessor(ImageProcessor processor) {
        Platform.runLater(() -> {
            for (int x = 0; x < processor.getOutputImage().getWidth(); x++) {
                for (int y = 0; y < processor.getOutputImage().getHeight(); y++) {
                    int px = processor.getOutputImage().getRGB(x, y);
                    setRGB(x, y, px);
                    int finalX = x;
                    int finalY = y;
                    Async.execute(() -> {
                        pixelCB.ifPresent(pixelCB -> {
                            pixelCB.setRGB(finalX, finalY, (px) & 0x00FFFFFF, false);
                        });
                    });

                }
            }
        });

    }


    public void setImageRGB(int[][] rgb) {
        Platform.runLater(() -> {
            for (int x = 0; x < rgb.length; x++) {
                for (int y = 0; y < rgb[0].length; y++) {
                    int px = rgb[x][y];
                    setRGB(x, y, px);
                    int finalX = x;
                    int finalY = y;
                    if (lastFrame == null || rgb[x][y] != lastFrame[x][y]) {
                        Async.execute(() -> {
                            pixelCB.ifPresent(pixelCB -> {
                                pixelCB.setRGB(finalX, finalY, (px) & 0x00FFFFFF, false);
                            });
                        });
                    }
                }
            }
            System.arraycopy(rgb, 0, lastFrame, 0, rgb.length);
        });

    }

    /**
     * sets a pixel at given PI coordinates, the pixel will be of the same color as the last successful set color
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setPixel(int x, int y) {
        gc.fillRect(x * pixelWidthX, y * pixelWidthY, pixelWidthX, pixelWidthY);
    }

    /**
     * sets a pixel in the given color at the given coordinates
     *
     * @param x     coordinate
     * @param y     coordinate
     * @param color color of the pixel in 0xRRGGBB
     */
    public void setRGB(int x, int y, int color) {
        gc.setFill(Color.rgb(((color >> 16) & 0xFF), ((color >> 8) & 0xFF), (color & 0xFF)));
        setPixel(x, y);
    }

    public int getRGB(int x, int y) {
        if (x < 0 || x > lastFrame.length || y < 0 || y > lastFrame[x].length) return 0;
        return lastFrame[x][y];
    }

    public void setRGB(int x, int y, Color color) {
        setRGB(x, y, color, false);
    }


    public void drawLine(int x0, int y0, int x1, int y1, Color color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            if ((x0 >= 0 && x0 < Constants.DEFAULT_X_OUTPUT) && (y0 >= 0 && y0 < Constants.DEFAULT_Y_OUTPUT))
                setRGB(x0, y0, color);

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
    }

    /**
     * sets a pixel in the given color at the given coordinates
     *
     * @param x     coordinate
     * @param y     coordinate
     * @param color color of the pixel
     */
    public void setRGB(int x, int y, Color color, boolean force) {
        gc.setFill(color);
        setPixel(x, y);

        pixelCB.ifPresent(pixelCB -> {
            pixelCB.setRGB(x, y, Util.toIntColor(color), force);
        });
    }

    public SimpleObjectProperty<Color> getColorProperty() {
        return colorProperty;
    }


    public interface RGBPixelCallback {
        void setRGB(int x, int y, int color, boolean force);
    }
}
