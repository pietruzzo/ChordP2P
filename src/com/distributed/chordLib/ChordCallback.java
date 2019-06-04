package com.distributed.chordLib;

import java.io.IOException;

/**
 * Interface that can optionally be implemented by application using ChordLIB that contains callback methods
 * Asynchronous
 */
public interface ChordCallback {

    /**
     * Notify change in set of Keys the application is responsible for
     */
    void notifyResponsabilityChange ();
}
