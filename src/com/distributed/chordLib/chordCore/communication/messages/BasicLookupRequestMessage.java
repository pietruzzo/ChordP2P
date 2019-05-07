package com.distributed.chordLib.chordCore.communication.messages;

import com.distributed.chordLib.chordCore.HashFunction;

import java.io.Serializable;

import static com.distributed.chordLib.chordCore.HashFunction.*;

public class BasicLookupRequestMessage extends Message implements Serializable{

    private static final long serialVersionUID = 40002L;

    public final Hash key;

    public BasicLookupRequestMessage(Hash key) {
        super(null);
        this.key = key;
    }
}
