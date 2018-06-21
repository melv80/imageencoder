package com.kjantz.util;

/* File Creation Info:
 * User: KristianJ
 * Date: 30.05.2018
 * Time: 11:50
 *
 * Copyright LucaNet AG
 */

import javafx.scene.paint.Color;

/**
 * Not documented yet.
 * //since - VERSION
 * //author - EXPERT TEAM
 */
public class Util {
    // =========================== Class Variables ===========================79
    // =============================  Variables  =============================79
    // ============================  Constructors  ===========================79
    // ===========================  public  Methods  =========================79
    public static int toIntColor(Color color) {
        int rgb = (int) (color.getRed() * 255) << 16 | (int) (color.getGreen() * 255) << 8 | (int) (color.getBlue() * 255);
        return rgb;
    }
    // =================  protected/package local  Methods ===================79
    // ===========================  private  Methods  ========================79
    // ============================  Inner Classes  ==========================79
    // ============================  End of class  ===========================79
}

