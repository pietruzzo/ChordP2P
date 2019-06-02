package com.distributed.chordApp.cooperativemirroring.client.view;

import com.distributed.chordApp.cooperativemirroring.client.controller.ClientController;
import com.distributed.chordApp.cooperativemirroring.client.utilities.ClientSettings;
import com.distributed.chordApp.cooperativemirroring.common.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.common.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class used for managing the client Text user interface
 */
public class ClientTUI {

    //Settings of the current client
    private ClientSettings settings = null;
    //Controller for the current client
    private ClientController controller = null;

    public ClientTUI(ClientSettings settings, ClientController controller){
        this.settings = settings;
        this.controller = controller;

        this.clientConsole();
    }

    /*
     * Method used for allowing the client to perform some operations on a server
     */
    private void clientConsole() {
        Boolean goAhead = true ;
        Integer choice = -1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String resourceID = null;
        RequestMessage request = null;
        ResponseMessage response = null;


        do {
            System.out.println("\n======{CLIENT\\\\" + this.settings.getClientIP() + ":" + this.settings.getClientPort() + " CONSOLE}======\n");
            System.out.println("1)Deposit a resource");
            System.out.println("2)Retrieve a resource");
            System.out.println("0)Exit");
            System.out.print("[Choice> ");

            try {
                choice = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println(settings.clientInfoString("Invalid input choice, retry"));
                choice = -1;
                goAhead = true;
            }

            switch(choice)
            {
                case -1:
                    break;
                case 0:
                    goAhead = false ;
                    break;
                case 1:
                    System.out.println(settings.clientInfoString("Deposit resource "));
                    System.out.print(settings.clientInfoString("Insert resource id: "));
                    try {
                        resourceID = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    request = controller.buildRequest(resourceID, true);
                    try {
                        response = null;
                        response = controller.sendRequest(request);
                    } catch (SocketManagerException e) {
                        System.err.println(settings.clientInfoString(e.getMessage()));
                    }
                    this.printResponse(request, response);
                    break;

                case 2:
                    System.out.println(settings.clientInfoString("Retrieve resource "));
                    System.out.print(settings.clientInfoString("Insert resource id: "));
                    try {
                        resourceID = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    request = controller.buildRequest(resourceID, false);
                    try {
                        response = this.controller.sendRequest(request);
                    } catch (SocketManagerException e) {
                        System.err.println(this.settings.clientInfoString(e.getMessage()));
                    }
                    this.printResponse(request, response);
                    break;
                default :
                    break;
            }

        }while(goAhead);

        System.out.println(this.settings.clientInfoString("Exiting from the client, bye"));

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
}
