package net.ballmerlabs.scatterbrain;

/**
 * Wrapper for a single message object in main UI
 */
public class DispMessage {
    public final String header;
    public final String body;

    public DispMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }
}
