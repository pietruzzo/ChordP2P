package com.distributed.chordApp.cooperativemirroring.core.backend.messages;

import com.distributed.chordApp.cooperativemirroring.core.Resource;

import java.io.Serializable;

/**
 * Class used to describe a Request Message coming to an host
 */
public class RequestMessage implements Serializable {
    private static final long serialVersionUID = 50001L;
    //Original client IP address
    private String originalSenderIP = null;
    //Original client port
    private Integer originalSenderPort = null;
    /*
     * Boolean flag used to state if the request is about depositing a resource (true) or
     * withdraw a resource (false)
     */
    private Boolean depositResource = null;
    //Resource ID , used for looking for a resource
    private String resourceID = null;
    //Resource , used to send a resource in order to store it
    private Resource resource = null ;
    //Boolean flag used to decree if the previous node is waiting for an ACK
    private Boolean ackRequested = null;
    //Boolean value used to state if this message has been forwarded
    private Boolean forewarded = null;
    //Boolean flag used to state if the request was sended by a host
    private Boolean hostDepositRequest = false;

    //Constructor called when a retrieve resource request has to be sent
    public RequestMessage(String originalSenderIP,Integer originalSenderPort,String resourceID, Boolean ackRequested,Boolean forewarded){
        this.setOriginalSenderIP(originalSenderIP);
        this.setOriginalSenderPort(originalSenderPort);
        this.setDepositResource(false);
        this.setResourceID(resourceID);
        this.setResource(null);
        this.setAckRequested(ackRequested);
        this.setForewarded(forewarded);

    }

    //Constructor called when a deposit resource request has to be sent
    public RequestMessage(String originalSenderIP,Integer originalSenderPort,Resource resource,Boolean ackRequested,Boolean forewarded){
        this.setOriginalSenderIP(originalSenderIP);
        this.setOriginalSenderPort(originalSenderPort);
        this.setDepositResource(true);
        this.setResourceID(resource.getResourceID());
        this.setResource(resource);
        this.setAckRequested(ackRequested);
        this.setForewarded(forewarded);
    }

    /*Setter methods*/
    private void setOriginalSenderIP(String originalSenderIP){this.originalSenderIP = originalSenderIP; }
    private void setOriginalSenderPort(Integer originalSenderPort){this.originalSenderPort = originalSenderPort; }
    private void setDepositResource(Boolean depositResource){this.depositResource = depositResource; }
    private void setResourceID(String resourceID){this.resourceID = resourceID; }
    private void setResource(Resource resource){this.resource = resource; }
    private void setAckRequested(Boolean ackRequested){ this.ackRequested = ackRequested; }
    private void setForewarded(Boolean forewarded){this.forewarded = forewarded; }

    public void setHostDepositRequest(boolean hostDepositRequest){this.hostDepositRequest = hostDepositRequest; }

    /*Getter methods*/
    public String getOriginalSenderIP(){return this.originalSenderIP; }
    public Integer getOriginalSenderPort(){return this.originalSenderPort; }
    public Boolean getDepositResource(){ return this.depositResource; }
    public String getResourceID(){return this.resourceID; }
    public Resource getResource(){ return this.resource; }
    public Boolean getAckRequested(){ return this.ackRequested; }
    public Boolean getForewarded(){return this.forewarded; }
    public Boolean getHostDepositRequest(){return this.hostDepositRequest; }

    @Override
    public String toString(){
        String state = "\n======{REQUEST MESSAGE}======\n";

        state += "\nOriginal sender IP: " + this.getOriginalSenderIP();
        state += "\nOriginal sender port: " + this.getOriginalSenderPort();
        if(this.getDepositResource()) state += "\nDeposit resource";
        else state += "\nRetrive resource";
        state += "\nResource ID: " + this.getResourceID();
        if(this.getAckRequested()) state += "\nACK requested";
        else state += "\nACK not requested";
        if(this.getForewarded()) state += "\nRequest forewarded";
        else state += "\nDirect request";
        if(this.getHostDepositRequest()) state += "\n<Host Deposit Request>";

        return state;
    }

}
