package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class BasicLookupRequest implements Serializable {

    private static final long serialVersionUID = 40002L;

    public final String key;

    public BasicLookupRequest(String key) {this.key = key;}
}
