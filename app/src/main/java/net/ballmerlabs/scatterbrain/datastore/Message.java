package net.ballmerlabs.scatterbrain.datastore;

/**
 * Basic data unit for a single message. 
 */
public class Message {



    public String application;
    public String body;
    public int ttl;
    public String replylink;
    public String senderluid;
    public String flags;
    public String sig;
    public String uuid;
    public String receiverluid;
    public int extbody;
    public int text;

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
