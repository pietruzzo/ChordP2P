package com.distributed.chordLib.exceptions;

public class CommunicationFailureException extends RuntimeException {

    private InterruptedException e;

    public CommunicationFailureException(InterruptedException e){
        this.e = e;
    }
}
