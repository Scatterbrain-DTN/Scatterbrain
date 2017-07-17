package net.ballmerlabs.scatterbrain.datastore;

/**
 * Basic data unit for a single message. 
 */
@SuppressWarnings("FieldCanBeLocal")
public class Message {



    public final String application;
    public final String body;
    private final int ttl;
    private final String replylink;
    public final String senderluid;
    private final String flags;
    private final String sig;
    private final String uuid;
    private final String receiverluid;
    private final int extbody;
    private final int text;

    public Message(String uuid, int extbody,   String body, String application, int text,  int ttl,
                   String replyto, String luid, String receiverLuid,
                   String sig, String flags){
        this.application = application;
        this.body = body;
        this.ttl = ttl;
        this.replylink = replyto;
        this.senderluid = luid;
        this.flags = flags;
        this.sig = sig;
        this.uuid = uuid;
        this.receiverluid = receiverLuid;
        this.extbody = extbody;
        this.text = text;
    }


}
