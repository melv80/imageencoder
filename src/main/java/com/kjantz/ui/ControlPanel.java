package com.kjantz.ui;

import com.kjantz.animation.PIClock;
import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.imageencoder.OutputFormat;
import com.kjantz.renderer.Simple3DModel;
import com.kjantz.renderer.SimpleRenderer;
import com.kjantz.util.Async;
import com.kjantz.util.Constants;
import com.madgag.gif.fmsware.GifDecoder;
import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import static com.kjantz.renderer.SimpleRenderer.*;
import static com.kjantz.util.Constants.DEFAULT_X_OUTPUT;
import static com.kjantz.util.Constants.DEFAULT_Y_OUTPUT;

public class ControlPanel extends TitledPane {

    private final Button clearAction = new Button("Clear");
    private final Button sentAction = new Button("Send Image");
    private final Button loadAction = new Button("Load Image ...");
    private final Button reconnect = new Button("Reconnect");
    private final Button clock = new Button("Clock");


    private final TextField outputX = new TextField(String.valueOf(DEFAULT_X_OUTPUT));
    private final TextField outputY = new TextField(String.valueOf(DEFAULT_Y_OUTPUT));
    private final TextField network = new TextField("192.168.180.5:81");
    private final TextField repeatDelay = new TextField(String.valueOf(250));

    private final Spinner<Integer> repeatSent = new Spinner<>(-1, 10000, 1);
    private final GridPane buttonPane = new GridPane();
    private Optional<Socket> liveConnection = Optional.empty();

    private Vector3D cameraPos = new Vector3D(0, 0, -20);

    private boolean rotating = false;

    private final UnaryOperator<TextFormatter.Change> intFilter = change -> {
        String text = change.getText();

        if (text.matches("[0-9]*")) {
            return change;
        }

        return null;
    };

    private ApplicationContext applicationContext;

    private final EventHandler<ActionEvent> sentImageAction = event -> {
        Async.execute(this::sendImage);
    };
    ;


    public ControlPanel(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        buttonPane.setVgap(4);
        setPadding(new Insets(5, 5, 5, 5));
        int row = 0;


        outputX.setTextFormatter(new TextFormatter<Object>(intFilter));
        outputY.setTextFormatter(new TextFormatter<Object>(intFilter));
        repeatDelay.setTextFormatter(new TextFormatter<Object>(intFilter));

        buttonPane.add(new Label("Output X: "), 0, row);
        buttonPane.add(outputX, 1, row++);

        buttonPane.add(new Label("Output Y: "), 0, row);
        buttonPane.add(outputY, 1, row++);

        buttonPane.add(new Label("PI address: "), 0, row);
        buttonPane.add(network, 1, row++);

        buttonPane.add(new Label("Repeat Sending: "), 0, row);
        buttonPane.add(repeatSent, 1, row++);

        buttonPane.add(new Label("Send Delay (ms):"), 0, row);
        buttonPane.add(repeatDelay, 1, row++);

        loadAction.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Image ...");
            File res = fileChooser.showOpenDialog(null);
            loadImage(res);
        });
        buttonPane.add(loadAction, 0, row++, 2, 1);
        buttonPane.add(reconnect, 0, row++, 2, 1);

        buttonPane.add(clock, 0, row++, 2, 1);
        clock.setOnAction(e -> new PIClock(applicationContext.getCanvas()).start());


        sentAction.setOnAction(sentImageAction);
        buttonPane.add(sentAction, 0, row++, 2, 1);


        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.PLUS) {
                cameraPos = cameraPos.add(new Vector3D(0, 0, 1));
            }
            else if (event.getCode() == KeyCode.MINUS) {
                cameraPos = cameraPos.add(new Vector3D(0, 0, -1));
            }
            else if (event.getCode() == KeyCode.W) {
                cameraPos = cameraPos.add(new Vector3D(0, -1, 0));
            }
            else if (event.getCode() == KeyCode.S) {
                cameraPos = cameraPos.add(new Vector3D(0, 1, -1));
            }
            else if (event.getCode() == KeyCode.A) {
                cameraPos = cameraPos.add(new Vector3D(-1, 0, 0));
            }
            else if (event.getCode() == KeyCode.D) {
                cameraPos = cameraPos.add(new Vector3D(1, 0, 0));
            }
        });

        clearAction.setOnAction(event -> clear());
        buttonPane.add(clearAction, 0, row++, 1, 1);
        Button startCube = new Button("Rotate Cube");
        startCube.setOnAction((e) -> {
            rotating = !rotating;
            SimpleRenderer renderer = new SimpleRenderer();

            int w = 30;
            int d = 65;

            new Thread(() -> {
                PICanvas canvas = applicationContext.getCanvas();

                double degree = 0;
                int[] colors = new int[]{0xff0000, 0xff0000, 0xff0000};
                ImageProcessor imageProcessor = new ImageProcessor(Constants.DEFAULT_X_OUTPUT, Constants.DEFAULT_Y_OUTPUT);
                Simple3DModel model = Simple3DModel.CUBE;

                while (rotating) {
                    renderer.setCameraPosition(cameraPos);
                    degree+=0.05;
                    degree %= 360;
                    imageProcessor.clear();
                    int vertexIndex = 0;
                    for (Vector3D coord : model.getVertices()) {
                        Vector2D pix = renderer.project(rotateX3D(degree, rotateY3D(degree, coord)));
                        int x = (int) (pix.getX() * w) + d;
                        int y = (int) (pix.getY() * w) + d;

                        int[] indizes = model.getEdges().get(vertexIndex);
                        for (int j = 0; j < indizes.length; j++) {
                            int index = indizes[j];
                            Vector2D end = renderer.project(rotateX3D(degree, rotateY3D(degree, model.getVertices().get(index))));
                            int x1 = (int) (end.getX() * w) + d;
                            int y1 = (int) (end.getY() * w) + d;

                            imageProcessor.drawLine(x, y, x1, y1, colors[j]);
                        }
                        vertexIndex++;
                    }
                    canvas.setImageProcessor(imageProcessor);

                    Async.sleep(Integer.valueOf(repeatDelay.getText()));

                }
            }).start();
        });
        buttonPane.add(startCube, 0, row++, 1, 1);
        ColorPicker colorChooser = new ColorPicker();
        buttonPane.add(colorChooser, 0, row++, 1, 1);

        applicationContext.getCanvas().getColorProperty().bind(colorChooser.valueProperty());
        setText("Controls");
        setContent(buttonPane);
        startLiveConnection();
        clear();
    }

    private void startLiveConnection() {
        PICanvas canvas = applicationContext.getCanvas();
        Thread t = new Thread( () -> {
            reconnect.setOnAction((e) -> {
                liveConnection.ifPresent(s -> {
                    try {
                        s.getOutputStream().flush();
                        s.close();
                    } catch (IOException e1) {
                        // NOOP
                        e1.printStackTrace();
                    }
                    applicationContext.getStatus().accept("Disconnected from PI.");
                });
            });
            
            while (true) {
                try {
                    String[] hostAndPort = network.getText().split(":");
                    Socket s = new Socket(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
                    PrintWriter w = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(s.getOutputStream())));
                    PICanvas.RGBPixelCallback c = (x, y, color, force) -> {
                        if (canvas.getRGB(x, y) == color && !force) return;

                        w.print(String.format("%d %d %s\n", x, y, Integer.toHexString(color)));
                        w.flush();
                        canvas.getLastFrame()[x][y] = color;
                    };

                    canvas.setPixelCB(c);
                    liveConnection = Optional.of(s);
                    applicationContext.getStatus().accept(String.format("Connected to PI (%s).", network.getText()));
                    while (!s.isClosed()) {
                        Async.sleep(500);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    int delay = 2000;
                    applicationContext.getStatus().accept(String.format("Disconnected from PI (%s). Trying to reconnect in %d s", network.getText(), delay /1000));
                    Async.sleep(delay);

                }
            }
        });
        t.setName("Live Canvas Connector");
        t.setDaemon(true);
        t.start();

    }



    private void sendImage() {
        final int[] repetitions = new int[1];
        repetitions[0] = repeatSent.getValue();
        String oldText = sentAction.getText();

        File res = new File("output.pi");
        try {
            Platform.runLater(() -> {
                sentAction.setText("Abort");
                sentAction.setOnAction((event) -> repetitions[0] = 0);
            });

            while (repetitions[0] > 0 || repetitions[0] == -1) {
                res.createNewFile();
                applicationContext.getProcessor().saveImage(res, OutputFormat.PI);
                String[] hostAndPort = network.getText().split(":");
                applicationContext.getProcessor().sentToSocket(hostAndPort[0], Integer.valueOf(hostAndPort[1]), OutputFormat.PI);
                applicationContext.getStatus().accept("Image has been successfully sent to PI.");

                if (repetitions[0] != 0) {
                    Async.sleep(Integer.valueOf(repeatDelay.getText()));
                }

                if (repetitions[0] > 0)
                    repetitions[0]--;
            }

        } catch (IOException e) {
            e.printStackTrace();
            applicationContext.getStatus().accept(e.getMessage());
        }

        Platform.runLater(() -> {
            sentAction.setText(oldText);
            sentAction.setOnAction(sentImageAction);
        });

    }


    public void clear() {

        applicationContext.getCanvas().clear(Color.gray(0));
        applicationContext.getStatus().accept("Ready Player One.");
    }

    public void loadImage(@Nullable File file) {
        if (file == null) return;
        try {
            if (file.getName().endsWith("gif")) {
                GifDecoder d = new GifDecoder();
                d.read(file.getAbsolutePath());
                new Thread(() -> {
                    for (int i = 0; i < d.getFrameCount();i++) {
                        applicationContext.getProcessor().loadImage(d.getFrame(i));
                        applicationContext.getCanvas().setImageRGB(applicationContext.getProcessor().toRGBArray());
                        Async.sleep(d.getDelay(i));
                    }
                    System.out.println("Closing animator thread");
                    applicationContext.getCanvas().clear(Color.BLACK);
                }).start();
            }else {
                applicationContext.getProcessor().loadImage(file);
                applicationContext.getCanvas().setImageProcessor(applicationContext.getProcessor());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
