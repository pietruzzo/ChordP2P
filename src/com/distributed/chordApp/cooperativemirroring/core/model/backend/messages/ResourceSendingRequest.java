package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;

import java.io.Serializable;

/**
 * Message used for require sending a resource to another Host
 */

public class ResourceSendingRequest implements Serializable {
    private Resource resource;

    public ResourceSendingRequest(Resource resource){ this.setResource(resource); }

    /*Setters*/
    private void setResource(Resource resource){ this.resource = resource; }

    /*Getters*/
    public Resource getResource(){ return this.resource; }

    @Override
    public String toString(){
        String stateString = "";

        stateString += "\nResource ID: " + this.getResource().getID();

        return stateString;
    }
}
