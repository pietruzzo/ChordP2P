package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;

import java.io.*;
import java.net.Socket;

/**
 * Class that is used for instantiate client objects for communicating with the host of
 * the cooperative mirroring application
 */
public class Client {
    //IP address of the client
    private String clientIP = null;
    //Port used by the client for the cooperative mirroring purpose
    private Integer clientPort = null;
    //Boolean flag used to decree if the client is executed in verbose mode
    private Boolean verbose = null;

    public Client(String clientIP, Integer clientPort, Boolean verbose) {
        this.setClientIP(clientIP);
        this.setClientPort(clientPort);
        this.setVerbose(verbose);
        this.clientConsole();
    }

    /*Setter methods*/
    private void setClientIP(String clientIP){this.clientIP = clientIP; }
    private void setClientPort(Integer clientPort){this.clientPort = clientPort; }
    private void setVerbose(Boolean verbose){this.verbose = verbose; }

    /*Application methods*/

    private String clientInfoString(String infoMessage)
    {
        String infoString = "[Client\\\\" + this.getClientIP() + ":" + this.getClientPort() + ">";

        infoString += infoMessage;

        return infoString;
    }

    /*
     * Method used for allowing the client to perform some operations on a server
     */
    private void clientConsole()
    {
        Boolean goAhead = true ;
        Integer choice = -1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String serverIP = "192.168.137.199";
        Integer serverPort = 9999;

        String resourceID = null;
        RequestMessage request = null;
        ResponseMessage response = null;

        do {
            System.out.println("\n======{CLIENT\\\\" + this.getClientIP() + ":" + this.getClientPort() + " CONSOLE}======\n");
            System.out.println("1)Deposit a resource");
            System.out.println("2)Retrive a resource");
            System.out.println("0)Exit");
            System.out.print("[Choice> ");

            try {
                choice = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println(this.clientInfoString("Invalid input choice type, retry"));
                choice = -1;
                goAhead = true;
            }

            switch(choice)
            {
                case -1:
                    break;
                case 0:
                    System.out.println(this.clientInfoString("Terminating the current session"));
                    goAhead = false ;
                    break;
                case 1:
                    System.out.println(this.clientInfoString("Deposit resource "));
                    System.out.print(this.clientInfoString("Insert resource id: "));
                    try {
                        resourceID = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    request = this.buildRequest(resourceID, true);
                    try {
                        response = this.sendRequest(serverIP, serverPort, request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    this.printResponse(request, response);
                    break;

                case 2:
                    System.out.println(this.clientInfoString("Retrive resource "));
                    System.out.print(this.clientInfoString("Insert resource id: "));
                    try {
                        resourceID = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    request = this.buildRequest(resourceID, false);
                    try {
                        response = this.sendRequest(serverIP, serverPort, request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    this.printResponse(request, response);
                    break;
                default :
                    break;
            }

        }while(goAhead);

        System.out.println(this.clientInfoString("Exiting from the client, bye"));

    }

    /*
     * Method used for building a new request for the server
     */
    private RequestMessage buildRequest(String resourceID,Boolean depositResource)
    {
        if(this.verbose) System.out.println(this.clientInfoString("creating the request ..."));
        RequestMessage request = null;
        Resource resource = null;

        if(depositResource)
        {
            resource = new Resource( resourceID );

            request = new RequestMessage(
                    this.getClientIP(),
                    this.getClientPort(),
                    resource,
                    false,
                    false
            );
        }
        else
        {
            request = new RequestMessage(
                    this.getClientIP(),
                    this.getClientPort(),
                    resourceID,
                    false,
                    false
            );
        }

        if(this.verbose) System.out.println(this.clientInfoString("request created"));

        return request;
    }

    /*
     * Method used for sending a request to a Server
     */
    private ResponseMessage sendRequest(String serverIP, Integer serverPort, RequestMessage requestMessage) throws IOException, ClassNotFoundException
    {
        if(this.verbose) System.out.println(this.clientInfoString("creating a connection with the server ..."));
        Socket server = new Socket(serverIP, serverPort);
        ResponseMessage response = null;
        ObjectOutputStream outChannel = new ObjectOutputStream(server.getOutputStream());
        ObjectInputStream inChannel = new ObjectInputStream(server.getInputStream());
        if(this.verbose) System.out.println(this.clientInfoString("connection with the server created, sending the request ..."));


        outChannel.writeObject(requestMessage);
        outChannel.flush();

        if(this.verbose) System.out.println(this.clientInfoString("request sended , waiting for a response ..."));

        response = (ResponseMessage) inChannel.readObject();

        if(this.verbose) System.out.println(this.clientInfoString("response arrived"));

        inChannel.close();
        outChannel.close();
        server.close();

        return response ;
    }

    /*
     * Method used for printing a request that the client will ask to a server of the
     * cooperative mirroring application
     */
    private void printRequest(RequestMessage requestMessage){
        String state = this.toString();

        state += "\nRequest = ";

        state += requestMessage.toString();

        System.out.println(state);

    }

    /*
     * Method used for printing the response to a specific request that a client has
     * send to a server of the cooperative mirroring application
     */
    private void printResponse(RequestMessage requestMessage, ResponseMessage responseMessage){
        String state = this.toString();

        state += "\nResponse = ";

        state += "\nRequest: " + requestMessage.toString();
        state += "\nResponse: " + responseMessage.toString();

        System.out.println(state);

    }

    /*Getter methods*/
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
        Client c = new Client("127.0.0.1", 7777, true);
    }

}
