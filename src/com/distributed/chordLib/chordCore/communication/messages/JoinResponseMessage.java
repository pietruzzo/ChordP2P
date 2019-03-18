package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.ChordClient;
import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class JoinResponseMessage extends Message implements Serializable, ResponseMessage {

    private static final long serialVersionUID = 40001L;

    public final int numFingers;
    public final int numSuccessors;
    public final Node successor;
    public final int module;

    public JoinResponseMessage(int numFingers, int numSuccessors, Node successor, int reqId, int module){
        super(reqId);
        this.numFingers = numFingers;
        this.numSuccessors = numSuccessors;
        this.successor = successor;
        this.module = module;
    }
    public JoinResponseMessage(ChordClient.InitParameters params, int reqId){
        super(reqId);
        this.numFingers = params.numFingers;
        this.numSuccessors = params.numSuccessors;
        this.successor = params.successor;
        this.module = params.module;
    }
}
