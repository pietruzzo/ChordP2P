package com.distributed.chordLib.chordCore.communication.messages;

public class PredecessorRequestMessage extends Message {

    private static final long serialVersionUID = 40007L;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     */
    public PredecessorRequestMessage() {
        super(null);
    }
}
