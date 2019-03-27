package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordLib.Chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private Socket client = null;
    //Used for state if the cliend handler request ack in case of request message forewarding
    private Boolean requireACK = null;

    public ClientHandlerThread(HostSettings hostSettings, Socket client, ResourcesManager resourcesManager, Chord chordEntryPoint, Boolean requireACK)
    {
        this.setHostSettings(hostSettings);
        this.setClient(client);
        this.setResourcesManager(resourcesManager);
        this.setChordEntryPoint(chordEntryPoint);
        this.setRequireACK(requireACK);
    }

    /*Setter methods*/
    private void setHostSettings(HostSettings hostSettings){this.hostSettings = hostSettings; }
    private void setClient(Socket client){ this.client = client; }
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

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("trying to retrive the resource : " + resourceID + " ...", true));

        resource = this.resourcesManager.retrieveResource(resourceID);

        if(this.getHostSettings().getVerboseOperatingMode())
        {
            if(resource == null)
                System.out.println(this.getHostSettings().verboseInfoString("resource: " + resourceID + " not found", true));
            else
                System.out.println(this.getHostSettings().verboseInfoString("resource: " + resourceID + " found", true));
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

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("trying to deposit the resource : " + resource.getResourceID() + " ...", true));

        result = this.resourcesManager.depositResource(resource);

        if(this.getHostSettings().getVerboseOperatingMode())
        {
            if(result)
                System.out.println(this.getHostSettings().verboseInfoString("resource: " + resource.getResourceID() + " deposited", true));
            else
                System.out.println(this.getHostSettings().verboseInfoString("resource: " + resource.getResourceID() + " not deposited", true));
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

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("performing a lookup for the resource : " + resourceID + " ..." , true));

        if(this.getHostSettings().getChordNetworkSettings().getPerformBasicLookups())
            resourceManagerAddress = this.chordEntryPoint.lookupKeyBasic(resourceID);
        else resourceManagerAddress = this.chordEntryPoint.lookupKey(resourceID);

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("IP of the resource keeper : " + resourceManagerAddress , true));

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

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("building a foreward request message ..." , true));

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

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("foreward request message: " + newRequest.toString() , true));

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

         if(this.getHostSettings().getVerboseOperatingMode())
             System.out.println(this.getHostSettings().verboseInfoString("building a response message ..." , true));

         if(requestMessage.getDepositResource()) result = this.resourcesManager.depositResource(requestMessage.getResource());
         else
         {
             requestedResource = this.resourcesManager.retrieveResource(requestMessage.getResourceID());
             if(requestedResource == null) result = false;
             else result = true;
         }

         responseMessage = new ResponseMessage( this.getHostSettings().getHostIP(),
                                                this.getHostSettings().getHostPort(),
                                                requestMessage,
                                                result,
                                      false,
                                                requestedResource);

         if(this.getHostSettings().getVerboseOperatingMode())
             System.out.println(this.getHostSettings().verboseInfoString("response message: " + responseMessage.toString() , true));


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

         if(this.getHostSettings().getVerboseOperatingMode())
             System.out.println(this.getHostSettings().verboseInfoString("building an ACK message ..." , true));

         ackMessage = new ResponseMessage(responseMessage.getSolverHostIP(),
                                          responseMessage.getSolverHostPort(),
                                          responseMessage.getOriginalRequest(),
                                          responseMessage.getRequestPerformedSuccessfully(),
                                            true,
                                           null);

         if(this.getHostSettings().getVerboseOperatingMode())
             System.out.println(this.getHostSettings().verboseInfoString("ACK message: " + ackMessage.toString() , true));

         return ackMessage;


     }

    @Override
    public void run()
    {
        //Canale di input dell'host che ci ha fatto/inoltrato la richiesta
        ObjectInputStream inputChannel = null;
        //Messaggio di richiesta inviatoci
        RequestMessage requestMessage = null;

        RequestMessage forewardedRequestMessage = null;
        ResponseMessage responseMessage = null;
        ResponseMessage ackMessage = null;

        Boolean thisHost = false ;

        try
        {
            inputChannel = new ObjectInputStream(this.client.getInputStream());
            requestMessage = (RequestMessage) inputChannel.readObject();

            thisHost = this.resourceLookup(requestMessage.getResourceID()).equals(this.getHostSettings().getHostIP());

            if(thisHost)responseMessage = this.buildResponseMessage(requestMessage);
            else forewardedRequestMessage = this.buildForewardRequestMessage(requestMessage, this.getRequireACK());

            if(!thisHost)
            {
                Socket nextHost = new Socket(this.resourceLookup(requestMessage.getResourceID()), this.getHostSettings().getHostPort());
                ObjectInputStream nextInputChannel = null;
                ObjectOutputStream nextOutputStream = new ObjectOutputStream(nextHost.getOutputStream());

                nextOutputStream.writeObject(forewardedRequestMessage);

                if(this.getRequireACK())
                {
                    nextInputChannel = new ObjectInputStream(nextHost.getInputStream());

                    ackMessage = (ResponseMessage) nextInputChannel.readObject();

                    nextInputChannel.close();
                }

                nextOutputStream.close();
                nextHost.close();
            }

            if(thisHost)
            {
                //Nel caso in cui la richiesta venisse dal richiedente originale , rispondo a lui direttamente usando
                //il socket client
                if(!requestMessage.getForewarded()) {
                    ObjectOutputStream outputChannel = new ObjectOutputStream(this.client.getOutputStream());

                    outputChannel.writeObject(responseMessage);

                    outputChannel.close();
                }
                //Caso in cui la richiesta sia stata inoltrata da un'altro host
                else {
                    Socket destinationHost = new Socket(requestMessage.getOriginalSenderIP(), requestMessage.getOriginalSenderPort());
                    ObjectOutputStream destinationChannel = new ObjectOutputStream(destinationHost.getOutputStream());

                    destinationChannel.writeObject(responseMessage);

                    destinationChannel.close();
                    destinationHost.close();
                }

                if(requestMessage.getForewarded() && requestMessage.getAckRequested())
                {
                    ackMessage = this.buildAckMessage(responseMessage);
                    ObjectOutputStream outputChannel = new ObjectOutputStream(this.client.getOutputStream());
                    outputChannel.writeObject(ackMessage);

                    outputChannel.close();
                }
            }

            inputChannel.close();


        }catch(IOException ioe){

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
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
