package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class PingRequestMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40006L;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     *
     * @param id
     */

    public PingRequestMessage() {
        super(null);
    }
}
