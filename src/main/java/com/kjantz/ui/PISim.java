package com.kjantz.ui;

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.imageencoder.OutputFormat;
import com.sun.istack.internal.Nullable;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.function.UnaryOperator;

public class PISim extends Application {
  private static final int DEFAULT_X_OUTPUT = 64;
  private static final int DEFAULT_Y_OUTPUT = 64;

  private final PICanvas canvas = new PICanvas(800  ,600, DEFAULT_X_OUTPUT,DEFAULT_Y_OUTPUT);
  private final TextField outputX = new TextField(String.valueOf(DEFAULT_X_OUTPUT));
  private final TextField outputY = new TextField(String.valueOf(DEFAULT_Y_OUTPUT));
  private ImageProcessor processor = new ImageProcessor(DEFAULT_X_OUTPUT, DEFAULT_Y_OUTPUT);

  private final UnaryOperator<TextFormatter.Change> intFilter = change -> {
    String text = change.getText();

    if (text.matches("[0-9]*")) {
      return change;
    }

    return null;
  };

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("PI Simulator");
    BorderPane borderPane = new BorderPane();
    Scene s = new Scene(borderPane, 1280, 768, Color.BLACK);
    loadImage(new File("J:\\Entwicklung\\imageencoder\\src\\main\\resources\\test.jpg"));


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
    Button loadAction = new Button("Load Image ...");
    loadAction.setOnAction(event -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Image ...");
      File res = fileChooser.showOpenDialog(null);
      loadImage(res);
    });
    buttonPane.add(loadAction, 0, 3, 2, 1);


    Button saveAction = new Button("Save Image ...");
    saveAction.setOnAction(event -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Save PI Output ...");
      File res = fc.showSaveDialog(null);
      try {
        res.createNewFile();
        processor.saveImage(res, OutputFormat.PI);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    buttonPane.add(saveAction, 0, 4, 2, 1);

    TitledPane buttons = new TitledPane("Controls", buttonPane);
    borderPane.setRight(buttons);
    canvas.setImageProcessor(processor);

    primaryStage.setScene(s);
    primaryStage.sizeToScene();
    primaryStage.show();

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
