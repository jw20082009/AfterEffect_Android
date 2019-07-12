
package com.eyedog.basic.imageloader;

/**
 * Created by yuanyang on 17/8/2.
 */

public class ImageRequest {

    private ResizeOptions mResizeOptions;

    private String path;

    private boolean inMutable;

    private boolean hasCancel = false;

    public ImageRequest(ImageRequestBuilder builder) {
        mResizeOptions = builder == null ? null : builder.getResizeOptions();
        path = builder == null ? null : builder.getPath();
        inMutable = builder == null ? false : builder.getMutable();
    }

    public void cancel() {
        hasCancel = true;
    }

    public boolean hasCancel() {
        return hasCancel;
    }

    public ResizeOptions getResizeOptions() {
        return mResizeOptions;
    }

    public void setResizeOptions(ResizeOptions resizeOptions) {
        mResizeOptions = resizeOptions;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setInMutable(boolean inMutable){
        this.inMutable = inMutable;
    }

    public boolean getMutable(){
        return this.inMutable;
    }

    public static class ImageRequestBuilder {

        ResizeOptions mResizeOptions;

        String path;

        boolean inMutable = false;

        public ResizeOptions getResizeOptions() {
            return mResizeOptions;
        }

        public void setResizeOptions(ResizeOptions resizeOptions) {
            mResizeOptions = resizeOptions;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public ImageRequestBuilder resizeOptions(ResizeOptions opts) {
            this.mResizeOptions = opts;
            return this;
        }

        public ImageRequestBuilder inMutable() {
            inMutable = true;
            return this;
        }

        public boolean getMutable() {
            return inMutable;
        }

        public ImageRequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ImageRequest build() {
            return new ImageRequest(this);
        }

    }
}
