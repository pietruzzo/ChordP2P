package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class PingMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40006L;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     *
     * @param id
     */
    public PingMessage(Integer id) {
        super(id);
    }

    public PingMessage() {
        super(null);
    }
}
