package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class LookupRequestMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40003L;

    public final String key;

    public LookupRequestMessage(String key) {
        super(null);
        this.key = key;}
}
