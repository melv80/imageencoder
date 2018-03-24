package com.kjantz.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple test class, that allows to test the network output of the {@link com.kjantz.imageencoder.ImageOutputter}
 */
public class NetworkConsoleLoopback {

  private volatile boolean running;
  private int port;

  /**
   * constructs a NetworkConsoleLoopback on the given port
   * @param port port that will be listened on for TCP/IP connections
   */
  public NetworkConsoleLoopback(int port) {
    this.port = port;
  }

  /**
   * May be called to stop the NetworkConsoleLoopback externally
   */
  public void stop() {
    running = false;
  }

  /**
   * Starts listening on the specified port and redirects output to System.out
   * @throws IOException
   */
  public void start() throws IOException {
    Thread t = new Thread(() -> {
        try {
          Logger.getLogger(NetworkConsoleLoopback.class.getName()).log(Level.INFO, "Listing on port: "+port+" ...");
          ServerSocket s = new ServerSocket(port);
          ExecutorService threads = Executors.newFixedThreadPool(2);
          running = true;
          while (running) {
            threads.submit(new SocketHandler(s.accept(), System.out));
          }
        } catch (IOException e) {
          Logger.getLogger(NetworkConsoleLoopback.class.getName()).log(Level.SEVERE, "could not start loop back listener", e);
        }
      });

    t.setName("Console Loop Back Connection Acceptor");
    t.start();

  }

  public static void main(String[] args) throws IOException {
    Logger.getLogger(NetworkConsoleLoopback.class.getName()).log(Level.INFO,"NetworkConsoleLoopback started.");
    new NetworkConsoleLoopback(8181).start();
  }
}
