package com.distributed.chordApp.cooperativemirroring.utilities.consoleInterface;

import java.io.Serializable;

public class OutputMessage implements Serializable {

    String message;
    boolean isError;

    public OutputMessage(String message, boolean isError){
        this.message = message;
        this.isError = isError;
    }
}
