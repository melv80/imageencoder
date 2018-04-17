package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.imageencoder.OutputFormat;
import com.sun.istack.internal.Nullable;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.function.UnaryOperator;

public class PISim extends Application {
  private static final int DEFAULT_X_OUTPUT = 64;
  private static final int DEFAULT_Y_OUTPUT = 64;

  private final PICanvas canvas = new PICanvas(800  ,600, DEFAULT_X_OUTPUT,DEFAULT_Y_OUTPUT);
  private final TextField outputX = new TextField(String.valueOf(DEFAULT_X_OUTPUT));
  private final TextField outputY = new TextField(String.valueOf(DEFAULT_Y_OUTPUT));
  private final TextField network = new TextField("localhost:8181");
  private final Label status = new Label("Ready.");

  private ImageProcessor processor = new ImageProcessor(DEFAULT_X_OUTPUT, DEFAULT_Y_OUTPUT);

  private final UnaryOperator<TextFormatter.Change> intFilter = change -> {
    String text = change.getText();

    if (text.matches("[0-9]*")) {
      return change;
    }

    return null;
  };
  private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss");

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("PI Simulator");
    BorderPane borderPane = new BorderPane();
    Scene s = new Scene(borderPane, 1280, 768, Color.BLACK);
    loadImage(new File("c:\\users\\kristianj\\ideaprojects\\imageencoder\\src\\main\\resources\\test.jpg"));


    TitledPane pi_output = new TitledPane("PI Output", canvas);
    borderPane.setCenter(pi_output);
    BorderPane.setAlignment(pi_output, Pos.TOP_LEFT);

    GridPane buttonPane = new GridPane();
    buttonPane.setVgap(4);
    buttonPane.setPadding(new Insets(5, 5, 5, 5));
    buttonPane.add(new Label("Output X: "), 0, 0);

    outputX.setTextFormatter(new TextFormatter<Object>(intFilter));
    outputY.setTextFormatter(new TextFormatter<Object>(intFilter));

    buttonPane.add(outputX, 1, 0);
    buttonPane.add(new Label("Output Y: "), 0, 1);
    buttonPane.add(outputY, 1, 1);
    buttonPane.add(new Label("PI address: "), 0, 2);
    buttonPane.add(network, 1, 2);
    Button loadAction = new Button("Load Image ...");
    loadAction.setOnAction(event -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Image ...");
      File res = fileChooser.showOpenDialog(null);
      loadImage(res);
    });
    buttonPane.add(loadAction, 0, 4, 2, 1);


    Button saveAction = new Button("Sent Image");
    saveAction.setOnAction(event -> {
      File res = new File("output.pi");
      try {
        res.createNewFile();
        processor.saveImage(res, OutputFormat.PI);
        String[] hostAndPort = network.getText().split(":");
        processor.sentToSocket(hostAndPort[0], Integer.valueOf(hostAndPort[1]), OutputFormat.PI);
        setStatus("Sent to PI.");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    buttonPane.add(saveAction, 0, 5, 2, 1);

    TitledPane buttons = new TitledPane("Controls", buttonPane);
    borderPane.setRight(buttons);
    borderPane.setBottom(status);
    canvas.setImageProcessor(processor);

    primaryStage.setScene(s);
    primaryStage.sizeToScene();
    primaryStage.show();

    setStatus("Ready.");
  }

  public void setStatus(String status) {
    this.status.setText(String.format("%s: %s ", DATE_FORMAT.format(Date.from(Instant.now())), status));
  }

  public void loadImage(@Nullable File file) {
    if (file == null) return;
    try {
      processor.loadImage(file);
      canvas.setImageProcessor(processor);

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}
