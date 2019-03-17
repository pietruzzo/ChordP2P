package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.codes.ResourceMessageType;

public class ResourceResponseMessage implements ResourceMessageInterface {
    //Current IP address of the sender of this message
    private String currentSenderIP;
    //Port address of the (current) sender of this message
    private Integer currentSenderPort;
    //Original IP address of the sender.
    private String originalSenderIP;
    //Original Port of the sender
    private Integer originalSenderPort;
    //ID of the resource involved in the message request
    private String resourceID;
    //(Optional) resource involved in the message request
    private Resource resource;
    //Type of operations related to the receivments of this message
    private ResourceMessageType messageType;
    //Is ACK boolean flag
    private Boolean isACK ;
    //States if the operation has been succesfully completed
    private Boolean success;

    /**
     * Constructor associated to :
     * -DEPOSIT_RESOURCE_MESSAGE
     * -STORE_RESOURCE_MESSAGE
     */
    public ResourceResponseMessage(String currentSenderIP, Integer currentSenderPort, String originalSenderIP, Integer originalSenderPort, String resourceID, ResourceMessageType messageType,Boolean isACK,Boolean result){
        this.setCurrentSenderIP(currentSenderIP);
        this.setCurrentSenderPort(currentSenderPort);
        this.setOriginalSenderIP(originalSenderIP);
        this.setOriginalSenderPort(originalSenderPort);
        this.setResourceID(resourceID);
        this.setResource(null);
        this.setMessageType(messageType);
        this.setIsACK(isACK);
        this.setSuccess(result);
    }

    /**
     * Consutructor associated to:
     * - RETRIEVE_RESOURCE_MESSAGE
     * - LOAD_RESOURCE_MESSAGE
     * @param resource
     * @param messageType
     */
    public ResourceResponseMessage(String currentSenderIP, Integer currentSenderPort, String originalSenderIP, Integer originalSenderPort, Resource resource, ResourceMessageType messageType,Boolean isACK,Boolean result){
        this.setCurrentSenderIP(currentSenderIP);
        this.setCurrentSenderPort(currentSenderPort);
        this.setOriginalSenderIP(originalSenderIP);
        this.setOriginalSenderPort(originalSenderPort);
        this.setResourceID(resource.getID());
        this.setResource(resource);
        this.setMessageType(messageType);
        this.setIsACK(isACK);
        this.setSuccess(result);
    }

    /*Setters*/
    private void setCurrentSenderIP(String currentSenderIP){this.currentSenderIP = currentSenderIP;}
    private void setCurrentSenderPort(Integer currentSenderPort){ this.currentSenderPort = currentSenderPort; }
    private void setOriginalSenderIP(String originalSenderIP){ this.originalSenderIP = originalSenderIP; }
    private void setOriginalSenderPort(Integer originalSenderPort){ this.originalSenderPort = originalSenderPort; }
    private void setResourceID(String resourceID){ this.resourceID = resourceID; }
    private void setResource(Resource resource){ this.resource = resource; }
    private void setMessageType(ResourceMessageType messageType){ this.messageType = messageType; }
    private void setIsACK(Boolean isACK){ this.isACK = isACK;}
    private void setSuccess(Boolean success){ this.success = success; }

    /*Getters*/
    public String getCurrentSenderIP(){ return this.currentSenderIP; }
    public Integer getCurrentSenderPort(){ return this.currentSenderPort; }
    public String getOriginalSenderIP(){ return this.originalSenderIP; }
    public Integer getOriginalSenderPort(){ return this.originalSenderPort; }
    public String getResourceID(){return this.resourceID; }
    public Resource getResource(){ return this.resource; }
    public ResourceMessageType getMessageType(){ return this.messageType; }
    public Boolean getIsACK(){ return this.isACK; }
    public Boolean getSuccess(){ return this.success; }

    @Override
    public String toString(){
        String stateString = "\n======{ Resource Response Method }======\n";

        stateString += "\nCurrent Sender IP: " + this.getCurrentSenderIP();
        stateString += "\nCurrent Sender Port: " + this.getCurrentSenderPort();
        stateString += "\nOriginal Sender IP: " + this.getOriginalSenderIP();
        stateString += "\nOriginal Sender Port: " + this.getOriginalSenderPort();
        stateString += "\nResource Request Message Type: " + this.getMessageType().getMessageTypeString();
        stateString += "\nResource ID: " + this.getResourceID();
        stateString += "\nIs ACK: " ;
        if(this.getIsACK()) stateString += "<YES>";
        else stateString += "<NO>";

        stateString += "\nOperation Succeed: " ;
        if(this.getSuccess()) stateString += "<YES>";
        else stateString += "<NO>";

        return  stateString;
    }
}
