package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.HashFunction;

import java.io.Serializable;

import static com.distributed.chordLib.chordCore.HashFunction.*;

public class LookupRequestMessage extends Message implements Serializable {

    private static final long serialVersionUID = 40003L;

    public final Hash key;

    public LookupRequestMessage(Hash key) {
        super(null);
        this.key = key;}
}
