package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordCallback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Class used for instantiating HostHandler threads used for notify changes on the resources of a Host
 *
 * @date 2019-03-27
 * @version 1.0
 */
public class HostHandlerThread extends Thread implements ChordCallback
{
    //Settings of the associated host
    private HostSettings hostSettings = null;
    //entry point of the associated chord network
    private Chord chordEntryPoint = null;
    //Reference to the resources manager of the current host
    private ResourcesManager resourcesManager = null;
    //Boolean flag that states if the HostHandlerThread has to wait for ack in case of changes in the keyset
    private Boolean requireAck = null;

    public HostHandlerThread(HostSettings hostSettings,
                             Chord chordEntryPoint,
                             ResourcesManager resourcesManager,
                             Boolean requireAck)
    {
        this.setHostSettings(hostSettings);
        this.setChordEntryPoint(chordEntryPoint);
        this.setResourcesManager(resourcesManager);
        this.setRequireAck(requireAck);

    }

    /*Setter methods*/
    private void setHostSettings(HostSettings hostSettings){this.hostSettings = hostSettings; }
    private void setChordEntryPoint(Chord chordEntryPoint){this.chordEntryPoint = chordEntryPoint;}
    private void setResourcesManager(ResourcesManager resourcesManager){this.resourcesManager = resourcesManager; }
    private void setRequireAck(Boolean requireAck){this.requireAck = requireAck; }

    /*Application methods*/

    @Override
    public synchronized void notifyResponsabilityChange()
    {
        Boolean thisHost = false ;

        this.hostSettings.verboseInfoLog("notified of responsability changes, verify the current host resources's new ownership" , HostSettings.HOST_HANDLER_CALLER,false);

        for(Resource resource : this.resourcesManager.getResources())
        {
            String destinationAddress = null;
            thisHost = false ;

            this.hostSettings.verboseInfoLog("verifying ownership of resource: " + resource.getResourceID() +" ..." , HostSettings.HOST_HANDLER_CALLER,false);

            if(this.getHostSettings().getChordNetworkSettings().getPerformBasicLookups())
            {
                destinationAddress = this.chordEntryPoint.lookupKeyBasic(resource.getResourceID());
            }
            else{
                destinationAddress = this.chordEntryPoint.lookupKey(resource.getResourceID());
            }

            if(destinationAddress.equals(this.getHostSettings().getHostIP()) || destinationAddress.equals("127.0.0.1") || destinationAddress.equals("127.0.1.1"))
            {
                thisHost = true;
            }

            if(!thisHost)
            {
                this.hostSettings.verboseInfoLog("need to exchange the resource: " + resource.getResourceID() +" with host: " + destinationAddress , HostSettings.HOST_HANDLER_CALLER,false);

                RequestMessage request = new RequestMessage(
                        this.getHostSettings().getHostIP(),
                        this.getHostSettings().getHostPort(),
                        resource,
                        this.requireAck,
                        false);
                request.setHostDepositRequest(true);

                try {
                    this.hostSettings.verboseInfoLog("sending request for resource: " + resource.getResourceID() +" to host: " + destinationAddress +" ..." , HostSettings.HOST_HANDLER_CALLER,false);
                    this.hostSettings.verboseInfoLog("opening a communication channel with host: " + destinationAddress + " ...", HostSettings.HOST_HANDLER_CALLER,false);
                    Socket destinationSocket = new Socket(destinationAddress, this.getHostSettings().getHostPort());

                    this.hostSettings.verboseInfoLog("communication channel with host: " + destinationAddress + " opened" , HostSettings.HOST_HANDLER_CALLER,false);
                    //Socket destinationSocket = new Socket();
                    //destinationSocket.connect(new InetSocketAddress(destinationAddress, this.getHostSettings().getHostPort()), this.getHostSettings().getConnectionTimeout_MS());
                    //destinationSocket.setSoTimeout(this.getHostSettings().getConnectionTimeout_MS());


                    ObjectOutputStream outChannel = new ObjectOutputStream(destinationSocket.getOutputStream());

                    this.hostSettings.verboseInfoLog("output channel with the destination host: " + destinationAddress + " opened, sending the resource: " + resource.getResourceID() +" ..." , HostSettings.HOST_HANDLER_CALLER,false);

                    outChannel.writeObject(request);
                    outChannel.flush();

                    this.hostSettings.verboseInfoLog("request send for resource: " + resource.getResourceID() + " to host: " + destinationAddress , HostSettings.HOST_HANDLER_CALLER,false);

                    if(this.getRequireAck())
                    {
                        this.hostSettings.verboseInfoLog("waiting for the ACK of " + resource.getResourceID() +" from host: " + destinationAddress + " ..." , HostSettings.HOST_HANDLER_CALLER,false);

                        ObjectInputStream inChannel = new ObjectInputStream(destinationSocket.getInputStream());

                        ResponseMessage ackMessage = (ResponseMessage) inChannel.readObject();

                        this.hostSettings.verboseInfoLog("ACK message for resource: " + resource.getResourceID() +" arrived from host: " + destinationAddress , HostSettings.HOST_HANDLER_CALLER,false);

                        inChannel.close();
                    }

                    outChannel.close();
                    destinationSocket.close();

                    this.resourcesManager.removeResource(resource.getResourceID());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                this.hostSettings.verboseInfoLog("the resource: " + resource.getResourceID() +" remains on this host" , HostSettings.HOST_HANDLER_CALLER,false);
            }
        }

    }

    @Override
    public void run() { }

    /*Getter methods*/
    public HostSettings getHostSettings(){return this.hostSettings; }
    public Chord getChordEntryPoint(){ return this.chordEntryPoint; }
    public ResourcesManager getResourcesManager(){return this.resourcesManager; }
    public Boolean getRequireAck(){return this.requireAck; }

    @Override
    public String toString(){
        String state = "\n======{HOST HANDLER THREAD}======\n";

        state += "\nAssociated host settings: " + this.getHostSettings().toString();
        if(this.getRequireAck()) state += "\nACK required";
        else state += "\nACK not required";

        return state;
    }
}
