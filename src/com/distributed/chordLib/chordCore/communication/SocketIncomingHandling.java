package com.distributed.chordLib.chordCore.communication;

import com.distributed.chordLib.chordCore.communication.messages.Message;

public interface SocketIncomingHandling {

    /**
     * register a mew message from a SocketNode
     * @param node
     * @param message
     */
    void handleNewMessage(Message message, SocketNode node);
}
