package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class PingResponseMessage extends Message implements Serializable, ResponseMessage {

    private static final long serialVersionUID = 40009L;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     *
     * @param id
     */
    public PingResponseMessage(Integer id) {
        super(id);
    }
}
