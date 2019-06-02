package com.distributed.chordApp.cooperativemirroring.common;

import java.io.Serializable;

/**
 * Class that represents a resource used in the cooperative mirroring application
 *
 * @date 2019-03-27
 * @version 1.0
 */
public class Resource implements Serializable {
    //Unique ID associated to a specific resource
    private String resourceID = null;

    public Resource(String resourceID){ this.setResourceID(resourceID);}

    /*Setter methods*/
    private void setResourceID(String resourceID){this.resourceID = resourceID; }

    /*Getter methods*/
    public String getResourceID(){return this.resourceID; }

    @Override
    public String toString(){
        String state = "\n======{ RESOURCE }======\n";

        state += "\nID: " + this.getResourceID();

        return state;
    }


}
