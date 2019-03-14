package com.distributed.chordApp.cooperativemirroring.core.model.backend;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;
import com.distributed.chordLib.Chord;

import java.io.IOException;

/**
 * Class that represents the basic structure associated to a thread that manage resources
 * for a host
 */

public class ResourceManager implements Runnable {
    //Entry point for the chord network
    private Chord chordEntryPoint;


    public ResourceManager(Chord chordEntryPoint){
        this.setChordEntryPoint(chordEntryPoint);
    }

    /*Setters*/
    private void setChordEntryPoint(Chord entryPoint){ this.chordEntryPoint = chordEntryPoint; }

    //Application methods

    /**
     * Method used for asking to the chord who is in charge of manage a resource
     * @param resourceID
     * @param basicLookup
     * @return
     */
    private String resourceLookup(String resourceID,Boolean basicLookup){
        String resourceManagerAddress = null;

        if(basicLookup.booleanValue()) resourceManagerAddress = this.chordEntryPoint.lookupKeyBasic(resourceID);
        else resourceManagerAddress = this.chordEntryPoint.lookupKey(resourceID);

        return resourceManagerAddress;
    }

    /**
     * Method used for store a resource on a host identified by the Chord algorithm
     * @param resource
     * @return
     */
    public Boolean storeResource(Resource resource,Boolean basicLookup) throws IOException, ClassNotFoundException {
        String resourceManagerAddress = null;
        CommunicationManager cm = null;
        Boolean result = false ;

        resourceManagerAddress = this.resourceLookup(resource.getID(), basicLookup);
        if(resourceManagerAddress != null){
            //Da modificare
            cm = new CommunicationManager(resourceManagerAddress, 11);
            result = cm.sendResource(resource);
        }

        return false;
    }

    /**
     * Method used for retrieve a resource from a host identified by the Chord algorithm
     * @param resourceID, basicLookup
     * @return
     */
    public Resource loadResource(String resourceID, Boolean basicLookup) throws IOException, ClassNotFoundException {
        String resourceManagerAddress = null;
        CommunicationManager cm = null;
        Resource result = null;

        resourceManagerAddress = this.resourceLookup(resourceID, basicLookup);
        if(resourceManagerAddress != null) {
            //Da modificare
            cm = new CommunicationManager(resourceManagerAddress, 11);
            result = cm.retrieveResource(resourceID);
        }

        return result;
    }

    @Override
    public void run() {

    }
}
