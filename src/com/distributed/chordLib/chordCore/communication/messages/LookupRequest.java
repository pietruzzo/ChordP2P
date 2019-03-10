package com.distributed.chordLib.chordCore.communication.messages;

public class LookupRequest {

    private static final long serialVersionUID = 40003L;

    public final String key;

    public LookupRequest(String key) {this.key = key;}
}
