package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class JoinResponseMessage extends ReqResp implements Serializable {

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
    public JoinResponseMessage(ChordClient.InitParameters params, int reqId){
        super(reqId);
        this.numFingers = params.numFingers;
        this.numSuccessors = params.numSuccessors;
        this.successor = params.successor;
    }
}
