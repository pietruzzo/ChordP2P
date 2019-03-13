package com.distributed.chordApp.cooperativemirroring.core.model;

import com.distributed.chordLib.Chord;

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

    /**
     * Method used for store a resource on a host identified by the Chord algorithm
     * @param resource
     * @return
     */
    public Boolean storeResource(Resource resource){
        return false;

    }

    /**
     * Method used for retrieve a resource from a host identified by the Chord algorithm
     * @param resource
     * @return
     */
    public Resource retrieveResource(String resource){
        return null;
    }

    @Override
    public void run() {

    }
}
