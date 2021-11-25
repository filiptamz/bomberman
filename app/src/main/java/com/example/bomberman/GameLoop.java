package com.example.bomberman;

import android.graphics.Canvas;
import android.media.audiofx.Visualizer;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Observer;

public class GameLoop extends Thread
{
    private static final double MAX_UPS = 60.0;
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private Game game;
    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder)
    {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
    }

    public double getAverageUPS()
    {
        return averageUPS;
    }

    public double getAverageFPS()
    {
        return averageFPS;
    }

    public void startLoop()
    {
        isRunning = true;
        start();
    }

    @Override
    public void run()
    {
        super.run();

        int updateCount = 0;
        int frameCount = 0;

        long startTime;
        long elapsedTime;
        long sleepTime;
        
        // Game Loop
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while(isRunning)
        {
            try
            {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder)
                {
                    game.update();
                    updateCount++;
                    game.draw(canvas);
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(canvas != null)
                {
                    try
                    {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            // Pause game loop to not exceed target UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long) (updateCount*UPS_PERIOD - elapsedTime);
            if(sleepTime > 0)
            {
                try
                {
                    sleep(sleepTime);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            //Skip frames to keep up with UPS
            while(sleepTime < 0 && updateCount < MAX_UPS - 1)
            {
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (updateCount*UPS_PERIOD - elapsedTime);
            }

            //Calculate FPS and UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime >= 1000)
            {
                averageUPS = updateCount / (elapsedTime / 1000);
                averageFPS = frameCount / (elapsedTime / 1000);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }
}
