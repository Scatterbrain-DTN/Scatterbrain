package net.ballmerlabs.scatterbrain.datastore;

/**
 * Basic data unit for a single message. 
 */
@SuppressWarnings("FieldCanBeLocal")
public class Message {



    public final String application;
    public final String body;
    @SuppressWarnings("unused")
    private final int ttl;
    @SuppressWarnings("unused")
    private final String replylink;
    public final String senderluid;
    @SuppressWarnings("unused")
    private final String flags;
    @SuppressWarnings("unused")
    private final String sig;
    @SuppressWarnings("unused")
    private final String uuid;
    @SuppressWarnings("unused")
    private final String receiverluid;
    @SuppressWarnings("unused")
    private final int extbody;
    @SuppressWarnings("unused")
    private final int text;

    private final int file;

    public final String filename;

    public Message(String uuid, int extbody,   String body, String application, int text, int file,  int ttl,
                   String replyto, String luid, String receiverLuid,
                   String sig, String flags, String filename){
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
        this.file = file;
        this.filename = filename;
    }


}
