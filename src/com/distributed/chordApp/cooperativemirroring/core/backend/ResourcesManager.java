package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;

import java.util.HashSet;

/**
 * Singleton class used to manage the resources of a specific Host
 *
 * @date 2019-03-27
 * @version 1.0
 *
 */
public class ResourcesManager
{
    private static ResourcesManager instance ;
    //Set of resources managed by a specific host
    private HashSet<Resource> resources = null;

    private ResourcesManager()
    {
        this.setResources(new HashSet<>());
    }

    /*Setter methods*/
    private void setResources(HashSet<Resource> resources) { this.resources = resources; }

    /*Application Methods*/

    /**
     * Method used for deposit a resource on a Host
     * @param resource
     * @return
     */
    public synchronized Boolean depositResource(Resource resource){
        return this.resources.add(resource);
    }

    /**
     * Method used for deposit multiple resources at a time on a specific
     * host.
     *
     * @param resources
     * @return
     */
    public synchronized Boolean depositResources(HashSet<Resource> resources){
        Boolean result = true ;

        for(Resource resource : resources){
            /*
             * For every resource passed as parameter I add it to the current resource container
             * and perform an AND in order to know if all the resources has been successfully deposited on this host
             * or not (true -> all the resources has been successfully hadded, false -> some resource could not have
             * stored)
             */
            result = result && this.resources.add(resource);

            if(!result) return false ;
        }

        return true;
    }

    /**
     * Method used for retriving a resource from a host
     * @param resourceID
     * @return
     */
    public synchronized Resource retrieveResource(String resourceID){
        Resource result = null;

        for(Resource resource : this.resources){
            if(resource.getResourceID() == resourceID){
                result = resource;
                break;
            }
        }

        return result;
    }

    /**
     * Method used for deleting a resource contained in the Host
     * @param resourceID
     * @return
     */
    public synchronized Boolean removeResource(String resourceID){
        Resource result = null;

        result = this.retrieveResource(resourceID);

        if(result != null) return this.resources.remove(result);

        return false;
    }

    /*Getter methods*/
    public synchronized HashSet<Resource> getResources(){ return this.resources; }

    public static synchronized ResourcesManager getInstance(){
        if(instance == null) instance = new ResourcesManager();

        return instance;
    }

    @Override
    public String toString() {
        String state = "\n======{ RESOURCES MANAGER }======\n";

        for(Resource r : this.resources) state += r.toString();

        return state;
    }

}
