package net.ballmerlabs.scatterbrain;

/**
 * Wrapper for a single message object in main UI
 */
public class DispMessage {
    public String header;
    public String body;

    public DispMessage(String header, String body) {
        this.header = header;
        this.body = body;
    }
}
