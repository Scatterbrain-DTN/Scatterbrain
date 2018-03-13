package net.ballmerlabs.scatterbrain.network.filesystem;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

import net.ballmerlabs.scatterbrain.utils.ScatterLogManager;
import net.ballmerlabs.scatterbrain.datastore.Message;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.io.File;
import java.util.ArrayList;

/**
 * Manages files read and stored from sdcard
 */

public class FileHelper {
    private static final String TAG = "FileHelper";
    private static final int LOCATION_EXTERNAL = 1;
    public static final int LOCATION_PRIVATE = 0;
    public static final int SOURCE_STREAM = 2;
    public static final int SOURCE_FILE = 3;
    public String externalDir = "ScatterBrain";
    public String privateDir = "scatterbrainFiles";
    private final Context context;
    private final NetTrunk trunk;

    public FileHelper(Context c, NetTrunk t) {
        this.context = c;
        this.trunk = t;
    }


    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public BlockDataPacket bdPacketFromFilename(String name) {
        BlockDataPacket bd = bdPacketFromFilename(name, LOCATION_PRIVATE);
        if(bd == null)
            bd = bdPacketFromFilename(name, LOCATION_EXTERNAL);
        if(bd == null)
            return null;

        return bd;
    }

    private BlockDataPacket bdPacketFromFilename(String name, int location) {
        if(location != LOCATION_EXTERNAL && location != LOCATION_PRIVATE)
            return null;

        File f = new File(getDirectory(location).getAbsolutePath() + name);
        if(!f.exists())
            return null;

        ArrayList<Message> res = trunk.mainService.dataStore.getMessageByFilename(name);

        if(res.size() != 1) {
            ScatterLogManager.e(TAG, "Datastore discrepancy: multilink file");
            return null;
        }

        Message m =  res.get(0);

        if(m.filename == null)
            return null;

        BlockDataPacket bd = new BlockDataPacket(f,f.getName(),f.length(),
                Base64.decode(m.senderluid, Base64.DEFAULT));

        if(bd.isInvalid())
            return null;

        return bd;

    }

    private File getDirectory(int location) {
        if(location != LOCATION_EXTERNAL && location != LOCATION_PRIVATE)
            return null;

        if(location == LOCATION_PRIVATE)
            return   context.getFilesDir();
        else {
            return Environment.getExternalStorageDirectory();
        }
    }


    /* warning: this is blocking */
    public byte[] writeBlockDataPacket(BlockDataPacket bd, int location, int source) {

        if(source != SOURCE_FILE && source != SOURCE_STREAM)
            return null;

        if(!isExternalStorageWritable())
            return null;
        File locfile = getDirectory(location);
        if(locfile == null)
            return null;
        String locationdir = locfile.getAbsolutePath();

        if(bd.getFilename() != null ) {
            File out = new File(locationdir + "/" + bd.getFilename());
            int counter = 0;
            while (out.exists()) {
                out = new File(locationdir
                        + "/" +bd.getFilename() + "." + counter++);
            }


            if(source == SOURCE_STREAM)
                bd.catBody(out);
            else if(source == SOURCE_FILE)
                bd.catFile(out);
        } else {
            return null;
        }

        return bd.streamhash;
    }
}
