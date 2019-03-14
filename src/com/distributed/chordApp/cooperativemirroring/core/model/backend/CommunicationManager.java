package com.distributed.chordApp.cooperativemirroring.core.model.backend;

//Standard Libraries
import com.distributed.chordApp.cooperativemirroring.core.model.Resource;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceRetrievementAck;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceRetrievementRequest;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceSendingAck;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.ResourceSendingRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class used for instantiate objects that are responsable for the communication among different Host
 * in a multithreaded fashion
 */

public class CommunicationManager implements Runnable{
    private String destinationIP;
    private Integer destinationPort;
    private Socket socket;

    CommunicationManager(String IPaddress, Integer port) throws IOException {
        this.setDestinationIP(IPaddress);
        this.setDestinationPort(port);
        this.setSocket();
    }

    /*Setters*/
    private void setDestinationIP(String destinationIP){ this.destinationIP = destinationIP; }
    private void setDestinationPort(Integer destinationPort){ this.destinationPort = destinationPort; }
    private void setSocket() throws IOException {
        this.socket = new Socket(this.getDestinationIP(), this.getDestinationPort().intValue());
    }

    /*Application methods*/

    /**
     * Method used for sending a resource to another host
     *
     * @param resource
     * @throws IOException
     */
    public Boolean sendResource(Resource resource) throws IOException, ClassNotFoundException {
        ObjectOutputStream outChannel = new ObjectOutputStream(this.socket.getOutputStream());
        ObjectInputStream inChannel = new ObjectInputStream(this.socket.getInputStream());
        Boolean result = false ;

        outChannel.writeObject(new ResourceSendingRequest(resource));
        if(((ResourceSendingAck) inChannel.readObject()).isAccepted()) result = true;
        else result = false;

        outChannel.close();
        inChannel.close();

        return result;
    }

    /**
     * Method used for retrieving a resource from another host
     * @param resourceID
     */
    public Resource retrieveResource(String resourceID) throws IOException, ClassNotFoundException {
        ObjectOutputStream outChannel = new ObjectOutputStream(this.socket.getOutputStream());
        ObjectInputStream inChannel = new ObjectInputStream(this.socket.getInputStream());
        Resource resource = null;

        outChannel.writeObject(new ResourceRetrievementRequest(resourceID));
        ResourceRetrievementAck rra = (ResourceRetrievementAck) inChannel.readObject();
        if(rra.isAccepted()) resource = rra.getResource();

        outChannel.close();
        inChannel.close();

        return resource;
    }

    /*Getters*/
    public String getDestinationIP(){ return this.destinationIP; }
    public Integer getDestinationPort(){ return this.destinationPort; }

    @Override
    public String toString(){
        String stateString = "" ;

        stateString += "\nDestination IP: " + this.getDestinationIP();
        stateString += "\nDestination Port: " + this.getDestinationPort();

        return stateString;
    }

    @Override
    public void run() {

    }
}
