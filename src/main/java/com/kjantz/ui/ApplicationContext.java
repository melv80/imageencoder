package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;

import java.util.function.Consumer;

public class ApplicationContext {
  private final PICanvas canvas;
  private final ImageProcessor processor;
  private final Consumer<String> logger;

  public ApplicationContext(PICanvas canvas, ImageProcessor processor, Consumer<String> logger) {
    this.canvas = canvas;
    this.processor = processor;
    this.logger = logger;
  }

  public PICanvas getCanvas() {
    return canvas;
  }

  public ImageProcessor getProcessor() {
    return processor;
  }

  public Consumer<String> getStatus() {
    return logger;
  }
}
