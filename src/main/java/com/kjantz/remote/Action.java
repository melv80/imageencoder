package com.kjantz.remote;

import com.kjantz.imageencoder.ImageProcessor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public enum Action {
    DASHBOARD("dashboard", "dashboard.png"),
    NOP("nop", "nop.png");

    Logger log = Logger.getLogger(getClass().getName());

    private final String command;
    private final String imageToLoad;

    private static final Map<String, Action> actions = new HashMap<>();

    Action(String command, String imageToLoad) {
        this.command = command;
        this.imageToLoad = imageToLoad;
    }

    public static synchronized Action get(String command) {
        Action action = actions.get(command);
        if (action == null) {
            for (Action value : values()) {
                if (value.command.equals(command)) {
                    action = value;
                    actions.put(command, action);
                    break;
                }
            }
        }
        if (action == null)
            return NOP;
        return action;
    }

    public void execute(ImageProcessor processor) {
        if (this == NOP) return;
        try {
            final File pathToImage = new File(imageToLoad);
            log.finest("loading image: "+pathToImage.getAbsolutePath());
            processor.loadImage(pathToImage);
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }
}

