package com.distributed.chordApp.cooperativemirroring.core.model;

import com.distributed.chordApp.cooperativemirroring.interfaces.ResourceInterface;

/**
 * Class the represent the basic structure for a resource to be managed by an host
 */

public class Resource implements ResourceInterface {
    private static final long VERSION = 10001L ;
    /*Unique ID associated to a specific resource*/
    private String ID ;

    public Resource(String ID){
        this.setID(ID);
    }

    /*Setters*/
    private void setID(String ID){ this.ID = ID; }

    /*Getters*/

    /**
     * Method that return the reosurce ID associated to the resource
     * @return String
     */
    public String getID(){ return this.ID; }


    @Override
    public String toString() {
       String stateString = "";

       stateString += "\nResource ID: " + this.getID();

       return stateString;
    }
}
