package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.codes;

import java.io.Serializable;

/**
 * Enum used for defining the type of message send to an host
 */

public enum ResourceMessageType implements Serializable {
    /**
     * RETRIEVE_RESOURCE_MESSAGE : Another Host has asked to us to find and give it back a resource on the chord network
     *                          (usually this will be used by final user's hosts that has just received this command
     *                          but hasn't do the lookup yet.).
     */
    RETRIEVE_RESOURCE_MESSAGE("<retrieve resource message>"),
    /**
     * BASIC_RETRIEVE_RESOURCE_MESSAGE : Another Host has asked to us to find and give it back a resource on the chord network in the basic way
     *                          (usually this will be used by final user's hosts that has just received this command
     *                          but hasn't do the lookup yet.).
     */
    BASIC_RETRIEVE_RESOURCE_MESSAGE("<basic retrieve resource message>"),
    /**
     * LOAD_RESOURCE_MESSAGE : Another host has asked us to load  a resource that we have hold
     *                         (usually, this code will be used by other hosts after that the lookup has defined us as
     *                         the holders of a specific resource).
     */
    LOAD_RESOURCE_MESSAGE("<load resource message>"),
    /**
     * STORE_RESOURCE_MESSAGE : Another host has asked us to store a resource that we should manage
     *                          (usually, this code will be used by other hosts after that the lookup has defined us as
     *                          the holders of a specific resource).
     */
    STORE_RESOURCE_MESSAGE("<store resource message>"),
    /**
     * DEPOSIT_RESOURCE_MESSAGE : Another host has asked us to deposit a given resource on a host of the chord network
     *                            (usally , this code will be used by final's users hosts that has just received this
     *                            command but hasn't do the lookup yet).
     */
    DEPOSIT_RESOURCE_MESSAGE("<deposit resource message>"),
    /**
     * BASIC_DEPOSIT_RESOURCE_MESSAGE : Another host has asked us to deposit a given resource on a host of the chord network in the basic way
     *                            (usally , this code will be used by final's users hosts that has just received this
     *                            command but hasn't do the lookup yet).
     */
    BASIC_DEPOSIT_RESOURCE_MESSAGE("<basic deposit resource message>");

    private String messageTypeString ;

    ResourceMessageType(String messageTypeString){ this.setMessageTypeString(messageTypeString); }

    private void setMessageTypeString(String messageTypeStringString){ this.messageTypeString = messageTypeStringString; }
    public String getMessageTypeString(){ return this.messageTypeString; }

    @Override
    public String toString(){
        String stateString = "";

        stateString += "\nMessage Type: " + this.getMessageTypeString();

        return stateString;
    }

}
