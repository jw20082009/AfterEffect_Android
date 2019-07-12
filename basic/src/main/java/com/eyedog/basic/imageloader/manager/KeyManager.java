package com.eyedog.basic.imageloader.manager;

import android.util.SparseArray;

/**
 * Created by yuanyang on 17/8/4.
 */

public class KeyManager {

    private static KeyManager instance;

    public static KeyManager getInstance(){
        if (instance == null){
            instance = new KeyManager();
        }
        return instance;
    }

    private SparseArray<String> keyMaps = new SparseArray<>();

    public void putKeyMaps(Integer hash,String key){
        keyMaps.put(hash,key);
    }

    public String getKey(Integer hash){
        return keyMaps.get(hash);
    }

    public void removeKey(Integer hash){
        keyMaps.remove(hash);
    }

}
