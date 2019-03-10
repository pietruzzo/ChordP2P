package com.distributed.chordLib.chordCore.communication.messages;

public class LookupRequestMessage {

    private static final long serialVersionUID = 40003L;

    public final String key;

    public LookupRequestMessage(String key) {this.key = key;}
}
