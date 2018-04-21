package com.kjantz.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Async {
  private static final ExecutorService workerThread = Executors.newFixedThreadPool(8);

  public static void execute(Runnable r){
    workerThread.submit(r);
  }

  public static void sleep(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
