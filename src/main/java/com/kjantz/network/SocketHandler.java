package com.kjantz.network;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * internal utility class that reads the input of a socket and writes it to the console.
 */
class SocketHandler implements Runnable {
  private Socket socket;
  private OutputStream out;

  SocketHandler(Socket socket, OutputStream outputStream) {
    this.socket = socket;
    this.out = outputStream;
  }

  @Override
  public void run() {

    try {
      InputStream inputStream = socket.getInputStream();
      byte[] buffer = new byte[1024];
      Logger.getLogger(getClass().getName()).log(Level.INFO, "Client connected.");
      while (socket.isConnected()) {
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1)
          break;
        out.write(buffer, 0, bytesRead);
        out.flush();
      }
      Logger.getLogger(getClass().getName()).log(Level.INFO, "Client disconnected.");
    } catch (IOException e) {
      try {
        out.close();
      } catch (IOException e1) {
        //
      }
      e.printStackTrace();
    }

  }
}
