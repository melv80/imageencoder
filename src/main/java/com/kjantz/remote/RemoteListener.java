package com.kjantz.remote;

import com.kjantz.imageencoder.ImageProcessor;
import com.kjantz.messagebroker.client.MessageClient;
import com.kjantz.messagebroker.model.Message;

import java.util.logging.Logger;

public class RemoteListener {

    private static final Logger log = Logger.getLogger(RemoteListener.class.getName());

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
        log.info("Listing for messages on "+client);
    }

    private void setClient(MessageClient client) {
        this.client = client;
    }

    private void dispatch(Message message) {
        log.info("received a message from " + message.getSenderID());
        Action.get(message.getMessageText()).execute(processor);
    }
}

