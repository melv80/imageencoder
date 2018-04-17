package com.kjantz.network;

import java.io.IOException;
import java.net.Socket;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket s = new Socket("localhost", 8181);

        new Thread(() -> {
            try {
                int letter;
                while ((letter = s.getInputStream().read()) > 0) {
                    System.out.print((char) letter);
                }
            }catch(IOException e) {
                e.printStackTrace();
            }
        }).start();

        while(true) {
            System.out.println("Sending...");
            s.getOutputStream().write("32 32 ffffff\n".getBytes());
            Thread.sleep(1000);
        }

    }
}
