package com.distributed.chordApp.cooperativemirroring.interfaces;

import java.io.Serializable;

/**
 * Interface that defines the public methods available to used the application
 */

public interface ResourceInterface extends Serializable {
    /**
     * Method used for getting the ID associated to a resource
     * @return
     */
    public String getID();

    /**
     * Method used for getting the version associated to a resource
     * @return
     */
    public Integer getVersion();

    /**
     * Method for getting the state associated to a resource
     * @return
     */
    public String toString();
}
