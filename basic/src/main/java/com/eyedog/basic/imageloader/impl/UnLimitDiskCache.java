package com.eyedog.basic.imageloader.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eyedog.basic.imageloader.inter.DiskCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yuanyang on 17/8/2.
 */

public class UnLimitDiskCache implements DiskCache {

    private static final int DEFAULT_COMPRESS_QUALITY = 100;

    private static final int DEFAULT_BUFFER_SIZE = 32*1024;

    private String cacheDir;

    private static final String SUFFIX_CACHE_FILE = ".tmp";

    public UnLimitDiskCache(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    @Override
    public synchronized Bitmap get(String key) {
        File file = new File(cacheDir,generateFileName(key)+SUFFIX_CACHE_FILE);
        if (!file.exists())return null;
        try {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inMutable = true;
            return BitmapFactory.decodeFile(file.getAbsolutePath(),op);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized void put(String key, Bitmap bitmap) throws IOException {
        File cacheDirFile = new File(cacheDir);
        if (cacheDirFile.exists() || ((!cacheDirFile.exists())&&cacheDirFile.mkdirs())){
            File cacheFile = new File(cacheDirFile,generateFileName(key)+SUFFIX_CACHE_FILE);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(cacheFile), DEFAULT_BUFFER_SIZE);
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_COMPRESS_QUALITY, os);
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void clear() {
        File cacheDirFile = new File(cacheDir);
        if (cacheDirFile.exists()){
            cacheDirFile.delete();
        }
    }

    @Override
    public String generateFileName(String key) {
        return String.valueOf(key.hashCode());
    }


}
