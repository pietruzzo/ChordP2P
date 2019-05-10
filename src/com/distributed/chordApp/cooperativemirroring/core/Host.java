package com.distributed.chordApp.cooperativemirroring.core;

import com.distributed.chordApp.cooperativemirroring.core.backend.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.backend.ClientHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.backend.HostHandlerThread;
import com.distributed.chordApp.cooperativemirroring.core.backend.ResourcesManager;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordLib.Chord;
import com.distributed.chordLib.ChordBuilder;

import com.distributed.chordLib.ChordCallback;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class used for representing a Host of the cooperative mirroring system.
 * Each host is used as Server in case of external requests or Peer in case of internal communication among
 * the hosts of the cooperative mirroring server network.
 *
 * @date 2019-03-27
 * @version 2.0
 */
public class Host implements Runnable, ChordCallback {
    //Settings for the current host
    private HostSettings hostSettings = null;

    //Entry point of the chord network for the cooperative mirroring application
    private Chord chordEntryPoint = null;
    //Manger of the resources of the current host
    private ResourcesManager resourcesManager = null;
    //Handler for changes in the keyset
    private HostHandlerThread hostHandlerThread = null;

    //Boolean flag used for stopping the server (it can be restarted later )
    private Boolean stopHost = false;
    //Boolean flag used for state if we want to definitly shutdown a server (will definitly close the host)
    private Boolean shutdownHost = false;

    public Host(HostSettings hostSettings, @Nullable HashSet<Resource> resources)
    {
        this.setHostSettings(hostSettings);
        this.setResourceManager(resources);

        this.stopHost = false;
        this.shutdownHost = false ;

        if(this.getHostSettings().getVerboseOperatingMode())
        {
            System.out.println(this.getHostSettings().verboseInfoString("Host ready to operate", false));
            System.out.println(this.toString());
        }
    }

    /**
     * Method used for enjoying a chord network
     */
    public void enjoyChordNetwork()
    {
        this.initHostHandlerThread(new HostHandlerThread(
                this.getHostSettings(),
                this.chordEntryPoint,
                this.resourcesManager,
                true
        ));
    }

    /**
     * Method used for transfering all the resources of the current host to another host
     * in case of network leaving
     *
     * @throws IOException
     */
    public void leaveChordNetwork() throws IOException, ClassNotFoundException {

        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("leaving the chord network ...", false));

        if(this.chordEntryPoint == null)
        {
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("chord network not previously setted", false));
            return ;
        }

        this.chordEntryPoint.closeNetwork();
        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("chord network leaved, sending host resources to other hosts", false));


        if(!this.hostSettings.hasShallopHost())
        {
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("shallop host not setted", false));
            return ;
        }

        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("opening the shallop host socket ...", false));
        Socket destinationHostSocket = new Socket(this.hostSettings.getShallopHostIP(), this.hostSettings.getHostPort());
        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("shallop host socket opened: " + this.hostSettings.getShallopHostIP(), false));

        ObjectOutputStream output = new ObjectOutputStream(destinationHostSocket.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(destinationHostSocket.getInputStream());

        for(Resource resource : this.resourcesManager.getResources())
        {
            RequestMessage request = new RequestMessage(this.hostSettings.getHostIP(), this.hostSettings.getHostPort(), resource, false, false);
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("created the Request Message for resource: " + resource.getResourceID(), false));

            output.flush();
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("sending the resource to the destination host ...", false));
            output.writeObject(request);
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("resource sended to the destination host", false));

            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("waiting for the response ...", false));
            ResponseMessage response = (ResponseMessage) input.readObject();
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("response arrived ...", false));

            if(response.getRequestPerformedSuccessfully())
            {
                if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("resource deposited successfully on destination host", false));
            }
            else
            {
                if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("resource NOT deposited on destination host, the resource will be lost", false));
            }
        }

        output.close();
        input.close();

    }

    /*Setter methods*/
    private void setHostSettings(HostSettings hostSettings){this.hostSettings = hostSettings; }
    private void setResourceManager(@Nullable HashSet<Resource> resources){
        this.resourcesManager = ResourcesManager.getInstance();
        if(resources != null) this.resourcesManager.depositResources(resources);
    }

    /*Application settings*/

    /**
     * Method used for instantianting the Handler for the chord callbacks
     * @param hostHandlerThread
     */
    private void initHostHandlerThread(HostHandlerThread hostHandlerThread) {
        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("instantiating a new host handler thread ....", false));
        this.hostHandlerThread = hostHandlerThread;

        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("starting new host handler thread ....", false));
        this.hostHandlerThread.start();

        if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("host handler thread started", false));
    }

    /*
     * Method used for joining or creating a Chord network (depending on the settings of the ChordNetworkSettings passed as parameters)
     */
    private void initChordEntryPoint(ChordNetworkSettings cns)
    {
        Chord cnep = null;

        if(this.getHostSettings().getVerboseOperatingMode())
        {
            if(cns.getJoinExistingChordNetwork())
                System.out.println(this.getHostSettings().verboseInfoString("trying to join a chord network...", false));
            else
                System.out.println(this.getHostSettings().verboseInfoString("trying to create a chord network...", false));
        }


        try
        {
            if(cns.getJoinExistingChordNetwork())
                cnep = ChordBuilder.joinChord(cns.getBootstrapServerAddress(), cns.getAssociatedPort(), this);
            else
                cnep = ChordBuilder.createChord(cns.getAssociatedPort(), cns.getNumberOfFingers(), cns.getNumberOfSuccessors(), cns.getChordModule(), this);



        } catch (IOException e)
        {
            System.err.println(this.getHostSettings().verboseInfoString("impossible to join a chord network", false));
            e.printStackTrace();
            System.err.print(this.getHostSettings().verboseInfoString("shutting down the host", false ));
            this.finalize();
            System.exit(1);
        }

        if(this.getHostSettings().getVerboseOperatingMode())
        {
            if(cns.getJoinExistingChordNetwork())
                System.out.println(this.getHostSettings().verboseInfoString("joined a chord network", false));
            else
                System.out.println(this.getHostSettings().verboseInfoString("created a chord network", false));
        }

        this.chordEntryPoint = cnep;
    }

    @Override
    public void run()
    {
        ServerSocket server = null;
        ThreadPoolExecutor executor = null;
        boolean firstRun = true;

        try {

            if(this.getHostSettings().getVerboseOperatingMode())
                System.out.println(this.getHostSettings().verboseInfoString("starting the server socket ...", false));

            server = new ServerSocket(this.getHostSettings().getHostPort().intValue());

            if(this.getHostSettings().getVerboseOperatingMode())
                System.out.println(this.getHostSettings().verboseInfoString("server socket started", false));

        } catch (IOException e)
        {
            System.err.println(this.getHostSettings().verboseInfoString("cannot instantiate a server socket", false));
            e.printStackTrace();
            System.err.println(this.getHostSettings().verboseInfoString("shutting down the host", false));
            return ;
        }

        try
        {
            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("instantiating the thread pool executor ...", false));

            /*We used the CachedThreadPool in order to have only as much thread as we
             *need , also according to the processing power of the machine of the host
             */
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("thread pool executor instantiated", false));

            while(!this.shutdownHost)
            {
                while(!this.stopHost)
                {
                    if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("waiting for a client request ...", false));

                    Socket client;
                    if(firstRun)
                    {
                        this.initChordEntryPoint(this.getHostSettings().getChordNetworkSettings());
                        client = server.accept();
                        this.enjoyChordNetwork();

                        firstRun = false;
                    }
                    else
                    {
                        client = server.accept();
                    }


                    if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("client request detected, instantiating a client handler thread ...", false));

                    ClientHandlerThread cht = new ClientHandlerThread(  this.getHostSettings(),
                                                                        client,
                                                                        this.resourcesManager,
                                                                        this.chordEntryPoint,
                                                                        true);

                    if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("client handler thread instantiated , trying to execute it ...", false));

                    executor.execute(cht);

                    if(this.getHostSettings().getVerboseOperatingMode()) System.out.println(this.getHostSettings().verboseInfoString("executing client handler thread", false));

                }

            }


        } catch (IOException e) {
            System.err.println(this.getHostSettings().verboseInfoString("unable to accet a client request", false));
            e.printStackTrace();
        }
        finally
        {
            executor.shutdown();

            try
            {
                server.close();
            }
            catch (IOException e)
            {
                System.err.println(this.getHostSettings().verboseInfoString("unable to close the server socket", false));
                e.printStackTrace();
            }

            this.finalize();
        }
    }

    /**
     * Method used for stopping momentaneusly the host
     * (it could be restarted later)
     * @return Boolean value that represent if the host is in the stop state
     */
    public synchronized Boolean stopHost(Boolean verbose)
    {
        if(verbose) System.out.println(this.getHostSettings().verboseInfoString("stopping the host ....",false));

        this.stopHost = true;

        if(verbose) System.out.println(this.getHostSettings().verboseInfoString("host stopped", false));

        return this.getHostStopped();
    }

    /**
     * Method used for restarting a host after it has been stopped
     *
     * @return Boolean value that represents if the host is stopped or not
     */
    public synchronized Boolean startHost(Boolean verbose)
    {
        if(verbose) System.out.println(this.getHostSettings().verboseInfoString("starting the host ....", false));

        this.stopHost = false;

        if(verbose) System.out.println(this.getHostSettings().verboseInfoString("host started", false));

        return this.getHostStopped();
    }

    /**
     * Method used for permanently shutting down a host
     * (it cannot be restarted anyhow)
     * @return
     */
    public synchronized Boolean shutdownHost(Boolean verbose)
    {
        if(verbose) System.out.println(this.getHostSettings().verboseInfoString("shutting down the host ....", false));

        this.shutdownHost = true;

        if(verbose) System.out.println(this.getHostSettings().verboseInfoString("host shutted down", false));

        return this.getHostPoweredOff();
    }

    /**
     * Method used for changing a resource placement
     */
    @Override
    public void notifyResponsabilityChange() {
        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("changing a resource ....", false));

        this.hostHandlerThread.notifyResponsabilityChange();
    }

    /*Getter methods*/
    public HostSettings getHostSettings(){return this.hostSettings; }
    public Boolean getHostStopped(){return this.stopHost; }
    public Boolean getHostPoweredOff(){ return this.shutdownHost; }

    @Override
    public String toString(){
        String state = "\n======{ HOST }======\n";

        state += "\nHost settings: " + this.getHostSettings().toString();
        if(this.getHostStopped()) state += "\nHost stopped";
        else state += "\nHost running";
        if(this.getHostPoweredOff()) state += "\nHost shutted down";

        return state;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() {

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("host handler thread ready to be stopped ... ", false));

        this.hostHandlerThread.stop();

        if(this.getHostSettings().getVerboseOperatingMode())
            System.out.println(this.getHostSettings().verboseInfoString("host handler thread stopped ", false));

        try {
            this.leaveChordNetwork();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
