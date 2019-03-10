package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

public class JoinResponseMessage {

    private static final long serialVersionUID = 40001L;

    public final int numFingers;
    public final int numSuccessors;
    public final Node successor;

    public JoinResponseMessage(int numFingers, int numSuccessors, Node successor){
        this.numFingers = numFingers;
        this.numSuccessors = numSuccessors;
        this.successor = successor;
    }
}
