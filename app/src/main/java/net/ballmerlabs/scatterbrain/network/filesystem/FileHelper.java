package net.ballmerlabs.scatterbrain.network.filesystem;

import android.content.Context;
import android.os.Environment;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Manages files read and stored from sdcard
 */

public class FileHelper {
    public static final int LOCATION_EXTERNAL = 1;
    public static final int LOCATION_PRIVATE = 0;
    public String externalDir = "ScatterBrain";
    public String privateDir = "scatterbrainFiles";
    private Context context;

    public FileHelper(Context c) {
        this.context = c;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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
