package com.distributed.chordLib.chordCore.communication.messages;

import jdk.internal.jline.internal.Nullable;

import java.io.Serializable;

/**
 * Abstract class that handle requests id
 */
public class Message implements Serializable {

    private static int idCounter = 0;

    private Integer id = null;

    /**
     * Assign an id to a request
     * if null, an id will be automatically assigned
     * @param @Nullable id
     */
    Message(@Nullable Integer id){
        if (id != null) this.id = id;
        else assignNewId();
    }

    /**
     * Assign a new id to request
     * @return id
     */
    int assignNewId(){
        idCounter = idCounter + 1;
        id = idCounter;
        return idCounter;
    }

    /**
     * id getter
     */
    public int getId(){
        return id;
    }
}
