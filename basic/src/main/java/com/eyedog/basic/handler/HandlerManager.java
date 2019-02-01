package com.eyedog.basic.handler;

import android.os.Looper;
import java.util.Hashtable;

/**
 * Created by walljiang on 2017/10/3.
 */

public class HandlerManager {

    private static volatile HandlerManager instance;

    private HandlerManager(){}

    Hashtable<String,WeakHandler> handlers;

    public static HandlerManager getInstance(){
        if(instance == null){
            synchronized (HandlerManager.class){
                if(instance == null){
                    instance = new HandlerManager();
                }
            }
        }
        return instance;
    }

    public void addHandler(Looper looper, HandlerCallback callback){
        if(looper != null && callback != null){
            WeakHandler handler = new WeakHandler(looper,callback);
            if(handlers == null){
                handlers = new Hashtable<>();
            }
            handlers.put(callback.hashCode() + "",handler);
        }
    }

    //多个handler之间共用Looper不会影响消息分发
    public void removeHandler(HandlerCallback callback){
        if(handlers != null){
            WeakHandler handler = handlers.get(callback.hashCode()+"");
            if(handler != null){
                handler.release();
            }
            handlers.remove(callback.hashCode()+"");
        }
    }

    public WeakHandler obtain(HandlerCallback callback){
        if(callback != null && handlers != null){
            return handlers.get(callback.hashCode()+"");
        }
        return null;
    }
}
