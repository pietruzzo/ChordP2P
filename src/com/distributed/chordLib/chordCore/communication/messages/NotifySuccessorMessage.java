package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class NotifySuccessorMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40005L;


    public NotifySuccessorMessage(Node node) { super(null); }
}
