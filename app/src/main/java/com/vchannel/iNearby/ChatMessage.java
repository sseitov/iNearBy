package com.vchannel.iNearby;

/**
 * Created by sseitov on 07.04.15.
 */
public class ChatMessage {
    public boolean fromMe;
    public String message;

    public ChatMessage(boolean fromMe, String message) {
        super();
        this.fromMe = fromMe;
        this.message = message;
    }
}
