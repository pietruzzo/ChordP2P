package com.distributed.chordApp.cooperativemirroring.core.model;

import com.distributed.chordApp.cooperativemirroring.interfaces.ResourceInterface;

/**
 * Class the represent the basic structure for a resource to be managed by an host
 */

public class Resource implements ResourceInterface {
    private static final long VERSION = 10001L ;
    /*Unique ID associated to a specific resource*/
    private String ID ;
    /*Version of the project to be released*/
    private Integer version;

    public Resource(String ID, Integer version){
        this.setID(ID);
        this.setVersion(version);
    }

    /*Setters*/
    private void setID(String ID){ this.ID = ID; }
    private void setVersion(Integer version){ this.version = version; }

    /*Getters*/

    /**
     * Method that return the reosurce ID associated to the resource
     * @return String
     */
    public String getID(){ return this.ID; }

    /**
     * Method that returns the version associated to a resource
     * @return Integer
     */
    public Integer getVersion(){ return this.version; }

    @Override
    public String toString() {
       String stateString = "";

       stateString += "\nResource ID: " + this.getID();
       stateString += "\nResource Version: " + this.getVersion();

       return stateString;
    }
}
