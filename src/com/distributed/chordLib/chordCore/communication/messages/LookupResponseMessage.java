package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

public class LookupResponseMessage extends ReqResp {

    private static final long serialVersionUID = 40004L;

    public final Node node;

    public LookupResponseMessage(Node node, int reqId){
        super(reqId);
        this.node = node;
    }
}
