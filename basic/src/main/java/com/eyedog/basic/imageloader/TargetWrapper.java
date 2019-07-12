package com.eyedog.basic.imageloader;

import java.lang.ref.WeakReference;

public class TargetWrapper<T> {

    private WeakReference<T> ref;

    public TargetWrapper(T target) {
        ref = new WeakReference<>(target);
    }

    public T get() {
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    @Override
    public int hashCode() {
        if (ref == null || ref.get() == null) return 0;
        return ref.get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof TargetWrapper) {
            TargetWrapper wrapper = (TargetWrapper) obj;
            if (wrapper.get() == null || get() == null) {
                return false;
            } else {
                return wrapper.get().equals(get());
            }
        }
        return false;
    }
}
