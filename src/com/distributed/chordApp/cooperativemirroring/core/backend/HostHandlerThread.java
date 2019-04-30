package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordCallback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

        for(Resource resource : this.resourcesManager.getResources())
        {
            String destinationAddress = null;
            thisHost = false ;

            if(this.getHostSettings().getChordNetworkSettings().getPerformBasicLookups()) destinationAddress = this.chordEntryPoint.lookupKeyBasic(resource.getResourceID());
            else destinationAddress = this.chordEntryPoint.lookupKey(resource.getResourceID());

            if(destinationAddress.equals(this.getHostSettings().getHostIP()) || destinationAddress.equals("127.0.0.1") || destinationAddress.equals("127.0.1.1"))
                thisHost = true;

            if(!thisHost)
            {
                RequestMessage request = new RequestMessage(
                        this.getHostSettings().getHostIP(),
                        this.getHostSettings().getHostPort(),
                        resource,
                        this.getRequireAck(),
                        false);
                try {
                    Socket destinationSocket = new Socket(destinationAddress, this.getHostSettings().getHostPort());
                    ObjectOutputStream outChannel = new ObjectOutputStream(destinationSocket.getOutputStream());

                    outChannel.writeObject(request);

                    if(this.getRequireAck())
                    {

                        ObjectInputStream inChannel = new ObjectInputStream(destinationSocket.getInputStream());

                        ResponseMessage ackMessage = (ResponseMessage) inChannel.readObject();

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
