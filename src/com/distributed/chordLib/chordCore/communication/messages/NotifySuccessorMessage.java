package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

import java.io.Serializable;

public class NotifySuccessorMessage implements Serializable {

    private static final long serialVersionUID = 40005L;

    public final Node node;

    public NotifySuccessorMessage(Node node) {
        this.node = node; }
}
