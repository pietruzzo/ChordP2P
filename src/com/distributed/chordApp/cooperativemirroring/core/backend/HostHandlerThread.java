package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.Resource;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordCallback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class used for instantiate object used for notify responsability changes in the key set
 */
public class HostHandlerThread extends Thread implements ChordCallback
{
    private String associatedHostIP = null;
    private Integer associatedHostPort = null;

    private ChordNetworkSettings chordNetworkSettings = null;
    private Chord chordEntryPoint = null;

    private ResourcesManager resourcesManager = null;

    private Boolean requireAck = null;

    public HostHandlerThread(String associatedHostIP,
                             Integer associatedHostPort,
                             ChordNetworkSettings chordNetworkSettings,
                             Chord chordEntryPoint,
                             ResourcesManager resourcesManager,
                             Boolean requireAck)
    {
        this.setAssociatedHostIP(associatedHostIP);
        this.setAssociatedHostPort(associatedHostPort);
        this.setChordNetworkSettings(chordNetworkSettings);
        this.setChordEntryPoint(chordEntryPoint);
        this.setResourcesManager(resourcesManager);
        this.setRequireAck(requireAck);

    }

    /*Setter methods*/
    private void setAssociatedHostIP(String associatedHostIP){this.associatedHostIP = associatedHostIP; }
    private void setAssociatedHostPort(Integer associatedHostPort){this.associatedHostPort = associatedHostPort; }
    private void setChordNetworkSettings(ChordNetworkSettings chordNetworkSettings){this.chordNetworkSettings = chordNetworkSettings; }
    private void setChordEntryPoint(Chord chordEntryPoint){this.chordEntryPoint = chordEntryPoint;}
    private void setResourcesManager(ResourcesManager resourcesManager){this.resourcesManager = resourcesManager; }
    private void setRequireAck(Boolean requireAck){this.requireAck = requireAck; }

    /*Application methods*/

    @Override
    public synchronized void notifyResponsabilityChange(String firstKey, String lastKey)
    {
        String destinationAddress = null;
        Resource resource = new Resource(lastKey);

        resourcesManager.removeResource(firstKey);

        if(this.getChordNetworkSettings().getPerformBasicLookups()) destinationAddress = this.chordEntryPoint.lookupKeyBasic(lastKey);
        else destinationAddress = this.chordEntryPoint.lookupKey(lastKey);

        if(destinationAddress.equals(this.getAssociatedHostIP())) resourcesManager.depositResource(resource);
        else
        {
            RequestMessage request = new RequestMessage(
                                                        this.associatedHostIP,
                                                        this.associatedHostPort,
                                                        resource,
                                                        this.getRequireAck(),
                                                        false);
            try {
                Socket destinationSocket = new Socket(destinationAddress, this.getAssociatedHostPort());
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

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() { }

    /*Getter methods*/
    public String getAssociatedHostIP(){return this.associatedHostIP; }
    public Integer getAssociatedHostPort(){return this.getAssociatedHostPort(); }
    public ChordNetworkSettings getChordNetworkSettings(){return this.chordNetworkSettings; }
    public Chord getChordEntryPoint(){ return this.chordEntryPoint; }
    public ResourcesManager getResourcesManager(){return this.resourcesManager; }
    public Boolean getRequireAck(){return this.requireAck; }

    @Override
    public String toString(){
        String state = "\n======{HOST HANDLER THREAD}======\n";

        state += "\nAssociated Host IP: " + this.getAssociatedHostIP();
        state += "\nAssociated Host Port: " + this.getAssociatedHostPort();
        state += "\nChord network settings: \n" + this.getChordNetworkSettings().toString();
        if(this.getRequireAck()) state += "\nACK required";
        else state += "\nACK not required";

        return state;
    }
}
