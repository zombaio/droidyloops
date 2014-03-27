package com.example.droidyloops.dloops;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by sid9102 on 3/16/14.
 */


public class GridView extends SurfaceView implements SurfaceHolder.Callback
{
    private PanelThread panelThread;
    private int height;
    private int width;

    private boolean play;

    private Paint gridPaint;
    private Paint hlPaint;
    private Paint squarePaint;

    public boolean[][] grid = new boolean[8][4];
    public int[] sampleIDs = new int[4];

    private float[] hlPos;

    // The time between beats, in milliseconds
    public int beatTime;

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

        Log.v("surface", "created");
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

        squarePaint = new Paint();
        squarePaint.setStyle(Paint.Style.FILL);
        squarePaint.setColor(0xe0ffb400);

        hlPos = new float[4];

        beatTime = 500;
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
//            Log.v("width", Integer.toString(width));
//            Log.v("height", Integer.toString(height));
        }

        float rowHeight = (float)height / 4;
        float colWidth = (float)width / 9;

        // fill in the background
        canvas.drawColor(0xfc0099cc);

        gridPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, colWidth, height, gridPaint);
        gridPaint.setStyle(Paint.Style.STROKE);

        // Draw highlight
        if(play)
        {
            hlPos[1] = 2;
            hlPos[3] = height - 2;
            canvas.drawRect(hlPos[0], hlPos[1], hlPos[2], hlPos[3], hlPaint);
        }

        // White rectangle on the left
        canvas.drawRect(2, 2, width - 1, height - 1, gridPaint);

        for(int i = 1; i < 9; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(grid[i - 1][j]) {
                    canvas.drawRect(colWidth * i, rowHeight * j, colWidth * (i +1), rowHeight * (j + 1), squarePaint);
                }
            }
        }

        // Draw vertical lines
        for(int i = 1; i < 9; i++)
        {
            canvas.drawLine(colWidth * i, 2, colWidth * i, height - 1, gridPaint);
        }

        // Draw horizontal lines
        for(int i = 1; i < 4; i++)
        {
            canvas.drawLine(2, rowHeight * i, width - 1, rowHeight * i, gridPaint);
        }

    }

    public void playStop()
    {
        play = !play;
        float colWidth = (float)width / 9;
        hlPos[0] = 0;
        hlPos[2] = colWidth;
    }

    public void changeBPM(int bpm)
    {
        beatTime = 1000 / (bpm / 60);
    }

    public void incrementHL()
    {
        float colWidth = (float)width / 9;

        if(hlPos[2] < width)
        {
            hlPos[0] += colWidth;
            hlPos[2] += colWidth;
        }
        else
        {
            hlPos[0] = colWidth;
            hlPos[2] = colWidth * 2;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float rowHeight = (float)height / 4;
        float colWidth = (float)width / 9;
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float x = event.getX();
            float y = event.getY();
            if(x > colWidth)
            {
                synchronized (grid) {
                    x -= x % colWidth;
                    y -= y % rowHeight;
                    int col = (int) (x / colWidth) - 1;
                    int row = (int) (y / rowHeight);
                    Log.v("row", Integer.toString(row));
                    Log.v("col", Integer.toString(col));
                    boolean found = grid[col][row];
                    grid[col][row] = !found;

                    if (!found) {
                        LooperActivity host = (LooperActivity) this.getContext();
                        if (host != null) {
                            host.playSound(row);
                        }
                    }
                }
            }
        }
        return true;
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
