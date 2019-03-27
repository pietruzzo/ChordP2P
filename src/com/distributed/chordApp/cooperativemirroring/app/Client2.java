package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class that is used for instantiate client objects for communicating with the host of
 * the cooperative mirroring application
 */
public class Client2 {
    //IP address of the client
    private String clientIP = null;
    //Port used by the client for the cooperative mirroring purpose
    private Integer clientPort = null;
    //Boolean flag used to decree if the client is executed in verbose mode
    private Boolean verbose = null;

    public Client2(String clientIP, Integer clientPort, Boolean verbose) {
        this.setClientIP(clientIP);
        this.setClientPort(clientPort);
        this.setVerbose(verbose);
    }

    /*Setter*/
    private void setClientIP(String clientIP){this.clientIP = clientIP; }
    private void setClientPort(Integer clientPort){this.clientPort = clientPort; }
    private void setVerbose(Boolean verbose){this.verbose = verbose; }

    /*Application methods*/

    /**
     * Method used for sending a request to a Server
     * @param serverIP
     * @param serverPort
     * @param requestMessage
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public ResponseMessage sendRequest(String serverIP, Integer serverPort, RequestMessage requestMessage) throws IOException, ClassNotFoundException {
        Socket server = new Socket(serverIP, serverPort);
        ResponseMessage response = null;
        ObjectInputStream inChannel = new ObjectInputStream(server.getInputStream());
        ObjectOutputStream outChannel = new ObjectOutputStream(server.getOutputStream());

        outChannel.writeObject(requestMessage);

        response = (ResponseMessage) inChannel.readObject();

        outChannel.close();
        inChannel.close();
        server.close();

        return response ;
    }

    /**
     * Method used for printing a request that the client will ask to a server of the
     * cooperative mirroring application
     * @param requestMessage
     */
    public void printRequest(RequestMessage requestMessage){
        String state = this.toString();

        state += "\nRequest = ";

        state += requestMessage.toString();

        System.out.println(state);

    }

    /**
     * Method used for printing the response to a specific request that a client has
     * send to a server of the cooperative mirroring application
     *
     * @param requestMessage
     * @param responseMessage
     */
    public void printResponse(RequestMessage requestMessage, ResponseMessage responseMessage){
        String state = this.toString();

        state += "\nResponse = ";

        state += "\nRequest: " + requestMessage.toString();
        state += "\nResponse: " + responseMessage.toString();

        System.out.println(state);

    }

    /*Getter*/
    public String getClientIP(){return this.clientIP; }
    public Integer getClientPort(){return this.clientPort; }
    public Boolean getVerbose(){return this.verbose; }

    @Override
    public String toString(){
        String state = "\n======{CLIENT}======\n";

        state += "\nIP Address: " + this.getClientIP() + " : " + this.getClientPort();

        if(this.getVerbose()) state += "\nVerbose mode";
        else state += "\nSilent mode";

        return state;
    }

    public static void main(String []args){
        
    }

}
