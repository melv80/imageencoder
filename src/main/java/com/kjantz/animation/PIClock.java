package com.kjantz.animation;

/* File Creation Info:
 * User: KristianJ
 * Date: 14.05.2018
 * Time: 17:08
 *
 * Copyright LucaNet AG
 */

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.ui.PICanvas;
import com.kjantz.util.Async;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;

/**
 * Not documented yet.
 * //since - VERSION
 * //author - EXPERT TEAM
 */
public class PIClock {

    // =========================== Class Variables ===========================79
    private static final double pi_2 = Math.PI *2;
    private static final double delta = pi_2 / 360;
    // =============================  Variables  =============================79
    @Nullable
    private PICanvas canvas;
    private int r = 15;
    private int x = 20;
    private int y = 100;

    private Vector2D second = new Vector2D(0, 0);
    private Vector2D minute = new Vector2D(0, 0);
    private Vector2D hour = new Vector2D(0, 0);
    private ImageProcessor processor;

    // ============================  Constructors  ===========================79
    // ===========================  public  Methods  =========================79

    /**
     *
     * @param canvas or null if you just want to render one image
     */
    public PIClock(PICanvas canvas) {
        this.canvas = canvas;
        this.processor = new ImageProcessor(128, 128);
    }

    public PIClock(ImageProcessor p) {
        this.processor = p;
        this.canvas = null;
    }

    // =================  protected/package local  Methods ===================79
    public void start() {
        if (canvas != null) {
            final Thread thread = new Thread(() -> {
                while (true) {
                    draw();
                    canvas.setImageProcessor(processor);
                    Async.sleep(500);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }else {
            draw();
        }

    }

    private void draw() {
        int steps = 0;
        for (double a = 0; a < pi_2; a += delta, steps++) {
            int radius = r;
            // outer
            final int outer_x = (int) (this.x + Math.sin(a) * radius);
            final int outer_y = (int) (this.y + Math.cos(a) * radius);

            processor.setRGB(outer_x, outer_y, 255);

            radius -= (r / 8);
            final int inner_x = (int) (this.x + Math.sin(a) * radius);
            final int inner_y = (int) (this.y + Math.cos(a) * radius);

            //inner
            processor.setRGB(inner_x, inner_y, 255);

            if (steps % 30 == 0) {
                processor.drawLine(outer_x, outer_y, inner_x, inner_y, 0xFFFFFF);
            }

            final int s = LocalTime.now().getSecond();
            final int m = (LocalTime.now().getMinute()) % 60;
            final int h = (LocalTime.now().getHour()) % 12;

            this.second = drawPointer(60, s, processor, this.second, radius - r / 8, 0xFF0000);
            minute = drawPointer(60 * 60, m * 60 + s, processor, minute, radius, 255);
            hour = drawPointer(12 * 60, h * 60 + m, processor, hour, radius - r / 4, 255);
        }
    }

    private Vector2D drawPointer(int base, int current, ImageProcessor p, Vector2D last, int radius, int color) {
        double degree_s = -(pi_2/ base * current) - Math.PI ;
        p.drawLine(x, y, (int)last.getX(), (int)last.getY(), 0);
        Vector2D res = new Vector2D((this.x + Math.sin(degree_s) * radius), (int) (this.y + Math.cos(degree_s) * radius));
        p.drawLine(x, y, (int)res.getX(), (int)res.getY(), color);
        return res;
    }
    // ===========================  private  Methods  ========================79
    // ============================  Inner Classes  ==========================79
    // ============================  End of class  ===========================79

}

