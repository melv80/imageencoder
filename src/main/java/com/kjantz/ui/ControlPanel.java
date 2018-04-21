package com.kjantz.ui;

import com.kjantz.imageencoder.OutputFormat;
import com.kjantz.util.Async;
import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.function.UnaryOperator;

import static com.kjantz.util.Constants.DEFAULT_X_OUTPUT;
import static com.kjantz.util.Constants.DEFAULT_Y_OUTPUT;

public class ControlPanel extends TitledPane {

  private final Button clearAction = new Button("Clear");
  private final Button sentAction = new Button("Send Image");
  private final Button loadAction = new Button("Load Image ...");


  private final TextField outputX = new TextField(String.valueOf(DEFAULT_X_OUTPUT));
  private final TextField outputY = new TextField(String.valueOf(DEFAULT_Y_OUTPUT));
  private final TextField network = new TextField("192.168.180.2:81");
  private final TextField repeatDelay = new TextField(String.valueOf(1000));

  private final Spinner<Integer> repeatSent = new Spinner<>(-1, 10000, 1);
  private final GridPane buttonPane = new GridPane();

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
  };;


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
    ColorPicker colorChooser = new ColorPicker();
    buttonPane.add(colorChooser, 0, row++, 1, 1);

    applicationContext.getCanvas().getColorProperty().bind(colorChooser.valueProperty());
    setText("Controls");
    setContent(buttonPane);
    clear();
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
