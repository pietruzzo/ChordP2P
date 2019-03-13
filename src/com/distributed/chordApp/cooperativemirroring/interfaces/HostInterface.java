package com.distributed.chordApp.cooperativemirroring.interfaces;

//Standard libraries
import java.util.HashSet;

//Application libraries
import com.distributed.chordApp.cooperativemirroring.core.model.Resource;

/**
 * Interface that defines the public methods available to used the application
 */
public interface HostInterface {

    /**
     * Method used to upload a resource to a host of the system
     * @param resource
     * @return Boolean as the result of the operation
     */
    public Boolean uploadResource(Resource resource);

    /**
     * Method used for download a resource from a host of the system
     * @param resourceID
     * @return Boolean as the result of the operation
     */
    public Boolean downloadResource(String resourceID);

    /**
     * IP Address associated to the current Host
     * @return String
     */
    public String getIP();

    /**
     * Port associated to the current Host for the current application
     * @return
     */
    public Integer getPort();

    /**
     * Getters for the set of resources mantained by a Host
     * @return HashSet<Resource>
     */
    public HashSet<Resource> getResources();

    /**
     * Method that returns the state of the current host
     * @return String
     */
    public String toString();
}
