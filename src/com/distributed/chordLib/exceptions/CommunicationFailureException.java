package com.distributed.chordLib.exceptions;

import com.distributed.chordLib.chordCore.Node;

public class CommunicationFailureException extends RuntimeException {

    private String n;

    public CommunicationFailureException(String nodeIP) {
        super();
        this.n = n;
    }

    public String getN() {
        return n;
    }
}
