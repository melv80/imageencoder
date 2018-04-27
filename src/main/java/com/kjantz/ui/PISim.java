package com.kjantz.ui;

import com.kjantz.animation.Animation;
import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.imageencoder.OutputFormat;
import com.kjantz.renderer.SimpleRenderer;
import com.kjantz.util.Async;
import com.kjantz.util.Constants;
import com.sun.istack.internal.Nullable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.function.UnaryOperator;

import static com.kjantz.util.Constants.DEFAULT_X_OUTPUT;
import static com.kjantz.util.Constants.DEFAULT_Y_OUTPUT;

public class PISim extends Application {

  private final PICanvas canvas = new PICanvas(800, 600, DEFAULT_X_OUTPUT, DEFAULT_Y_OUTPUT);
  private final Label status = new Label("Ready.");

  private ImageProcessor processor = new ImageProcessor(DEFAULT_X_OUTPUT, DEFAULT_Y_OUTPUT);

  private ControlPanel controlPanel;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("PI Simulator");
    BorderPane borderPane = new BorderPane();
    Scene s = new Scene(borderPane, 1280, 768, Color.BLACK);
    //loadImage(new File("c:\\users\\kristianj\\ideaprojects\\imageencoder\\src\\main\\resources\\test.jpg"));

    TitledPane pi_output = new TitledPane("PI Output", new ScrollPane(canvas));
    borderPane.setCenter(pi_output);
    BorderPane.setAlignment(pi_output, Pos.TOP_LEFT);


    ApplicationContext context = new ApplicationContext(canvas, processor, this::setStatus);
    controlPanel = new ControlPanel(context);
    borderPane.setRight(controlPanel);

    borderPane.setBottom(status);
    BorderPane.setAlignment(status, Pos.TOP_LEFT);


    // TODO: 17.04.2018 temporary animation
    /*Animation animation = new Animation(canvas);
    Thread t = new Thread(() -> {
      while (true) {
        animation.nextFrame();
        sleep(1000);
        clear();
      }
    });
    t.setDaemon(true);
    t.start();*/

    primaryStage.setScene(s);
    primaryStage.setMaximized(true);

    primaryStage.show();
  }




  public void setStatus(String status) {
    Platform.runLater(() -> {
      this.status.setText(String.format("%s: %s ", Constants.DATE_FORMAT.format(Date.from(Instant.now())), status));
    });
  }


}
