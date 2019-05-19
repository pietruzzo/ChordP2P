package com.distributed.chordApp.cooperativemirroring.core.settings.exceptions;

/**
 * Class used to rise an exception in case of an invalid parameter passed to a Host
 *
 */
public class HostSettingException extends Exception
{
    //Message associated to the exception
    private String exceptionMessage = null;

    public HostSettingException(String exceptionMessage)
    {
        super(exceptionMessage);

        this.setExceptionMessage(exceptionMessage);
    }

    //Setters
    private void setExceptionMessage(String exceptionMessage){ this.exceptionMessage = exceptionMessage; }

    //Getters

    /**
     * Getter method used for retrive the associated exception message
     * @return exceptionMessage
     */
    public String getExceptionMessage(){return this.exceptionMessage; }
}
