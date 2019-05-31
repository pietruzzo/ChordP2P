package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class PredecessorResponseMessage extends Message implements Serializable, ResponseMessage {

    private static final long serialVersionUID = 40008L;

    public Node node;
    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     *
     * @param id
     * @param predecessor
     */
    public PredecessorResponseMessage(Integer id, Node predecessor) {
        super(id);
        this.node = predecessor;
    }
}
