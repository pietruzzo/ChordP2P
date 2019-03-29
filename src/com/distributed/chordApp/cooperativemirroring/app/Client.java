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

    /*Setter*/
    private void setClientIP(String clientIP){this.clientIP = clientIP; }
    private void setClientPort(Integer clientPort){this.clientPort = clientPort; }
    private void setVerbose(Boolean verbose){this.verbose = verbose; }

    /*Application methods*/

    private void clientConsole()
    {
        Boolean goAhead = true ;
        Integer choice = -1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String serverIP = "127.0.0.1";
        Integer serverPort = 6666;

        String resourceID = null;
        RequestMessage request = null;
        ResponseMessage response = null;

        do {
            System.out.println("\n======{CLIENT CONSOLE}======\n");
            System.out.println("1)Deposit a resource");
            System.out.println("2)Retrive a resource");
            System.out.println("0)Exit");
            System.out.print("[Choice> ");

            try {
                choice = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println("[Client> Invalid input choice type, retry");
                choice = -1;
                goAhead = true;
            }

            switch(choice)
            {
                case -1:
                    break;
                case 0:
                    System.out.println("\n[Client> terminating the current session");
                    goAhead = false ;
                    break;
                case 1:
                    System.out.println("\nDeposit resource");
                    System.out.print("Insert resource id: ");
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
                    System.out.println("\nRetrive resource");
                    System.out.print("Insert resource id: ");
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

        System.out.print("\nExiting from the client , bye\n");
    }

    /**
     * Method used for building a new request for the server
     * @param resourceID
     * @param depositResource
     * @return
     */
    private RequestMessage buildRequest(String resourceID,Boolean depositResource){
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

        return request;
    }

    /**
     * Method used for sending a request to a Server
     * @param serverIP
     * @param serverPort
     * @param requestMessage
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ResponseMessage sendRequest(String serverIP, Integer serverPort, RequestMessage requestMessage) throws IOException, ClassNotFoundException {
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
    private void printRequest(RequestMessage requestMessage){
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
    private void printResponse(RequestMessage requestMessage, ResponseMessage responseMessage){
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
        Client c = new Client("127.0.0.1", 4567, true);
    }

}
