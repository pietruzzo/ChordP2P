package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class JoinRequestMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40000L;


    public JoinRequestMessage() {
        super(null);
    }
}
