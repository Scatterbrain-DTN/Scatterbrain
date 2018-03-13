package net.ballmerlabs.scatterbrain.network.API;

import java.util.UUID;

/**
 * Identifier for low level transport
 */

public interface ScatterTransport {
    UUID getUUID();
    String getNameString();
    int getPriority();
    void setPriority(int priority);
}
