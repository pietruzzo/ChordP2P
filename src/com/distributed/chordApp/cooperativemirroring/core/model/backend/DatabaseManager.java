package com.distributed.chordApp.cooperativemirroring.core.model.backend;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;
import com.distributed.chordApp.cooperativemirroring.core.utility.GeneralApplicationSettings;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DatabaseManager{
    //Name of the resource to be saved/loaded
    private String resourceID;
    //Path of the resource to be saved/loaded
    private String resourcePath;
    //Boolean flag to state if the thread has to store a resource
    private Boolean storeResource;
    //Boolean flag used to state if the database thread has to retrieve a resource
    private Boolean loadResource;

    /**
     * Constructor invoked when we have to load a resource
     * @param resourceID
     */
    public DatabaseManager(String resourceID){
        this.setResourceID(resourceID);
        this.setResourcePath();
        this.setLoadResource(true);
        this.setStoreResource(false);
    }

    /**
     * Constructor invoked when we have to store a resource
     * @param resource
     */
    public DatabaseManager(Resource resource){
        this.setResourceID(resource.getID());
        this.setResourcePath();
        this.setLoadResource(false);
        this.setStoreResource(true);
    }

    /*Setters*/
    private void setResourceID(String resourceID){ this.resourceID = resourceID; }
    private void setStoreResource(Boolean storeResource){ this.storeResource = storeResource; }
    private void setLoadResource(Boolean loadResource){ this.loadResource = loadResource; }
    private void setResourcePath(){
        this.resourcePath = GeneralApplicationSettings.DATABASE_PATH;
        this.resourcePath += "/";
        this.resourcePath += this.getResourceID();
        this.resourcePath += GeneralApplicationSettings.DATABASE_RECORD_EXTENSION;
    }

    /*Application Methods*/

    /**
     * Method used for storing a new resource
     */
    public Boolean storeResource(){
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(this.getResourcePath());

            out.write(0);

            out.close();

        } catch (IOException e) {
            return false ;
        }

        return true;
    }

    /**
     * Method used for loading an existing resource
     */
    public Resource loadResource(){
        FileInputStream in = null;
        Resource r = null;

        try {
            in = new FileInputStream(this.getResourcePath());

            r = new Resource(resourceID);

            in.close();
        } catch (IOException e) {
            return null;
        }

        return r;
    }

    /*Getters*/
    public String getResourceID(){return this.resourceID; }
    public Boolean getStoreResource(){ return this.storeResource; }
    public Boolean getLoadResource(){ return this.loadResource; }
    public String getResourcePath(){ return this.resourcePath; }

    @Override
    public String toString(){
        String stateString = "\n======{ Database Manager Thread }======\n";

        stateString += "\nResource ID: " + this.getResourceID();
        stateString += "\nResource Path: " + this.getResourcePath();
        stateString += "\nLoad Resource: ";
        if(this.getLoadResource()) stateString += "<YES>";
        else stateString += "<NO>";
        stateString += "\nStore Resource: ";
        if(this.getStoreResource()) stateString += "<YES>";
        else stateString += "<NO>";

        return stateString;
    }

}
