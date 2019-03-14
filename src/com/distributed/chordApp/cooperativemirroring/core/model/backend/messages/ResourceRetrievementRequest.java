package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages;

import java.io.Serializable;

/**
 * Message used for requireing a resource from another Host
 */

public class ResourceRetrievementRequest implements Serializable {
    private String resourceID;

    public ResourceRetrievementRequest(String resourceID){ this.setResourceID(resourceID); }

    /*Setters*/
    private void setResourceID(String resourceID){ this.resourceID = resourceID; }

    /*Getters*/
    public String getResourceID(){ return this.resourceID; }

    @Override
    public String toString(){
        String stateString = "";

        stateString += "\nResource ID: " + this.getResourceID();

        return stateString;
    }
}
