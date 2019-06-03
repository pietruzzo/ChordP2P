package com.distributed.chordApp.cooperativemirroring.common.utilities;

import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.codes.SocketManagerExceptionCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Class used for defining a more complex socket management for our connection
 */
public class SocketManager {

    public static final Integer DEFAULT_CONNECTION_TIMEOUT_MS = 7000;
    public static final Integer DEFAULT_CONNECTION_RETRIES = 5;

    private String destinationIP = null;
    private Integer destinationPort = null;
    private Integer connectionTimeout_ms = null;
    private Integer connectionRetries = null;

    private Socket destinationSocket = null;
    private ObjectOutputStream outStream = null;
    private ObjectInputStream inStream = null;

    private boolean enableTimeout = false;

    public SocketManager(String destinationIP,
                         Integer destinationPort,
                         Integer connectionTimeout_ms,
                         Integer connectionRetries,
                         boolean enableTimeout)
    {
        this.setDestinationIP(destinationIP);
        this.setDestinationPort(destinationPort);
        this.setConnectionTimeout_ms(connectionTimeout_ms);
        this.setConnectionRetries(connectionRetries);

        this.enableTimeout = enableTimeout;
    }

    public SocketManager(Socket destinationSocket,
                         Integer connectionTimeout_ms,
                         Integer connectionRetries,
                         boolean enableTimeout)
    {
        this.setDestinationSocket(destinationSocket);
        this.setConnectionTimeout_ms(connectionTimeout_ms);
        this.setConnectionRetries(connectionRetries);

        this.enableTimeout = enableTimeout;
    }

    //Setters
    private void setDestinationIP(String destinationIP){this.destinationIP = destinationIP;}
    private void setDestinationPort(Integer destinationPort){this.destinationPort = destinationPort;}
    private void setConnectionTimeout_ms(Integer connectionTimeout_ms){this.connectionTimeout_ms = connectionTimeout_ms;}
    private void setConnectionRetries(Integer connectionRetries){this.connectionRetries = connectionRetries;}

    private void setDestinationSocket(Socket destinationSocket){this.destinationSocket = destinationSocket;}
    private void setOutStream(ObjectOutputStream outStream){this.outStream = outStream;}
    private void setInStream(ObjectInputStream inStream){this.inStream = inStream; }

    //Application methods

    /**
     * Method used for opening a connection with the destination
     * @throws SocketManagerException
     */
    private void openConnection() throws SocketManagerException {
        Socket socket = new Socket();

        if(this.destinationSocket != null) {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_ALREADY_ESTABLISHED.getCode());
        }

        try {
            
            if(enableTimeout){
                socket.connect(new InetSocketAddress(this.destinationIP, this.destinationPort),  this.connectionTimeout_ms);
            }else{
                socket.connect(new InetSocketAddress(this.destinationIP, this.destinationPort));
            }


        } catch (IOException e) {
            throw new SocketManagerException(e.getMessage());
        }

        this.setDestinationSocket(socket);
    }

    /**
     * Method used for opening an output channel with the destination host
     * @throws SocketManagerException
     */
    private void openOutputStream() throws SocketManagerException {
        if(this.destinationSocket == null)
        { throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(this.destinationSocket.getOutputStream());
        } catch (IOException e) {
            throw new SocketManagerException(e.getMessage());
        }

        this.setOutStream(oos);
    }

    /**
     * Method used for opening an input stream with the socket
     */
    private void openInputStream() throws SocketManagerException {

        if(this.destinationSocket == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        if(this.outStream == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.OUTPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(this.destinationSocket.getInputStream());
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_OPEN_INPUT_STREAM.getCode() + " : " + e.getMessage());
        }


        this.setInStream(ois);
    }

    /**
     * Method used for trying to connect to a specific socket
     * @throws SocketManagerException
     */
    public void connect() throws SocketManagerException
    {
        if(this.destinationSocket == null) {
            this.openConnection();
        }

        this.openOutputStream();
        this.openInputStream();
    }

    /**
     * Method used for exchanging messages with the destination host
     * @param message
     * @return
     */
    public Boolean post(Serializable message) throws SocketManagerException {
        Boolean send = false;

        if(this.destinationSocket == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        if(this.outStream == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.OUTPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        if(this.inStream == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.INPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        try {
            this.outStream.flush();
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_FLUSH_OUTPUT_STREAM.getCode() + " : " + e.getMessage());
        }

        try {
            this.outStream.writeObject(message);
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_WRITE_MESSAGE_ON_OUTPUT_STREAM.getCode() + " : " + e.getMessage());
        }

        send = true;

        return send;
    }

    /**
     * Method used for waiting messages from the destination host
     * @return
     */
    public Serializable get() throws SocketManagerException {
        Serializable request = null;

        if(this.destinationSocket == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        if(this.inStream == null) {
            throw new SocketManagerException(SocketManagerExceptionCode.INPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        try {
            request = (Serializable) this.inStream.readObject();
        } catch (IOException | ClassNotFoundException e ) {
            throw new SocketManagerException( e.getMessage() + e.getStackTrace() );
        }

        return request;
    }

    /**
     * Method used for closing an open socket connection
     * @throws SocketManagerException
     */
    public void disconnect() throws SocketManagerException {
        if(this.destinationSocket == null)
        {
            return ;
        }

        try {
            this.outStream.close();
            this.setOutStream(null);
        } catch (IOException e) {
           throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_CLOSE_OUTPUT_STREAM.getCode() + " : " + e.getMessage());
        }

        try {
            this.inStream.close();
            this.setInStream(null);
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_CLOSE_INPUT_STREAM.getCode() + " : " + e.getMessage());
        }

        try {
            this.destinationSocket.close();
            this.setDestinationSocket(null);
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_CLOSE_CONNECTION.getCode() + " : " +e.getMessage());
        }

    }

    //Getters
    public String getDestinationIP(){return this.destinationIP;}
    public Integer getDestinationPort(){return this.destinationPort;}
    public Integer getConnectionTimeout_ms(){return this.connectionTimeout_ms;}
    public Integer getConnectionRetries(){return this.connectionRetries;}

    public Socket getDestinationSocket(){return this.destinationSocket;}
    public ObjectOutputStream getOutStream(){return this.outStream;}
    public ObjectInputStream getInStream(){return this.inStream;}
}
