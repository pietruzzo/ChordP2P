package com.distributed.chordApp.cooperativemirroring.server.core.settings.exceptions;

/**
 * Class used for managing the exceptions associated to a ChordNetworkSettings class
 */
public class ChordNetworkSettingsException extends Exception
{
    private String exceptionMessage = null;

    public ChordNetworkSettingsException(String exceptionMessage)
    {
        super(exceptionMessage);

        this.setExceptionMessage(exceptionMessage);
    }

    //Setter
    private void setExceptionMessage(String exceptionMessage){this.exceptionMessage = exceptionMessage;}

    //Getter
    public String getExceptionMessage(){return this.exceptionMessage;}
}
