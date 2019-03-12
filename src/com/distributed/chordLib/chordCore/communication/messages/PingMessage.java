package com.distributed.chordLib.chordCore.communication.messages;

public class PingMessage extends ReqResp {

    private static final long serialVersionUID = 40006L;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     *
     * @param id
     */
    PingMessage(Integer id) {
        super(id);
    }
}
