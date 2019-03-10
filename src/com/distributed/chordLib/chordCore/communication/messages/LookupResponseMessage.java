package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

public class LookupResponseMessage {

    private static final long serialVersionUID = 40004L;

    public final Node node;

    public LookupResponseMessage(Node node){ this.node = node; }
}
