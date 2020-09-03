package io.foundation21.smartvenue;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class ChartDoors extends Drawable {

    private static final String TAG = "ChartDoors";
    static long MIN_TIMERANGE = 1000 * 60 * 5;

    Vector<PointF> points;
    long minTime = Long.MAX_VALUE;
    long maxTime = Long.MIN_VALUE;
    int minCounter = Integer.MAX_VALUE;
    int maxCounter = Integer.MIN_VALUE;

    public ChartDoors(long[] timestamps, int[] counters){
        for (int i=0; i<timestamps.length;i++){
            minTime=Math.min(minTime,timestamps[i]);
            maxTime=Math.max(maxTime,timestamps[i]);
            minCounter=Math.min(minCounter,counters[i]);
            maxCounter=Math.max(maxCounter,counters[i]);
        }
        float timeRange=Math.max(maxTime-minTime,MIN_TIMERANGE);
        float counterRange=maxCounter-minCounter;
        points = new Vector();
        for (int i=0; i<timestamps.length;i++) {
            float x = ((float)(timestamps[i]-minTime))/timeRange;
            float y = ((float)(counters[i]-minCounter))/counterRange;
            points.add(new PointF(x,y));
        }
    }

    public void draw(@NonNull Canvas canvas) {
        Paint graphPaint = new Paint();
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setColor(Color.BLUE);
        graphPaint.setStrokeWidth(12);
        graphPaint.setTextSize(50);
        graphPaint.setFakeBoldText(false);

        Paint txtPaint = new Paint();
        txtPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setStrokeWidth(4);
        txtPaint.setColor(Color.DKGRAY);
        txtPaint.setTextSize(50);
        txtPaint.setFakeBoldText(true);

        float bottom = canvas.getHeight()-80;
        float right =  canvas.getWidth()-80;
        float left = 0;
        float top = 0;

        Path graph = new Path();
        for (PointF point : points){
            float x = point.x * right;
            float y = (1-point.y) * bottom;
            if (graph.isEmpty()) graph.moveTo(x,y); else graph.lineTo(x,y);
        }
        canvas.drawRect(left,top,right,bottom,txtPaint);
        canvas.drawPath(graph,graphPaint);

        String label = String.valueOf(minCounter);
        canvas.drawText(label,canvas.getWidth()-txtPaint.measureText(label),bottom,txtPaint);

        label = String.valueOf(maxCounter);
        canvas.drawText(label,canvas.getWidth()-txtPaint.measureText(label),5+txtPaint.getTextSize(),txtPaint);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        label = sdf.format(new Date(minTime));
        canvas.drawText(label,left,bottom+txtPaint.getTextSize()+10,txtPaint);

        label = sdf.format(new Date(maxTime));
        canvas.drawText(label,right-txtPaint.measureText(label),bottom+txtPaint.getTextSize()+10,txtPaint);
    }

    public void setAlpha(int i) { }
    public void setColorFilter(@Nullable ColorFilter colorFilter) { }
    @SuppressLint("WrongConstant")
    public int getOpacity() { return 0;}
}
