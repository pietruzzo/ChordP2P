package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

public class JoinResponseMessage extends ReqResp {

    private static final long serialVersionUID = 40001L;

    public final int numFingers;
    public final int numSuccessors;
    public final Node successor;

    public JoinResponseMessage(int numFingers, int numSuccessors, Node successor, int reqId){
        super(reqId);
        this.numFingers = numFingers;
        this.numSuccessors = numSuccessors;
        this.successor = successor;
    }
}
