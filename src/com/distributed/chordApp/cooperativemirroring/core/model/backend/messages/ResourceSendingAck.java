package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages;

import java.io.Serializable;

/**
 * Message used for states if the Host is able to store the resource
 */

public class ResourceSendingAck implements Serializable {
    private Boolean accepted;

    public ResourceSendingAck(Boolean accepted){ this.setAccepted(accepted); }

    /*Setters*/
    private void setAccepted(Boolean accepted){ this.accepted = accepted; }

    /*Getters*/
    public Boolean isAccepted(){ return this.accepted; }

    @Override
    public String toString() {
        String stateString = "";

        stateString += "\nAccepted: " + this.isAccepted().booleanValue();

        return stateString;
    }
}
