package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.Node;

public class LookupResponse {

    private static final long serialVersionUID = 40004L;

    public final Node node;

    public LookupResponse(Node node){ this.node = node; }
}
