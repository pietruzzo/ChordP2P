package com.distributed.chordApp.cooperativemirroring.utilities.consoleInterface;

import java.io.Serializable;

public class OutputMessage implements Serializable {

    /**
     * Use '/' to separate lines
     */
    String message;
    MessageOptions messageOptions;

    public OutputMessage(String message, MessageOptions messageOptions){
        this.message = message;
        this.messageOptions = messageOptions;
    }

    public enum MessageOptions { ISERROR, ISOPTION, ISMESSAGE}

}
