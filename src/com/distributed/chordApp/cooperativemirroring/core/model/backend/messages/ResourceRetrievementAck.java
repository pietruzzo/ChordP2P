package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;

import java.io.Serializable;

/**
 * Message used for states if the Host is able to retrieve the resource
 */

public class ResourceRetrievementAck implements Serializable {
    private Boolean accepted;
    private Resource resource;

    public ResourceRetrievementAck(Boolean accepted, Resource resource){
        this.setAccepted(accepted);

    }

    /*Setters*/
    private void setAccepted(Boolean accepted){ this.accepted = accepted; }
    private void setResource(Resource resource) { this.resource = resource; }

    /*Getters*/
    public Boolean isAccepted(){ return this.accepted; }
    public Resource getResource(){ return this.resource; }

    @Override
    public String toString() {
        String stateString = "";

        stateString += "\nAccepted: " + this.isAccepted().booleanValue();
        if(this.isAccepted()) stateString += "\nResource: " + this.getResource().getID();

        return stateString;
    }
}
