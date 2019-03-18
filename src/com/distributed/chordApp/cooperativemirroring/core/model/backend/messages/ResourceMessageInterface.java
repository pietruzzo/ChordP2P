package com.distributed.chordApp.cooperativemirroring.core.model.backend.messages;

import com.distributed.chordApp.cooperativemirroring.core.model.Resource;
import com.distributed.chordApp.cooperativemirroring.core.model.backend.messages.codes.ResourceMessageType;

import java.io.Serializable;

/**
 * Interface that defines basic methods that netweork's messages classes must implement
 */
public interface ResourceMessageInterface extends Serializable {

    public String getOriginalSenderIP();
    public Integer getOriginalSenderPort();
    public String getCurrentSenderIP();
    public Integer getCurrentSenderPort();
    public String getResourceID();
    public Resource getResource();
    public ResourceMessageType getMessageType();
}
