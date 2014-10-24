package io.github.omkarmoghe.colorblind;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

/**
 * Created by Omkar on 5/20/2014.
 *
 */
public class GameLoopThread extends Thread {
    private GameView view;
    private boolean running = false;
    private static final long FPS = 30;

    public GameLoopThread(GameView view) {
        this.view = view;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        long tPS = 1000 / FPS; //ticksPerSecond is 1000ms (1sec) divided by the FramesPerSecond.
        long startTime;
        long sleepTime;

        while(running) {
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    view.onDraw(c);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                if(c != null){
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }

            sleepTime = tPS - (System.currentTimeMillis() - startTime);
            try {
                if(sleepTime > 0)
                    sleep(sleepTime);
                else
                    sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //End run.
    } //End method.

    public void setRunning(boolean b) {
        running = b;
    }

}
