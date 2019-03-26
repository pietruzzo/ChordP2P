package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordLib.Chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class used to handle requests coming from clients
 */

public class ClientHandlerThread implements Runnable{
    //Address of the associated host
    private String associatedHostIP =  null;
    //Port of the associated port
    private Integer associatedHostPort = null;
    //Chord network entry point reference
    private Chord chordEntryPoint = null;
    //Manager of resources associated to the current host
    private ResourcesManager resourcesManager = null;
    //Socket associated to the client
    private Socket client = null;
    //Used for state if the cliend handler request ack in case of request message forewarding
    private Boolean requestACK = null;
    //Boolean flag used to decree if the handler has to perform the basic lookup
    private Boolean basicLookup = null;

    public ClientHandlerThread(String associatedHostIP,Integer associatedHostPort, Socket client, ResourcesManager resourcesManager, Chord chordEntryPoint,Boolean basicLookup, Boolean requestACK){
       this.setAssociatedHostIP(associatedHostIP);
       this.setAssociatedHostPort(associatedHostPort);
       this.setClient(client);
       this.setResourcesManager(resourcesManager);
       this.setChordEntryPoint(chordEntryPoint);
       this.setBasicLookup(basicLookup);
       this.setRequestACK(requestACK);
    }

    /*Setter methods*/
    private void setAssociatedHostIP(String associatedHostIP){this.associatedHostIP = associatedHostIP;}
    private void setAssociatedHostPort(Integer associatedHostPort){this.associatedHostPort = associatedHostPort;}
    private void setClient(Socket client){ this.client = client; }
    private void setChordEntryPoint(Chord chordEntryPoint){this.chordEntryPoint = chordEntryPoint; }
    private void setResourcesManager(ResourcesManager resourcesManager){ this.resourcesManager = resourcesManager; }
    private void setRequestACK(Boolean requestACK){this.requestACK = requestACK; }
    private void setBasicLookup(Boolean basicLookup){this.basicLookup = basicLookup; }

    /*Application methods*/

    /**
     * Method used for retrieving a resource that should be stored here
     * @param resourceID
     */
    private Resource retrieveResourceLocally(String resourceID)
    {
        Resource resource = null;

        resource = this.resourcesManager.retrieveResource(resourceID);

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

        result = this.resourcesManager.depositResource(resource);

        return result;
    }

    /**
     * Method used for doing the lookup
     * @param resourceID
     * @return
     */
    private String resourceLookup(String resourceID){
        String resourceManagerAddress = null;

        if(this.getBasicLookup()) resourceManagerAddress = this.chordEntryPoint.lookupKeyBasic(resourceID);
        else resourceManagerAddress = this.chordEntryPoint.lookupKey(resourceID);

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

         if(requestMessage.getDepositResource()) result = this.resourcesManager.depositResource(requestMessage.getResource());
         else
         {
             requestedResource = this.resourcesManager.retrieveResource(requestMessage.getResourceID());
             if(requestedResource == null) result = false;
             else result = true;
         }

         responseMessage = new ResponseMessage( this.getAssociatedHostIP(),
                                                this.getAssociatedHostPort(),
                                                requestMessage,
                                                result,
                                      false,
                                                requestedResource);


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

         ackMessage = new ResponseMessage(responseMessage.getSolverHostIP(),
                                          responseMessage.getSolverHostPort(),
                                          responseMessage.getOriginalRequest(),
                                          responseMessage.getRequestPerformedSuccessfully(),
                                            true,
                                           null);

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

            thisHost = this.resourceLookup(requestMessage.getResourceID()).equals(this.getAssociatedHostIP());

            if(thisHost)responseMessage = this.buildResponseMessage(requestMessage);
            else forewardedRequestMessage = this.buildForewardRequestMessage(requestMessage, this.getRequestACK());

            if(!thisHost)
            {
                Socket nextHost = new Socket(this.resourceLookup(requestMessage.getResourceID()), this.getAssociatedHostPort());
                ObjectInputStream nextInputChannel = null;
                ObjectOutputStream nextOutputStream = new ObjectOutputStream(nextHost.getOutputStream());

                nextOutputStream.writeObject(forewardedRequestMessage);

                if(this.getRequestACK())
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
    public String getAssociatedHostIP(){return this.associatedHostIP; }
    public Integer getAssociatedHostPort(){ return this.associatedHostPort; }
    public Boolean getRequestACK(){return this.requestACK; }
    public Boolean getBasicLookup(){return this.basicLookup; }

    @Override
    public String toString(){
        String state = "\n======{ CLIENT HANDLER THREAD }======\n";

        state += "\nAssociated host ip: " + this.getAssociatedHostIP();
        state += "\nAssociated host port: " + this.getAssociatedHostPort();
        if(this.getRequestACK()) state += "\nACK requested";
        else state += "\nACK not required";
        if(this.getBasicLookup()) state += "\nPerform basic lookup";
        else state += "\nPerform advanced lookup";

        return state;
    }


}
