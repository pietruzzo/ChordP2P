package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class LookupResponseMessage extends Message implements Serializable, ResponseMessage {

    private static final long serialVersionUID = 40004L;

    public final Node node;

    public LookupResponseMessage(Node node, int reqId){
        super(reqId);
        this.node = node;
    }
}
