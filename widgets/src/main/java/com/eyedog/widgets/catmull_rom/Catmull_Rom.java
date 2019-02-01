package com.eyedog.widgets.catmull_rom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;

public class Catmull_Rom extends View {
    private final Paint mGesturePaint = new Paint();
    private final Path mPath = new Path();

    private ArrayList<Point> point = new ArrayList<Point>();
    private ArrayList<Point> save = new ArrayList<Point>();

    public Catmull_Rom(Context context) {
        super(context);
    }

    public Catmull_Rom(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Style.STROKE);
        mGesturePaint.setStrokeWidth(5);
        mGesturePaint.setColor(Color.WHITE);
    }

    public Catmull_Rom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        for (int i = 0; i < 10; i++) {
            float x = i * 100;
            float y = (float) (Math.random() * 100 + 300);
            Log.i("catmull_room", "x:" + x + ";y:" + y);
            point.add(new Point(x, y));
        }

        function_Catmull_Rom(point, 1000, save, mPath);
        canvas.drawPath(mPath, mGesturePaint);
    }

    public void function_Catmull_Rom(ArrayList<Point> point, int cha, ArrayList<Point> save,
        Path path) {
        if (point.size() < 4) {
            return;
        }
        path.moveTo(point.get(0).x, point.get(0).y);
        save.add(point.get(0));
        for (int index = 1; index < point.size() - 2; index++) {
            Point p0 = point.get(index - 1);
            Point p1 = point.get(index);
            Point p2 = point.get(index + 1);
            Point p3 = point.get(index + 2);

            for (int i = 1; i <= cha; i++) {
                float t = i * (1.0f / cha);
                float tt = t * t;
                float ttt = tt * t;

                Point pi = new Point(); // intermediate point
                pi.x = (float) (0.5 * (2 * p1.x
                    + (p2.x - p0.x) * t
                    + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tt
                    + (3 * p1.x - p0.x - 3 * p2.x + p3.x)
                    * ttt));
                pi.y = (float) (0.5 * (2 * p1.y
                    + (p2.y - p0.y) * t
                    + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tt
                    + (3 * p1.y - p0.y - 3 * p2.y + p3.y)
                    * ttt));
                path.lineTo(pi.x, pi.y);
                save.add(pi);
                pi = null;
            }
        }
        path.lineTo(point.get(point.size() - 1).x, point.get(point.size() - 1).y);
        save.add(point.get(point.size() - 1));
    }
}

