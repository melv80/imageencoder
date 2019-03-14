package com.kjantz;

import com.kjantz.animation.PIClock;
import com.kjantz.imageencoder.OutputFormat;
import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.remote.RemoteListener;
import com.kjantz.util.Async;
import com.kjantz.util.BlendMode;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Main {

    private Logger mainLogger = Logger.getLogger("Main");
    private Properties properties = new Properties();
    private ImageProcessor processor;

    private Main() {
        loadConfiguration();

    }

    private void loadConfiguration() {
        File configFile = getConfigFile();
        mainLogger.info("Reading config from " + configFile.getAbsolutePath());
        if (configFile.exists()) {
            try (FileInputStream inStream = new FileInputStream(configFile)) {
                properties.load(inStream);
                return;
            } catch (IOException e) {
                mainLogger.warning("Could not read config file, using default values.");
            }
        }
    }

    @NotNull
    public File getConfigFile() {
        return new File("default.conf");
    }

    public int getWidth() {
        return Integer.valueOf(properties.getOrDefault("width", 64).toString());
    }

    public int getHeight() {
        return Integer.valueOf(properties.getOrDefault("height", 64).toString());
    }

    public String getHost() {
        return properties.getOrDefault("host", "localhost").toString();
    }

    public int getPort() {
        return Integer.valueOf(properties.getOrDefault("port", 81).toString());
    }

    public String getRemoteControlHost() {
        return properties.getOrDefault("remote", "http://localhost:8080").toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Main().start(args);


    }

    private void start(String[] args) throws IOException {
        if (args.length >= 1) {
            String host = getHost();
            int port = getPort();
            String input;
            if (args.length == 3) {
                host = args[1].split(":")[0];
                port = Integer.parseInt(args[1].split(":")[1]);
            }



            processor = new ImageProcessor(getWidth(), getHeight());
            if ("-sent".equals(args[0])) {
                if (args.length == 2)
                    input = args[1];
                else
                    input = args[2];
                
                mainLogger.info("sending image");
                mainLogger.info("sending image to " + host + ":" + port);


                processor.loadImage(new File(input));
                processor.sentToSocket(host, port, OutputFormat.PI, BlendMode.IGNORE);
            } else if ("-clock".equals(args[0])) {
                PIClock piClock = new PIClock(processor);
                while(true) {
                    piClock.start();
                    processor.sentToSocket(host, port, OutputFormat.PI, BlendMode.IGNORE);
                    Async.sleep(500);
                }
            } else if ("-clear".equals(args[0])) {
                processor.clear();
                processor.sentToSocket(host, port, OutputFormat.PI, BlendMode.IGNORE);
            }
            else if ("-listen".equals(args[0])) {
                RemoteListener.create(getRemoteControlHost(), processor);
            }

            // no command
            else {
                printSynopsis();
                return;
            }



        } else {
            printSynopsis();
        }



    }

    private void printSynopsis() {
        System.out.println("Sent Image to PI");
        System.out.println("program might use default settings if present in file: " + getConfigFile().getAbsolutePath());
        System.out.println("usage: ");
        System.out.println("[-sent localhost:80 pathToImage");
        System.out.println("-sent pathToImage");
        System.out.println("**********************************");
        System.out.println("-clock localhost:80 pathToImage");
        System.out.println("-clock pathToImage");
        System.out.println("**********************************");
        System.out.println("-clear localhost:80 ");
        System.out.println("-clear");
    }


}
