package com.distributed.chordLib.chordCore;

/**
 * Interface that can optionally be implemented by application using ChordLIB that contains callback methods
 */
public interface ChordNetworkCallback {

    /**
     * Notify change in set of Keys the application is responsible
     */
    void notifyResponsabilityChange ();
}
