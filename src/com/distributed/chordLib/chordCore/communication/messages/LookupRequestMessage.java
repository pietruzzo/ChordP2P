package com.distributed.chordLib.chordCore.communication.messages;

public class LookupRequestMessage extends ReqResp {

    private static final long serialVersionUID = 40003L;

    public final String key;

    public LookupRequestMessage(String key) {
        super(null);
        this.key = key;}
}
