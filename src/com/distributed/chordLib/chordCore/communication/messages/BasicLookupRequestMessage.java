package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class BasicLookupRequestMessage extends Message implements Serializable{

    private static final long serialVersionUID = 40002L;

    public final String key;

    public BasicLookupRequestMessage(String key) {
        super(null);
        this.key = key;
    }
}
