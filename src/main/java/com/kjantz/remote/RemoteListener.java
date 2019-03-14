package com.kjantz.remote;

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.messagebroker.client.MessageClient;
import com.kjantz.messagebroker.model.Message;

public class RemoteListener {

    private ImageProcessor processor;
    private MessageClient client;

    private RemoteListener(ImageProcessor processor) {
        this.processor = processor;
    }

    public static void create(String remoteControlHost, ImageProcessor processor) {

        RemoteListener r = new RemoteListener(processor);
        MessageClient client = MessageClient.create()
                .connectTo(remoteControlHost)
                .registerConsumer(r::dispatch)
                .build();
        r.setClient(client);
    }

    private void setClient(MessageClient client) {
        this.client = client;
    }

    private void dispatch(Message message) {
        System.out.println(message);
    }
}

