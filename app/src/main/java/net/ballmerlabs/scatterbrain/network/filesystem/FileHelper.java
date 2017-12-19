package net.ballmerlabs.scatterbrain.network.filesystem;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.datastore.Message;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Manages files read and stored from sdcard
 */

public class FileHelper {
    public static final String TAG = "FileHelper";
    public static final int LOCATION_EXTERNAL = 1;
    public static final int LOCATION_PRIVATE = 0;
    public String externalDir = "ScatterBrain";
    public String privateDir = "scatterbrainFiles";
    private Context context;
    private NetTrunk trunk;

    public FileHelper(Context c, NetTrunk t) {
        this.context = c;
        this.trunk = t;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public BlockDataPacket bdPacketFromFilename(String name) {
        BlockDataPacket bd = bdPacketFromFilename(name, LOCATION_PRIVATE);
        if(bd == null)
            bd = bdPacketFromFilename(name, LOCATION_EXTERNAL);
        if(bd == null)
            return null;

        return bd;
    }

    public BlockDataPacket bdPacketFromFilename(String name, int location) {
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

    public File getDirectory(int location) {
        if(location != LOCATION_EXTERNAL && location != LOCATION_PRIVATE)
            return null;

        if(location == LOCATION_PRIVATE)
            return context.getFilesDir();
        else {
            return Environment.getExternalStorageDirectory();
        }
    }


    /* warning: this is blocking */
    public boolean writeBlockDataPacket(BlockDataPacket bd, int location) {

        if(!isExternalStorageWritable())
            return false;
        File locfile = getDirectory(location);
        if(locfile == null)
            return false;
        String locationdir = locfile.getAbsolutePath();

        if(bd.getFilename() != null ) {
            File out = new File(locationdir + bd.getFilename());
            int counter = 0;
            while (out.exists()) {
                out = new File(locationdir
                        + bd.getFilename() + "." + counter++);
            }

            try {
                FileOutputStream fo = new FileOutputStream(out);
                bd.catBody(fo);
            } catch(IOException e) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }


}
