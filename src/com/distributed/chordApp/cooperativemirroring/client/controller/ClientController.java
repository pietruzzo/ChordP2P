package com.distributed.chordApp.cooperativemirroring.client.controller;

import com.distributed.chordApp.cooperativemirroring.client.utilities.ClientSettings;
import com.distributed.chordApp.cooperativemirroring.common.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.common.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SocketManager;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.common.Resource;

/**
 * Method used for managing the client operations
 */
public class ClientController {
    private ClientSettings settings = null;

    public ClientController(ClientSettings settings){
        this.settings = settings;
    }

    /*
     * Method used for building a new request for the server
     */
    public RequestMessage buildRequest(String resourceID, Boolean depositResource) {
        RequestMessage request = null;
        Resource resource = null;

        if(depositResource) {
            resource = new Resource( resourceID );

            request = new RequestMessage(
                    this.settings.getClientIP(),
                    this.settings.getClientPort(),
                    resource);
        }
        else {
            request = new RequestMessage(
                    this.settings.getClientIP(),
                    this.settings.getClientPort(),
                    resourceID);
        }


        return request;
    }

    /*
     * Method used for sending a request to a Server
     */
    public ResponseMessage sendRequest(RequestMessage requestMessage) throws SocketManagerException {

        SocketManager server = new SocketManager(this.settings.getReferenceServerIP(), this.settings.getReferenceServerPort(), 5000, 5);

        server.connect();
        ResponseMessage response = null;

        server.post(requestMessage);

        response = (ResponseMessage)server.get();

        server.disconnect();

        return response ;
    }
}
