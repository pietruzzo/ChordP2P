package com.distributed.chordApp.cooperativemirroring.core.model.backend;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceMessageInterface;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceRequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.codes.ResourceMessageType;
import com.distributed.chordApp.cooperativemirroring.core.utility.GeneralApplicationSettings;
import com.distributed.chordLib.Chord;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class that represents the basic structure associated to a thread that manage resources
 * for a host
 */

public class ClientHandlerThread implements Runnable {

    //Current Host IP
    private String currentHostIP;
    //Current Host Port
    private Integer currentHostPort;
    //Entry point for the chord network
    private Chord chordEntryPoint;
    //Client's socket
    private Socket clientSocket;


    public ClientHandlerThread(String currentHostIP,Integer currentHostPort, Socket clientSocket, Chord chordEntryPoint){
        this.setCurrentHostIP(currentHostIP);
        this.setCurrentHostPort(currentHostPort);
        this.setClientSocket(clientSocket);
        this.setChordEntryPoint(chordEntryPoint);
    }

    /*Setters*/
    private void setCurrentHostIP(String currentHostIP){ this.currentHostIP = currentHostIP; }
    private void setCurrentHostPort(Integer currentHostPort){ this.currentHostPort = currentHostPort; }
    private void setClientSocket(Socket clientSocket){ this.clientSocket = clientSocket; }
    private void setChordEntryPoint(Chord entryPoint){ this.chordEntryPoint = chordEntryPoint; }

    /*Application Methods*/
    /**
     * Method used for asking to the chord who is in charge of manage a resource
     * @param resourceID
     * @param basicLookup
     * @return
     */
    private String resourceLookup(String resourceID,Boolean basicLookup){
        String resourceManagerAddress = null;

        if(basicLookup.booleanValue()) resourceManagerAddress = this.chordEntryPoint.lookupKeyBasic(resourceID);
        else resourceManagerAddress = this.chordEntryPoint.lookupKey(resourceID);

        return resourceManagerAddress;
    }

    /**
     * Method used for sending a request to another host
     * @param originalRequestMessage
     * @param destinationIP
     */
    private void sendRequest(ResourceRequestMessage originalRequestMessage,String destinationIP,Integer destinationPort){
        Socket destinationSocket = null;
        ResourceRequestMessage newRequestMessage = null;
        ResourceResponseMessage responseMessage = null;
        ObjectInputStream inputChannel = null;
        ObjectOutputStream outputChannel = null;
        ResourceMessageType newMessageType = null;

        switch(originalRequestMessage.getMessageType())
        {
            case RETRIEVE_RESOURCE_MESSAGE:
            case BASIC_RETRIEVE_RESOURCE_MESSAGE:
                newMessageType = ResourceMessageType.LOAD_RESOURCE_MESSAGE;
                break;
            case DEPOSIT_RESOURCE_MESSAGE:
            case BASIC_DEPOSIT_RESOURCE_MESSAGE:
                newMessageType = ResourceMessageType.STORE_RESOURCE_MESSAGE;
                break;
            default:
                break;
        }

        try {
           destinationSocket = new Socket(destinationIP, destinationPort);
           destinationSocket.setSoTimeout(GeneralApplicationSettings.DEFAULT_CONNECTION_TIMEOUT_MS);

            newRequestMessage = new ResourceRequestMessage(this.getCurrentHostIP(), this.getCurrentHostPort(),
                    originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getOriginalSenderPort(),
                    originalRequestMessage.getResource(), newMessageType, originalRequestMessage.getWaitACK());

            outputChannel = new ObjectOutputStream(destinationSocket.getOutputStream());

            outputChannel.writeObject(newRequestMessage);
            if(originalRequestMessage.getWaitACK()){
                inputChannel = new ObjectInputStream(destinationSocket.getInputStream());

                responseMessage = (ResourceResponseMessage) inputChannel.readObject();

                inputChannel.close();
            }

            outputChannel.close();
            destinationSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used for sending an ack to a previous traversed node
     * @param originalRequestMessage
     * @param result
     */
    private void sendAck(ResourceRequestMessage originalRequestMessage,Boolean result){
        ResourceResponseMessage responseMessage = null;
        ObjectOutputStream outputChannel = null;

        try {

            outputChannel = new ObjectOutputStream(this.clientSocket.getOutputStream());

            responseMessage = new ResourceResponseMessage(this.getCurrentHostIP(), this.getCurrentHostPort(),
                    originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getOriginalSenderPort(),
                    originalRequestMessage.getResourceID(), originalRequestMessage.getMessageType(),
                    true, result);

            outputChannel.writeObject(responseMessage);

            outputChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used for sending a response to the original caller
     * @param originalRequestMessage
     */
    private void sendResponseToOriginalHost(ResourceRequestMessage originalRequestMessage,Boolean forewarded){
        Socket originalDestinationSocket = null;
        ResourceResponseMessage responseMessage = null;
        ObjectOutputStream originalOutputChannel = null;
        Resource resource = null;
        Boolean result = false ;

        try {
            originalDestinationSocket = new Socket(originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getOriginalSenderPort());
            originalOutputChannel = new ObjectOutputStream(originalDestinationSocket.getOutputStream());

            switch(originalRequestMessage.getMessageType())
            {
                case DEPOSIT_RESOURCE_MESSAGE:
                case BASIC_DEPOSIT_RESOURCE_MESSAGE:
                    responseMessage = new ResourceResponseMessage(this.getCurrentHostIP(), this.getCurrentHostPort(),
                            originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getCurrentSenderPort(),
                            originalRequestMessage.getResourceID(), originalRequestMessage.getMessageType(),
                            false, this.localResourceStore(originalRequestMessage.getResource()));
                    break;
                case RETRIEVE_RESOURCE_MESSAGE:
                case BASIC_RETRIEVE_RESOURCE_MESSAGE:
                    resource = this.localResourceLoad(originalRequestMessage.getResourceID());
                    responseMessage = new ResourceResponseMessage(this.getCurrentHostIP(), this.getCurrentHostPort(),
                            originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getCurrentSenderPort(),
                            resource, originalRequestMessage.getMessageType(),
                            false, (resource == null));
                    break;
                case STORE_RESOURCE_MESSAGE:
                    result = this.localResourceStore(originalRequestMessage.getResource());
                    responseMessage = new ResourceResponseMessage(this.getCurrentHostIP(), this.getCurrentHostPort(),
                            originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getOriginalSenderPort(),
                            originalRequestMessage.getResourceID(), ResourceMessageType.DEPOSIT_RESOURCE_MESSAGE,
                            false, result);
                    if(forewarded) if(originalRequestMessage.getWaitACK()) this.sendAck(originalRequestMessage, result);
                    break;
                case LOAD_RESOURCE_MESSAGE:
                    resource = this.localResourceLoad(originalRequestMessage.getResourceID());
                    responseMessage = new ResourceResponseMessage(this.getCurrentHostIP(), this.getCurrentHostPort(),
                            originalRequestMessage.getOriginalSenderIP(), originalRequestMessage.getOriginalSenderPort(),
                            resource, ResourceMessageType.RETRIEVE_RESOURCE_MESSAGE,
                            false, (resource == null));
                    if(forewarded) if(originalRequestMessage.getWaitACK()) this.sendAck(originalRequestMessage, (resource == null));
                    break;
                default:
                    break;
            }

            originalOutputChannel.writeObject(responseMessage);
            originalDestinationSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used for storing a resource locally
     * @return
     */
    private Boolean localResourceStore(Resource resource){
        DatabaseManager dbm = new DatabaseManager(resource);
        return dbm.storeResource();
    }

    /**
     * Method used for retreiving a resource stored locally
     * @param resourceID
     * @return
     */
    private Resource localResourceLoad(String resourceID){
      DatabaseManager dbm = new DatabaseManager(resourceID);
      return dbm.loadResource();
    }


    /*Getters*/
    public String getCurrentHostIP(){return this.currentHostIP; }
    public Integer getCurrentHostPort(){ return this.currentHostPort; }

    @Override
    public String toString(){
        String stateString = "\n======{ Client Handler Thread }======\n";

        stateString += "\nCurrent Host IP: " + this.getCurrentHostIP();
        stateString += "\nCurrent Host Port: " + this.getCurrentHostPort();

        return stateString;
    }

    @Override
    public void run(){

        ObjectInputStream inputChannel = null;
        ResourceRequestMessage requestMessage = null;
        String destinationAddress = null;
        Boolean forewarded = false;

        try{
            inputChannel = new ObjectInputStream(this.clientSocket.getInputStream());
            requestMessage = (ResourceRequestMessage) inputChannel.readObject();

            switch(requestMessage.getMessageType())
            {
                case BASIC_RETRIEVE_RESOURCE_MESSAGE:
                case BASIC_DEPOSIT_RESOURCE_MESSAGE:
                    destinationAddress = this.resourceLookup(requestMessage.getResourceID(), true);
                    forewarded = false;
                    break;

                case RETRIEVE_RESOURCE_MESSAGE:
                case DEPOSIT_RESOURCE_MESSAGE:
                    destinationAddress = this.resourceLookup(requestMessage.getResourceID(), false);
                    forewarded = false ;
                    break;
                default :
                    destinationAddress = requestMessage.getOriginalSenderIP();
                    forewarded = true;
                    break;

            }

            if(forewarded) this.sendResponseToOriginalHost(requestMessage, forewarded);
            else {
                if(destinationAddress.equals(this.getCurrentHostIP())) this.sendResponseToOriginalHost(requestMessage, forewarded);
                else this.sendRequest(requestMessage, destinationAddress, GeneralApplicationSettings.APPLICATION_PORT);
            }

            inputChannel.close();

        }catch(IOException ioe){

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally{

        }

        
    }
}
