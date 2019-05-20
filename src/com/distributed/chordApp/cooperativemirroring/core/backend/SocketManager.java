package com.distributed.chordApp.cooperativemirroring.core.backend;

import com.distributed.chordApp.cooperativemirroring.core.backend.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.core.backend.exceptions.codes.SocketManagerExceptionCode;
import com.distributed.chordApp.cooperativemirroring.core.backend.messages.AckMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Class used for defining a more complex socket management for our connection
 */
public class SocketManager
{

    private String destinationIP = null;
    private Integer destinationPort = null;
    private Integer connectionTimeout_ms = null;
    private Integer connectionRetries = null;

    private Socket destinationSocket = null;
    private ObjectOutputStream outStream = null;
    private ObjectInputStream inStream = null;

    public SocketManager(String destinationIP,
                         Integer destinationPort,
                         Integer connectionTimeout_ms,
                         Integer connectionRetries)
    {
        this.setDestinationIP(destinationIP);
        this.setDestinationPort(destinationPort);
        this.setConnectionTimeout_ms(connectionTimeout_ms);
        this.setConnectionRetries(connectionRetries);
    }

    public SocketManager(Socket destinationSocket,
                         Integer connectionTimeout_ms,
                         Integer connectionRetries)
    {
        this.setDestinationSocket(destinationSocket);
        this.setConnectionTimeout_ms(connectionTimeout_ms);
        this.setConnectionRetries(connectionRetries);
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
    private void openConnection() throws SocketManagerException
    {
        Socket socket = null;
        String exceptionMessage = null;
        boolean connectionEstablished = true;

        if(this.destinationSocket != null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_ALREADY_ESTABLISHED.getCode());
        }

        //Here we try more than one time to establish a connection
        for(int i = this.connectionRetries; i > 0; i--)
        {
            socket = new Socket();
            connectionEstablished = false;

            try {
                socket.setSoTimeout(this.connectionTimeout_ms);

            } catch (SocketException e) {
                exceptionMessage = SocketManagerExceptionCode.CONNECTION_INVALID_TIMEOUT.getCode();
            }

            try {
                socket.connect(new InetSocketAddress(this.destinationIP, this.destinationPort));
                connectionEstablished = true;

            } catch (IOException e) {
                exceptionMessage = SocketManagerExceptionCode.CONNECTION_BAD_PARAMETERS.getCode();
                connectionEstablished = false;
            }

            if(connectionEstablished)
            {
                i = 0;
            }
        }

        if(!connectionEstablished)
        {
            String message = SocketManagerExceptionCode.CONNECTION_MAXIMUM_RETRIES_REACHED + "\n" ;

            message += SocketManagerExceptionCode.CONNECTION_TIMEOUT_REACHED + "\n";
            message += exceptionMessage;

            throw new SocketManagerException(message);
        }
        else
        {
            this.setDestinationSocket(socket);
        }
    }

    /**
     * Method used for opening an output channel with the destination host
     * @throws SocketManagerException
     */
    private void openOutputStream() throws SocketManagerException {
        if(this.destinationSocket == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        ObjectOutputStream oos = null;
        boolean oosEstablished = false;

        for(int i = this.connectionRetries; i > 0 ; i--)
        {
            try {
                oos = new ObjectOutputStream(this.destinationSocket.getOutputStream());
                oosEstablished = true;
            } catch (IOException e) {
                oosEstablished = false;
            }

            if(oosEstablished)
            {
                i = 0;
            }
        }

        if(!oosEstablished)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_OPEN_OUTPUT_STREAM.getCode());
        }

        this.setOutStream(oos);
    }

    /**
     * Method used for opening an input stream with the socket
     */
    private void openInputStream() throws SocketManagerException {

        if(this.destinationSocket == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        if(this.outStream == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.OUTPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        ObjectInputStream ois = null;
        boolean oisEstablished = false;

        for(int i = this.connectionRetries; i > 0; i--)
        {
            try {
                ois = new ObjectInputStream(this.destinationSocket.getInputStream());
                oisEstablished = true;
            } catch (IOException e) {
               oisEstablished = false;
            }

            if(oisEstablished)
            {
                i = 0;
            }
        }

        if(!oisEstablished)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_OPEN_INPUT_STREAM.getCode());
        }

        this.setInStream(ois);
    }

    /**
     * Method used for trying to connect to a specific socket
     * @throws SocketManagerException
     */
    public void connect() throws SocketManagerException
    {
        if(this.destinationSocket == null)
        {
            this.openConnection();
        }

        this.openOutputStream();
        this.openInputStream();
    }

    /**
     * Method used for exchanging messages with the destination host
     * @param message
     * @param waitACK
     * @return
     */
    public Serializable post(Serializable message,Boolean waitACK) throws SocketManagerException {
        Serializable response = null;

        if(this.destinationSocket == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        if(this.outStream == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.OUTPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        if(this.inStream == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.INPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        try {
            this.outStream.flush();
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_FLUSH_OUTPUT_STREAM.getCode());
        }

        try {
            this.outStream.writeObject(message);
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_WRITE_MESSAGE_ON_OUTPUT_STREAM.getCode());
        }

        if(waitACK)
        {
            try {
                response = (Serializable) inStream.readObject();
            } catch (IOException e) {
                throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_READ_OBJECT_FROM_INPUT_STREAM.getCode() + ":\n" + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_READ_OBJECT_FROM_INPUT_STREAM.getCode() + ":\n" + e.getMessage());

            }
        }

        return response;
    }

    /**
     * Method used for waiting messages from the destination host
     * @return
     */
    public Serializable get() throws SocketManagerException {
        Serializable request = null;

        if(this.destinationSocket == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.CONNECTION_NOT_ESTABLISHED.getCode());
        }

        if(this.inStream == null)
        {
            throw new SocketManagerException(SocketManagerExceptionCode.INPUT_STREAM_NOT_OPENED_YET.getCode());
        }

        try {
            request = (Serializable) this.inStream.readObject();
        } catch (IOException | ClassNotFoundException e ) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_READ_OBJECT_FROM_INPUT_STREAM.getCode());
        }

        return request;
    }

    /**
     * Method used for closing an open socket connection
     * @throws SocketManagerException
     */
    public void disconnect() throws SocketManagerException
    {
        if(this.destinationSocket == null)
        {
            return ;
        }

        try {
            this.outStream.close();
            this.setOutStream(null);
        } catch (IOException e) {
           throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_CLOSE_OUTPUT_STREAM.getCode());
        }

        try {
            this.inStream.close();
            this.setInStream(null);
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_CLOSE_INPUT_STREAM.getCode());
        }

        try {
            this.destinationSocket.close();
            this.setDestinationSocket(null);
        } catch (IOException e) {
            throw new SocketManagerException(SocketManagerExceptionCode.UNABLE_TO_CLOSE_CONNECTION.getCode());
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
