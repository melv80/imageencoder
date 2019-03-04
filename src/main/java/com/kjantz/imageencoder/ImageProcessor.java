package com.kjantz.imageencoder;

import com.kjantz.util.Constants;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

/**
 * Is used to process images and allows to convert them to different fileformats and sent them directly to a network socket.
 * This class is intented to be used for converting images to a file format that might be understandable by simple RGB LED pixel device.
 */
public class ImageProcessor {
    private final int outputWidth;
    private final int outputHeight;

    private BufferedImage scaled;
    private File pathToImage;

    /**
     * Constructs an Imageprocessor with the given outputwidth and size
     *
     * @param outputWidth  target width of the supported LED pixel device
     * @param outputHeight target height of the supported LED pixel device
     */
    public ImageProcessor(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.scaled = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * the resulting output image as {@link BufferedImage} it may be used for further processing
     *
     * @return the outputimage
     */
    public BufferedImage getOutputImage() {
        return scaled;
    }

    /**
     * loads an image into the imageprocessor or throws an {@link IOException} if the image can not be loaded.
     *
     * @param pathToImage path to the image
     * @return this, for function chaining
     * @throws IOException if the image can not be loaded.
     */
    public ImageProcessor loadImage(@NotNull File pathToImage) throws IOException {
        BufferedImage im = ImageIO.read(pathToImage);
        this.pathToImage = pathToImage;
//    Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image loaded %dx%d pixels", im.getWidth(), im.getHeight()));

        scaleImageToOutput(im);
        return this;
    }

    public ImageProcessor loadImage(@NotNull InputStream input) throws IOException {
        BufferedImage im = ImageIO.read(input);

//    Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image loaded %dx%d pixels", im.getWidth(), im.getHeight()));

        scaleImageToOutput(im);
        input.close();
        return this;
    }

    /**
     * set an already loaded image to the imageprocessor
     */
    public ImageProcessor loadImage(@NotNull BufferedImage image) {
        scaleImageToOutput(image);
        return this;
    }

    /**
     * sets the output image of another {@link ImageProcessor}
     */
    public ImageProcessor loadImage(@NotNull ImageProcessor outputter) {
        scaleImageToOutput(outputter.getOutputImage());
        this.pathToImage = outputter.pathToImage;
        return this;
    }

    /**
     * sents the resulting image to the specified server, using the given format.
     * The server may be a rasperry pi or any other system that understands the given data format.
     *
     * @param host         destination server
     * @param port         the port
     * @param outputFormat the outputformat of the image
     * @return this
     * @throws IOException if sending the image failed for any reason
     */
    public ImageProcessor sentToSocket(@NotNull String host, int port, @NotNull OutputFormat outputFormat) throws IOException {
        try {
            Socket s = new Socket(host, port);
            long start = System.currentTimeMillis();
            if (scaled != null)
                outputFormat.saveImage(scaled, new BufferedOutputStream(s.getOutputStream()));
//      Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image sent to %s:%d, took %d ms", host, port, System.currentTimeMillis() - start));
            s.close();
        } catch (IOException e) {
            throw new IOException(String.format("could not sent image to host: %s:%d, make sure receiving server is running.", host, port), e);
        }
        return this;
    }

    /**
     * writes the resulting image to the specified file or to the specified directory using the name of the input file together with the format extension.
     *
     * @param outFileOrDirectory if it is a file, it is used as direct output file. If it is a directory, it servers as parent directory and the filename is either calculated from the input file of this processor or just "image"
     * @param outputFormat       the outputformat of the image
     * @return this
     * @throws IOException if storing the image failed for any reason
     */
    public ImageProcessor saveImage(@Nullable File outFileOrDirectory, @NotNull OutputFormat outputFormat) throws IOException {
        File outputFile;

        if (outFileOrDirectory != null && outFileOrDirectory.isFile())
            outputFile = outFileOrDirectory;
        else {
            if (pathToImage == null && outFileOrDirectory == null) {
                throw new IOException("Could not save image, no output file name specified.");
            }

            if (pathToImage == null) {
                pathToImage = new File("output." + outputFormat.getFormatExtension());
            }

            File out = outFileOrDirectory != null ? outFileOrDirectory : pathToImage.getParentFile();
            outputFile = new File(out, pathToImage.getName().split("\\.")[0] + "." + outputFormat.getFormatExtension().toLowerCase());
        }

        try {
            long start = System.currentTimeMillis();
            if (scaled != null)
                outputFormat.saveImage(scaled, new BufferedOutputStream(new FileOutputStream(outputFile)));
//      Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image written to %s, took %d ms", outputFile.getAbsoluteFile().toString(), System.currentTimeMillis() - start));
        } catch (IOException e) {
            throw new IOException("Could not save image.", e);
        }

        return this;
    }


    private ImageProcessor scaleImageToOutput(@NotNull BufferedImage im) {
        AffineTransform at = new AffineTransform();
        at.scale((double) outputWidth / im.getWidth(), (double) outputHeight / im.getHeight());
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        scaled = scaleOp.filter(im, null);
        return this;
    }

    public void clear() {
        scaled = new BufferedImage(scaled.getWidth(), scaled.getHeight(), BufferedImage.TYPE_INT_RGB);

    }

    public void setRGB(int x, int y, int color) {
        if ((x >= 0 && x < outputWidth) && (y >= 0 && y < outputHeight))
            getOutputImage().setRGB(x, y, color);
    }

    public void drawLine(int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
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

    public int[][] toRGBArray() {
        int[][] rgb = new int[outputWidth][outputHeight];
        for (int x = 0; x < outputWidth; x++) {
            for (int y = 0; y < outputHeight; y++) {
                rgb[x][y] = scaled.getRGB(x, y);
            }
        }
        return rgb;
    }
}
