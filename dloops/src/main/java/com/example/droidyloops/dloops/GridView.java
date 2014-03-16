package com.example.droidyloops.dloops;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by sid9102 on 3/16/14.
 */


public class GridView extends SurfaceView implements SurfaceHolder.Callback
{
    PanelThread panelThread;
    int height;
    int width;

    Paint gridPaint;
    Paint hlPaint;

    float[] hlPos;

    public GridView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()

        panelThread = new PanelThread(getHolder(), this); //Start the thread that
        panelThread.setRunning(true);                     //will make calls to
        panelThread.start();                              //onDraw()

        // The paint we use to draw the grid
        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2.0f);
        gridPaint.setColor(Color.WHITE);

        // Highlight paint
        hlPaint = new Paint();
        hlPaint.setStyle(Paint.Style.FILL);
        hlPaint.setColor(0xffff2800);

        hlPos = new float[4];
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            panelThread.setRunning(false);                //Tells thread to stop
            panelThread.join();                           //Removes thread from mem.
        } catch (InterruptedException e) {}
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(width == 0 || height == 0)
        {
            width = canvas.getWidth();
            height = canvas.getHeight();
            Log.v("width", Integer.toString(width));
            Log.v("height", Integer.toString(height));
        }

        float rowHeight = (float)height / 6;
        float colWidth = (float)width / 9;

        // fill in the background
        canvas.drawColor(0xff0099cc);

        gridPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, rowHeight, colWidth, height - rowHeight, gridPaint);
        gridPaint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(2, rowHeight, width - 1, height - rowHeight, gridPaint);

        // Draw vertical lines
        for(int i = 1; i < 9; i++)
        {
            canvas.drawLine(colWidth * i, rowHeight, colWidth * i, height - rowHeight, gridPaint);
        }

        // Draw horizontal lines
        for(int i = 1; i < 5; i++)
        {
            canvas.drawLine(2, rowHeight * i, width - 1, rowHeight * i, gridPaint);
        }

    }

    class PanelThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private GridView mGridView;
        private boolean _run = false;


        public PanelThread(SurfaceHolder surfaceHolder, GridView view) {
            _surfaceHolder = surfaceHolder;
            mGridView = view;
        }


        public void setRunning(boolean run) { //Allow us to stop the thread
            _run = run;
        }


        @Override
        public void run() {
            Canvas c;
            while (_run) {     //When setRunning(false) occurs, _run is
                c = null;      //set to false and loop ends, stopping thread


                try {


                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {


                        //Insert methods to modify positions of items in onDraw()
                        postInvalidate();


                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}
