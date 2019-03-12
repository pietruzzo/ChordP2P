package com.distributed.chordLib.chordCore.communication.messages;

import java.io.Serializable;

public class BasicLookupRequest extends ReqResp implements Serializable{

    private static final long serialVersionUID = 40002L;

    public final String key;

    public BasicLookupRequest(String key) {
        super(null);
        this.key = key;
    }
}
