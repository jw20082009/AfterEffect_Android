package com.eyedog.basic.imageloader.manager;

import com.eyedog.basic.imageloader.ImageRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuanyang on 17/8/4.
 */

public class RequestManager {

    private Map<Integer, ImageRequest> requestMaps = Collections.synchronizedMap(new HashMap<Integer, ImageRequest>());

    public void putRequest(int hash,ImageRequest request){
        requestMaps.put(hash,request);
    }

    public ImageRequest get(int hash){
        return requestMaps.get(hash);
    }

    public ImageRequest remove(int hash){
        if (hash == -1)return null;
        return requestMaps.remove(hash);
    }
}
