package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.imageencoder.OutputFormat;
import com.kjantz.renderer.Simple3DModel;
import com.kjantz.renderer.SimpleRenderer;
import com.kjantz.util.Async;
import com.kjantz.util.Constants;
import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.File;
import java.io.IOException;
import java.util.function.UnaryOperator;

import static com.kjantz.renderer.SimpleRenderer.*;
import static com.kjantz.util.Constants.DEFAULT_X_OUTPUT;
import static com.kjantz.util.Constants.DEFAULT_Y_OUTPUT;

public class ControlPanel extends TitledPane {

    private final Button clearAction = new Button("Clear");
    private final Button sentAction = new Button("Send Image");
    private final Button loadAction = new Button("Load Image ...");


    private final TextField outputX = new TextField(String.valueOf(DEFAULT_X_OUTPUT));
    private final TextField outputY = new TextField(String.valueOf(DEFAULT_Y_OUTPUT));
    private final TextField network = new TextField("192.168.180.3:81");
    private final TextField repeatDelay = new TextField(String.valueOf(250));

    private final Spinner<Integer> repeatSent = new Spinner<>(-1, 10000, 1);
    private final GridPane buttonPane = new GridPane();

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


        sentAction.setOnAction(sentImageAction);
        buttonPane.add(sentAction, 0, row++, 2, 1);

        clearAction.setOnAction(event -> clear());
        buttonPane.add(clearAction, 0, row++, 1, 1);
        Button startCube = new Button("Rotate Cube");
        startCube.setOnAction((e) -> {
            rotating = !rotating;
            SimpleRenderer renderer = new SimpleRenderer();
            renderer.setCameraPosition(new Vector3D(0, 0, -20));

            int w = 30;
            int d = 65;

            new Thread(() -> {
                PICanvas canvas = applicationContext.getCanvas();

                double degree = 0;
                int[] colors = new int[]{0xff0000, 0xff0000, 0xff0000};
                ImageProcessor imageProcessor = new ImageProcessor(Constants.DEFAULT_X_OUTPUT, Constants.DEFAULT_Y_OUTPUT);
                Simple3DModel model = Simple3DModel.CUBE;

                while (rotating) {
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

                            drawLine(x, y, x1, y1, colors[j], imageProcessor);
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
        clear();
    }

    public void drawLine(int x0, int y0, int x1, int y1, int color, ImageProcessor img) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            if ((x0 >= 0 && x0 < Constants.DEFAULT_X_OUTPUT) && (y0 >= 0 && y0 < Constants.DEFAULT_Y_OUTPUT))
                img.getOutputImage().setRGB(x0, y0, color);

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
            applicationContext.getProcessor().loadImage(file);
            applicationContext.getCanvas().setImageProcessor(applicationContext.getProcessor());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
