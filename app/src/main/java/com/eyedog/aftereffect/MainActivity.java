package com.eyedog.aftereffect;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.eyedog.aftereffect.DashLine.DashLineView;

public class MainActivity extends AppCompatActivity {

    ControllableContainer mContainer;
    DashLineView mDashLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = findViewById(R.id.container);
        mDashLineView = findViewById(R.id.dashline_view);
    }

    public void addText(View view) {
        TestTextView textView = new TestTextView(this);
        textView.setLayoutParams(new FrameLayout.LayoutParams(300, 200));
        textView.setText("test");
        textView.setTextSize(12);
        textView.setBackgroundColor(Color.parseColor("#99ff0000"));
        mContainer.addView(textView);
        mContainer.setDashLineView(mDashLineView);
    }
}
