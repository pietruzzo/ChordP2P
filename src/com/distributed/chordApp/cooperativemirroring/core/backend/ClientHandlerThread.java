package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordLib.Chord;

import java.net.Socket;

/**
 * Class used to handle requests coming from clients (which could be effettive clients or other hosts of the network)
 *
 * @date 2019-03-27
 * @version 2.0
 */

public class ClientHandlerThread implements Runnable
{
    //Settings of the associated host
    private HostSettings hostSettings = null;
    //Chord network entry point reference
    private Chord chordEntryPoint = null;
    //Manager of resources associated to the current host
    private ResourcesManager resourcesManager = null;
    //Socket associated to the client
    private SocketManager client = null;
    //Used for state if the cliend handler request ack in case of request message forewarding
    private Boolean requireACK = null;

    public ClientHandlerThread(HostSettings hostSettings, Socket client,ResourcesManager resourcesManager, Chord chordEntryPoint, Boolean requireACK) throws SocketManagerException {

        this.setHostSettings(hostSettings);
        this.setClient(client);
        this.setResourcesManager(resourcesManager);
        this.setChordEntryPoint(chordEntryPoint);
        this.setRequireACK(requireACK);
    }

    /*Setter methods*/
    private void setHostSettings(HostSettings hostSettings){this.hostSettings = hostSettings; }
    private void setClient(Socket client) throws SocketManagerException {
        this.client = new SocketManager(client, this.hostSettings.getConnectionTimeout_MS(), this.hostSettings.getConnectionRetries());

        this.client.connect();
    }
    private void setChordEntryPoint(Chord chordEntryPoint){this.chordEntryPoint = chordEntryPoint; }
    private void setResourcesManager(ResourcesManager resourcesManager){ this.resourcesManager = resourcesManager; }
    private void setRequireACK(Boolean requireACK){this.requireACK = requireACK; }

    /*Application methods*/

    /**
     * Method used for retrieving a resource that should be stored here
     * @param resourceID
     */
    private Resource retrieveResourceLocally(String resourceID)
    {
        Resource resource = null;

        this.hostSettings.verboseInfoLog("trying to retrieve resource: " + resourceID + " on the current host ...", HostSettings.CLIENT_HANDLER_CALLER,false);

        resource = this.resourcesManager.retrieveResource(resourceID);

        if(resource == null)
        {
            this.hostSettings.verboseInfoLog("resource: " + resourceID + " NOT found on the current host ", HostSettings.CLIENT_HANDLER_CALLER,true);
        }
        else
        {
            this.hostSettings.verboseInfoLog("resource: " + resourceID + " retrieved on the current host", HostSettings.CLIENT_HANDLER_CALLER,false);
        }

        return resource;
    }

    /**
     * Method used for deposit a resource that should be stored here
     * @param resource
     * @return
     */
    private Boolean depositResourceLocally(Resource resource)
    {
        Boolean result = null;

        this.hostSettings.verboseInfoLog("trying to deposit resource: " + resource.getResourceID() + " on the current host ...", HostSettings.CLIENT_HANDLER_CALLER,false);

        result = this.resourcesManager.depositResource(resource);

        if(result)
        {
            this.hostSettings.verboseInfoLog("resource: " + resource.getResourceID() + " successfully deposited on the current host", HostSettings.CLIENT_HANDLER_CALLER,false);
        }
        else
        {
            this.hostSettings.verboseInfoLog("unable to deposit resource: " + resource.getResourceID() + " on the current host ", HostSettings.CLIENT_HANDLER_CALLER,true);
        }

        return result;
    }

    /**
     * Method used for doing the lookup
     * @param resourceID
     * @return
     */
    private String resourceLookup(String resourceID){
        String resourceManagerAddress = null;

        this.hostSettings.verboseInfoLog("performing the lookup for the resource: " + resourceID + "...", HostSettings.CLIENT_HANDLER_CALLER,false);

        if(this.getHostSettings().getChordNetworkSettings().getPerformBasicLookups())
            resourceManagerAddress = this.chordEntryPoint.lookupKeyBasic(resourceID);
        else resourceManagerAddress = this.chordEntryPoint.lookupKey(resourceID);

        this.hostSettings.verboseInfoLog("IP of the resource: " + resourceID + " owner: " + resourceManagerAddress, HostSettings.CLIENT_HANDLER_CALLER,false);

        return resourceManagerAddress;
    }

    /**
     * Method used for creating a forewarding request message
     * @param originalRequest
     * @param ackRequested
     * @return
     */
    private RequestMessage buildForewardRequestMessage(RequestMessage originalRequest,Boolean ackRequested)
    {
        RequestMessage newRequest = null;

        this.hostSettings.verboseInfoLog("building a forward request message ...", HostSettings.CLIENT_HANDLER_CALLER,false);

        if(originalRequest.getDepositResource())
        {
            newRequest = new RequestMessage(originalRequest.getOriginalSenderIP(),
                                            originalRequest.getOriginalSenderPort(),
                                            originalRequest.getResource(),
                                            ackRequested,
                                            true
                                            );
        }
        else
        {
            newRequest = new RequestMessage(originalRequest.getOriginalSenderIP(),
                                            originalRequest.getOriginalSenderPort(),
                                            originalRequest.getResourceID(),
                                            ackRequested,
                                            true
            );

        }

        this.hostSettings.verboseInfoLog("Foreward request: \n" + newRequest.toString(), HostSettings.CLIENT_HANDLER_CALLER,false);

        return newRequest;
     }

    /**
     * Method used for creating a response message (i.e. a message associated to a local storing/loading of resources
     * @param requestMessage
     * @return
     */
     private ResponseMessage buildResponseMessage(RequestMessage requestMessage)
     {
         ResponseMessage responseMessage = null;
         Resource requestedResource = null;
         Boolean result = null;

         this.hostSettings.verboseInfoLog("building a response message ...", HostSettings.CLIENT_HANDLER_CALLER,false);

         if(requestMessage.getDepositResource())
         {
             result = this.depositResourceLocally(requestMessage.getResource());
         }
         else
         {
             requestedResource = this.retrieveResourceLocally(requestMessage.getResourceID());

             if(requestedResource == null)
             {
                 result = false;
             }
             else{
                 result = true;
             }
         }

         responseMessage = new ResponseMessage( this.getHostSettings().getHostIP(),
                                                this.getHostSettings().getHostPort(),
                                                requestMessage,
                                                result,
                                      false,
                                                requestedResource);

         this.hostSettings.verboseInfoLog("response message: \n" + responseMessage.toString(), HostSettings.CLIENT_HANDLER_CALLER,false);


         return responseMessage;

     }

    /**
     * Method used for building an ack response message
     * @param responseMessage
     * @return
     */
     private ResponseMessage buildAckMessage(ResponseMessage responseMessage)
     {
         ResponseMessage ackMessage = null;

         this.hostSettings.verboseInfoLog("building an ACK message ...", HostSettings.CLIENT_HANDLER_CALLER,false);

         ackMessage = new ResponseMessage(responseMessage.getSolverHostIP(),
                                          responseMessage.getSolverHostPort(),
                                          responseMessage.getOriginalRequest(),
                                          responseMessage.getRequestPerformedSuccessfully(),
                                            true,
                                           null);

         this.hostSettings.verboseInfoLog("ACK message: \n" + ackMessage.toString(), HostSettings.CLIENT_HANDLER_CALLER,false);

         return ackMessage;


     }

    @Override
    //Gestisco le richieste di un client
    public void run()
    {
        //Request message that the client send to us.
        RequestMessage requestMessage = null;

        //Nel caso in cui io debba inoltrare la richiesta
        RequestMessage forewardedRequestMessage = null;
        ResponseMessage responseMessage = null;
        ResponseMessage ackMessage = null;

        Boolean thisHost = false;

        try {

            this.hostSettings.verboseInfoLog("waiting for a request ...", HostSettings.CLIENT_HANDLER_CALLER,false);

            requestMessage = (RequestMessage) this.client.get();

            this.hostSettings.verboseInfoLog("request arrived", HostSettings.CLIENT_HANDLER_CALLER,false);

            //IP address of te resource keeper
            String resourceKeeperIP ;

            //Case of direct deposit request (lookup don't needed)
            if(requestMessage.getHostDepositRequest())
            {
                resourceKeeperIP = this.getHostSettings().getHostIP();
            }
            //In case we have to perfrom the canonical lookup
            else
            {
                resourceKeeperIP = this.resourceLookup(requestMessage.getResourceID());
            }

            if((resourceKeeperIP.equals("127.0.0.1")) || (resourceKeeperIP.equals("127.0.1.1")) || (resourceKeeperIP.equals(this.getHostSettings().getHostIP())))
            {
                thisHost = true;
            }

            if (thisHost)
            {

                this.hostSettings.verboseInfoLog("thre request is directed to this host ...", HostSettings.CLIENT_HANDLER_CALLER,false);
                responseMessage = this.buildResponseMessage(requestMessage);
            }
            else {
                this.hostSettings.verboseInfoLog("the request is directed to another host: " + resourceKeeperIP + " , forwarding the request", HostSettings.CLIENT_HANDLER_CALLER,false);
                forewardedRequestMessage = this.buildForewardRequestMessage(requestMessage, this.getRequireACK());
            }

            if (!thisHost) {

                String nextHostAddress = this.resourceLookup(requestMessage.getResourceID());

                this.hostSettings.verboseInfoLog("trying to open a channel with host: " + nextHostAddress + " ...", HostSettings.CLIENT_HANDLER_CALLER,false);

                //Socket nextHost = new Socket(nextHostAddress, this.getHostSettings().getHostPort());
                SocketManager nextHost = new SocketManager(nextHostAddress, this.hostSettings.getHostPort(), this.hostSettings.getConnectionTimeout_MS(), this.hostSettings.getConnectionRetries());
                nextHost.connect();

                this.hostSettings.verboseInfoLog("opened a connection with the host: " + nextHostAddress , HostSettings.CLIENT_HANDLER_CALLER,false);
                this.hostSettings.verboseInfoLog("forwarding the request to: " + nextHostAddress, HostSettings.CLIENT_HANDLER_CALLER,false);

                nextHost.post(forewardedRequestMessage, false);

                this.hostSettings.verboseInfoLog("request forwarded to host: " + nextHostAddress, HostSettings.CLIENT_HANDLER_CALLER,false);

                if (this.getRequireACK())
                {
                    this.hostSettings.verboseInfoLog("waiting for the ACK from host: " + nextHostAddress, HostSettings.CLIENT_HANDLER_CALLER,false);

                    ackMessage = (ResponseMessage) nextHost.get();

                    this.hostSettings.verboseInfoLog("ACK message arrived from host: " + nextHostAddress + " closing the connection with such host ....", HostSettings.CLIENT_HANDLER_CALLER,false);
                }

                this.hostSettings.verboseInfoLog("closing connection with host: " + nextHostAddress + " ...", HostSettings.CLIENT_HANDLER_CALLER,false);

                nextHost.disconnect();

                this.hostSettings.verboseInfoLog("connection with host: " + nextHostAddress + " closed", HostSettings.CLIENT_HANDLER_CALLER,false);


            }

            if (thisHost)
            {


                if (!requestMessage.getForewarded())
                {

                    this.hostSettings.verboseInfoLog("request directed to this host, sending back the response ...", HostSettings.CLIENT_HANDLER_CALLER,false);

                    this.client.post(responseMessage, false);
                }
                //Caso in cui la richiesta sia stata inoltrata da un'altro host
                /*else {
                    if(this.getHostSettings().getVerboseOperatingMode())
                        System.out.println(this.getHostSettings().verboseInfoString("forwarded request, creating a channel with the client ... " , true));
                    //Socket destinationHost = new Socket(requestMessage.getOriginalSenderIP(), requestMessage.getOriginalSenderPort());
                    Socket destinationHost = new Socket(requestMessage.getOriginalSenderIP(), requestMessage.getOriginalSenderPort());
                    //destinationHost.connect();
                    //destinationHost.setSoTimeout(this.getHostSettings().getConnectionTimeout_MS());

                    if(this.getHostSettings().getVerboseOperatingMode())
                        System.out.println(this.getHostSettings().verboseInfoString("client channel created, sending the response ..." , true));

                    ObjectOutputStream destinationChannel = new ObjectOutputStream(destinationHost.getOutputStream());

                    destinationChannel.flush();
                    destinationChannel.writeObject(responseMessage);

                    destinationChannel.close();
                    destinationHost.close();
                }*/

                if ((requestMessage.getForewarded()) && (requestMessage.getAckRequested()))
                {

                    this.hostSettings.verboseInfoLog("sending the ACK to the previous host ...", HostSettings.CLIENT_HANDLER_CALLER,false);

                    ackMessage = this.buildAckMessage(responseMessage);

                    this.client.post(responseMessage, false);

                    this.hostSettings.verboseInfoLog("ACK send, connection with the previous host closed", HostSettings.CLIENT_HANDLER_CALLER,false);
                }
            }

            try {
                this.hostSettings.verboseInfoLog("closing the connection with the client socket ... ", HostSettings.CLIENT_HANDLER_CALLER,false);
                this.client.disconnect();
                this.hostSettings.verboseInfoLog("connection with the client socket closed.", HostSettings.CLIENT_HANDLER_CALLER,false);
            } catch (SocketManagerException e) {
                this.hostSettings.verboseInfoLog("unable to close client socket: \n" + e.getMessage(), HostSettings.CLIENT_HANDLER_CALLER,true);
            }
        } catch (SocketManagerException e) {
            this.hostSettings.verboseInfoLog("exception rised: " + e.getMessage(), HostSettings.CLIENT_HANDLER_CALLER,false);
        }
    }

        /*Getter methods*/
    public HostSettings getHostSettings(){return this.hostSettings; }
    public Boolean getRequireACK(){return this.requireACK; }

    @Override
    public String toString(){
        String state = "\n======{ CLIENT HANDLER THREAD }======\n";

        state += "\nAssociated host settings: " + this.getHostSettings().toString();
        if(this.getRequireACK()) state += "\nACK required";
        else state += "\nACK not required";

        return state;
    }


}
