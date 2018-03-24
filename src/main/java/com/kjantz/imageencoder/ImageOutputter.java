package com.kjantz.imageencoder;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageOutputter {
  private final int outputWidth;
  private final int outputHeight;

  private BufferedImage scaled;
  private File pathToImage;

  public ImageOutputter(int outputWidth, int outputHeight) {
    this.outputWidth = outputWidth;
    this.outputHeight = outputHeight;
  }

  public BufferedImage getOutputImage() {
    return scaled;
  }

  public ImageOutputter loadImage(File pathToImage) throws IOException {
    BufferedImage im = ImageIO.read(pathToImage);
    if (im.getWidth() != im.getHeight()) {
      throw new IOException(String.format("Image needs to be squared current resolution is %d x %d ",im.getHeight(), im.getWidth()));
    }
    this.pathToImage = pathToImage;

    Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image loaded %dx%d pixels", im.getWidth(), im.getHeight()));

    scaleImageToOutput(im);
    return this;
  }

  public ImageOutputter loadImage(@NotNull BufferedImage image) {
    scaleImageToOutput(image);
    return this;
  }

  public ImageOutputter loadImage(@NotNull ImageOutputter outputter) {
    scaleImageToOutput(outputter.getOutputImage());
    this.pathToImage = outputter.pathToImage;
    return this;
  }

  private ImageOutputter scaleImageToOutput(@NotNull BufferedImage im) {
    AffineTransform at = new AffineTransform();
    at.scale((double)outputWidth / im.getWidth() , (double)outputHeight / im.getHeight() );
    AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
    scaled = scaleOp.filter(im, null);
    Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image scaled to %dx%d pixels", scaled.getWidth(), scaled.getHeight()));
    return this;
  }

  public ImageOutputter sentToSocket(@NotNull String host, int port, Format format) throws IOException {
    Socket s = new Socket(host, port);
    long start = System.currentTimeMillis();
    format.saveImage(scaled, new BufferedOutputStream(s.getOutputStream()));
    Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image sent to %s:%d, took %d ms", host, port, System.currentTimeMillis() - start));
    s.close();
   return this;
  }
  public ImageOutputter saveImage(@Nullable File outFileOrDirectory, Format format) throws IOException {
    File outputFile;

    if (outFileOrDirectory != null && outFileOrDirectory.isFile())
      outputFile = outFileOrDirectory;
    else {
      if (pathToImage == null && outFileOrDirectory == null) {
        throw new IOException("no output file specified");
      }

      if (pathToImage == null) {
        pathToImage = new File("output."+format.getFormatExtension());
      }

      File out = outFileOrDirectory != null ? outFileOrDirectory : pathToImage.getParentFile();
      outputFile = new File(out, pathToImage.getName().split("\\.")[0] + "." + format.getFormatExtension().toLowerCase());
    }

    long start = System.currentTimeMillis();
    format.saveImage(scaled, new BufferedOutputStream(new FileOutputStream(outputFile)));
    Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Image written to %s, took %d ms", outputFile.getAbsoluteFile().toString(), System.currentTimeMillis() - start));
    return this;
  }
}
