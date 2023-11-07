package com.akinyildirim.paintapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<Stroke> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaint.setAlpha(1);
    }

    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = Color.BLACK;
        strokeWidth = 20;
    }

    public void setColor(int color) {
        currentColor = color;
    }

    public int getColor(){
        return currentColor;
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void undo() {
        if (paths.size() != 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }
    public boolean isEmpty(){
        return paths.size() == 0;
    }
    public void clear(){
        if(paths.size() != 0){
            paths.clear();
            invalidate();
        }
    }

    public Bitmap save() {
        //save view to gallery
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String fileName = "PaintApp" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Log.e("save", "save: " + file.getAbsolutePath());
        } catch (java.io.IOException e) {
            e.printStackTrace();

        }
        Toast.makeText(getContext(), "Saved to gallery", Toast.LENGTH_SHORT).show();
        return mBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);

        for (Stroke fp : paths) {
            mPaint.setColor(fp.getColor());
            mPaint.setStrokeWidth(fp.getStrokeWidth());
            mCanvas.drawPath(fp.getPath(), mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }
    private void touchStart(float x, float y) {
        mPath = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, mPath);
        paths.add(fp);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }
    public static List<DrawStartListener> drawStartListeners = new ArrayList<>();

    public void addDrawStartListener(DrawStartListener listener) {
        drawStartListeners.add(listener);
    }
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                for (DrawStartListener listener : drawStartListeners) {
                    listener.onDrawStart();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}