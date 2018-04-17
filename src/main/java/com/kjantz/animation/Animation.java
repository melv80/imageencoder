package com.kjantz.animation;

import com.kjantz.ui.PICanvas;
import javafx.scene.paint.Color;

public class Animation {
  private PICanvas canvas;
  public Animation(PICanvas canvas) {
    this.canvas = canvas;
  }

  public void nextFrame() {
    boolean setLine = false;
    int setPixel = 0;
    for (int x = 0; x < 64; x++) {
      for (int y = 0; y < 64; y++) {
        if (setPixel == 0) {
          setLine = Math.random() > 0.98 || setLine;
        }
        if (setLine) {
          canvas.setRGB(x, y, Color.rgb(0, y * 25 & 0xFF, 0));
          setPixel++;
          if (setPixel > 7) setLine = false;
        }
      }
      setLine = false;
      setPixel = 0;
    }
  }
}
