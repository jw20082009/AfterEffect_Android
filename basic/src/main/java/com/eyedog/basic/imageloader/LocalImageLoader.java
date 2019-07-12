
package com.eyedog.basic.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.eyedog.basic.BuildConfig;
import com.eyedog.basic.imageloader.impl.DefaultMemoryCache;
import com.eyedog.basic.imageloader.impl.LruMemoryCache;
import com.eyedog.basic.imageloader.impl.UnLimitDiskCache;
import com.eyedog.basic.imageloader.inter.DiskCache;
import com.eyedog.basic.imageloader.inter.MemoryCache;
import com.eyedog.basic.imageloader.manager.KeyManager;
import com.eyedog.basic.imageloader.manager.RequestManager;
import com.eyedog.basic.imageloader.util.KeyGeneratorUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yuanyang on 17/8/2.
 */

/**
 * 本地图片加载，压缩，居中裁剪 适合用于列表页的本地图片加载
 */
public class LocalImageLoader {

    private final LruMemoryCache mLruMemoryCache;

    private static LocalImageLoader loader;

    private MemoryCache mMemoryCache;

    private DiskCache mDiskCache;

    private ExecutorService mExecutor;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private RequestManager mRequestManager;

    public static LocalImageLoader getInstance(Context c) {
        if (loader == null) {
            synchronized (LocalImageLoader.class) {
                if (loader == null) {
                    loader = new LocalImageLoader(c);
                }
            }
        }
        return loader;
    }

    private LocalImageLoader(Context context) {
        Context c = context.getApplicationContext();
        String imageCachePath = c.getExternalCacheDir() + "/local_image";
        mMemoryCache = new DefaultMemoryCache();
        mDiskCache = new UnLimitDiskCache(imageCachePath);
        mExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ImageLoadFactory());
        this.mRequestManager = new RequestManager();
        this.mLruMemoryCache = new LruMemoryCache();
    }

    public void clearDiskCache() {
        mDiskCache.clear();
    }

    public void loadImageBitmap(ImageRequest request, ILocalBitmapListener listener) {
        if (request == null || listener == null)
            return;
        String key = KeyGeneratorUtil.generateKey(request);

        KeyManager.getInstance().putKeyMaps(listener.hashCode(), key);
        // load lrucache
        Bitmap lruBitmap = mLruMemoryCache.get(key);
        if (lruBitmap != null) {
            listener.onLoadResult(lruBitmap);
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "hit lru cache");
            }
            return;
        }

        // load memory cache
        Bitmap b = mMemoryCache.get(key);
        if (b != null) {
            Log.i("LocalLoader", "memory");
            listener.onLoadResult(b);
            return;
        }
        // cancel prior request
        ImageRequest waitingRequest = mRequestManager.get(listener.hashCode());

        TargetWrapper<ILocalBitmapListener> wrapper = new TargetWrapper<>(listener);
        if (waitingRequest != null) {
            waitingRequest.cancel();
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "cancel a task");
            }
        }

        mRequestManager.putRequest(listener.hashCode(), request);
        mExecutor.execute(new ImageLoadTask(mRequestManager, request, wrapper, mDiskCache,
                mMemoryCache, mLruMemoryCache, mainHandler));
    }

    public void loadImageBitmapSync(ImageRequest request, ILocalBitmapListener listener) {
        if (request == null || listener == null)
            return;
        String key = KeyGeneratorUtil.generateKey(request);

        KeyManager.getInstance().putKeyMaps(listener.hashCode(), key);
        // load lrucache
        Bitmap lruBitmap = mLruMemoryCache.get(key);
        if (lruBitmap != null) {
            listener.onLoadResult(lruBitmap);
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "hit lru cache");
            }
            return;
        }

        // load memory cache
        Bitmap b = mMemoryCache.get(key);
        if (b != null) {
            Log.i("LocalLoader", "memory");
            listener.onLoadResult(b);
            return;
        }
        // cancel prior request
        ImageRequest waitingRequest = mRequestManager.get(listener.hashCode());

        TargetWrapper<ILocalBitmapListener> wrapper = new TargetWrapper<>(listener);
        if (waitingRequest != null) {
            waitingRequest.cancel();
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "cancel a task");
            }
        }

        mRequestManager.putRequest(listener.hashCode(), request);
        new ImageLoadTask(mRequestManager, request, wrapper, mDiskCache, mMemoryCache,
                mLruMemoryCache, mainHandler).run();
    }

    public void displayImage(ImageRequest request, ImageView imageView) {
        if (request == null || imageView == null)
            return;
        String key = KeyGeneratorUtil.generateKey(request);

        KeyManager.getInstance().putKeyMaps(imageView.hashCode(), key);
        // load lrucache
        Bitmap lruBitmap = mLruMemoryCache.get(key);
        if (lruBitmap != null) {
            imageView.setImageBitmap(lruBitmap);
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "hit lru cache");
            }
            return;
        }

        // load memory cache
        Bitmap b = mMemoryCache.get(key);
        if (b != null) {
            Log.i("LocalLoader", "memory");
            imageView.setImageBitmap(b);
            return;
        }

        // set placeholder
        imageView.setImageDrawable(new ColorDrawable(Color.WHITE));

        ImageViewWrapper wrapper = new ImageViewWrapper(imageView);
        // cancel prior request
        ImageRequest waitingRequest = mRequestManager.get(imageView.hashCode());
        if (waitingRequest != null) {
            waitingRequest.cancel();
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "cancel a task");
            }
        }

        mRequestManager.putRequest(imageView.hashCode(), request);
        mExecutor.execute(new ImageLoadTask(mRequestManager, request, wrapper, mDiskCache,
                mMemoryCache, mLruMemoryCache, mainHandler));
    }

    public void clear() {
        if (mMemoryCache != null) {
            mMemoryCache.clear();
        }
    }

    public interface ILocalBitmapListener {
        void onLoadResult(Bitmap bitmap);
    }

    private static class ImageLoadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(Thread.currentThread().getThreadGroup(), r,
                    "local image load thread-" + threadNumber.getAndIncrement(), 0);
        }
    }
}
