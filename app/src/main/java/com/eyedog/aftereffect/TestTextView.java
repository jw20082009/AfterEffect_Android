package com.eyedog.aftereffect;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * created by jw200 at 2018/12/7 16:44
 **/
public class TestTextView extends android.support.v7.widget.AppCompatTextView {
    public TestTextView(Context context) {
        super(context);
    }

    public TestTextView(Context context,
        @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTestText() {
        StringBuilder builder = new StringBuilder();
        builder.append("rotation:");
        builder.append(getRotation());
        builder.append(";");
        builder.append("transX:");
        builder.append(getTranslationX());
        builder.append(";transY:");
        builder.append(getTranslationY());
        setText(builder.toString());
    }
}
