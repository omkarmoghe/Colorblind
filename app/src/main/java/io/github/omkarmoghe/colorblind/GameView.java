package io.github.omkarmoghe.colorblind;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Omkar Moghe, Joshua Komala, and Raymond Hsu on 5/20/2014.
 * Colorblind v1.1 alpha
 *
 * Game modes:
 * 1 - Classic - how fast can you cross 25 tiles
 * 2 - Marathon - every 10 rows get 5 seconds
 */
public class GameView extends SurfaceView {

    private SurfaceHolder                     holder;
    private io.github.omkarmoghe.colorblind.GameLoopThread gameLoopThread;

    //Paints
    private Paint red, blue, yellow, green;
    private ArrayList<Paint> paints;
    private Paint            r1p, r2p, r3p, r4p, r5p, r6p, r7p, r8p;
    private Paint cbcolor;
    private Paint black33, black50, black75, white33, white50, white100, green38, red38;

    //Rectangles
    private Rect r1, r2, r3, r4, r5, r6, r7, r8;
    private ArrayList<Rect> rectangles = new ArrayList<Rect>();

    //Game Booleans
    private boolean initialSets  = false;
    private boolean gameOver     = false;
    private boolean timerStarted = false;
    private boolean rectPainted  = false;
    private boolean menu         = true;
    private int gameMode;

    //Misc
    private Random g = new Random();

    //Dimensions
    private float row11left, row11right, row12left, row12right, row13left, row13right, row14left, row14right;
    private float row1top, row1bottom, row2top, row2bottom;
    private float cbleft, cbtop, cbright, cbbottom; // Block
    private float cbRadius; // Circle
    private float replayX, replayY, quitX, quitY;

    //Display Texts (Score, time, etc.)
    private int score = 0;
    private int bonusTime = 0;

    //Timer
    private Timer timer;
    private double ms;
    private DecimalFormat df = new DecimalFormat("##.###");

    public GameView(Context context) {
        super(context);
        gameLoopThread = new io.github.omkarmoghe.colorblind.GameLoopThread(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @SuppressLint("WrongCall")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * Initializes all the paint classes to be used later for drawing the rectangles.
     */
    private void initializePaint() {
        paints = new ArrayList<Paint>();
        red = new Paint();
        blue = new Paint();
        green = new Paint();
        yellow = new Paint();

        red.setColor(Color.rgb(234, 15, 114));
        blue.setColor(Color.rgb(72, 150, 227));
        green.setColor(Color.rgb(14, 248, 149));
        yellow.setColor(Color.rgb(248, 239, 82));

        paints.add(red);
        paints.add(blue);
        paints.add(green);
        paints.add(yellow);

        cbcolor = paints.get(g.nextInt(4)); //FOR intial assignment. instantialized in getNextColor()

        //Text Color
        black50 = new Paint(Color.BLACK);
        black50.setTextSize(50 * getResources().getDisplayMetrics().density);
        black50.setTextAlign(Paint.Align.CENTER);

        black75 = new Paint(Color.BLACK);
        black75.setTextSize(75 * getResources().getDisplayMetrics().density);
        black75.setTextAlign(Paint.Align.CENTER);

        black33 = new Paint(Color.BLACK);
        black33.setTextSize(33 * getResources().getDisplayMetrics().density);
        black33.setTextAlign(Paint.Align.CENTER);

        white50 = new Paint();
        white50.setColor(Color.WHITE);
        white50.setTextSize(50 * getResources().getDisplayMetrics().density);
        white50.setTextAlign(Paint.Align.CENTER);
        white50.setFakeBoldText(true);

        white33 = new Paint();
        white33.setColor(Color.WHITE);
        white33.setTextSize(33 * getResources().getDisplayMetrics().density);
        white33.setTextAlign(Paint.Align.CENTER);

        white100 = new Paint();
        white100.setColor(Color.WHITE);
        white100.setTextSize(100 * getResources().getDisplayMetrics().density);
        white100.setTextAlign(Paint.Align.CENTER);
        white100.setFakeBoldText(true);

        green38 = new Paint(green);
        green38.setTextSize(38 * getResources().getDisplayMetrics().density);
        green38.setTextAlign(Paint.Align.CENTER);

        red38 = new Paint(red);
        red38.setTextSize(38 * getResources().getDisplayMetrics().density);
        red38.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(!initialSets) {
            initializePaint();
            generateDimensions();
        }
        if(menu){
            canvas.drawRect(0, 0, getWidth()/2, getHeight()/2, red);
            canvas.drawText("Classic", getWidth()/4, getHeight()/4, black33);

            canvas.drawRect(getWidth()/2, 0, getWidth(), getHeight()/2, blue);
            canvas.drawText("Marathon", getWidth()*3/4, getHeight()/4, black33);

            canvas.drawRect(0, getHeight()/2, getWidth()/2, getHeight(), green);
            canvas.drawText("TBD", getWidth()/4, getHeight()*3/4, black33);

            canvas.drawRect(getWidth()/2, getHeight()/2, getWidth(), getHeight(), yellow);
            canvas.drawText("TBD", getWidth()*3/4, getHeight()*3/4, black33);
        } else if (gameMode == 1){
            if (gameOver) {
                canvas.drawColor(Color.BLACK);
                canvas.drawText("Quit", quitX, quitY, red38);
                canvas.drawText("Replay", replayX, replayY, green38);

                if (score == 25) {
                    canvas.drawText("FINISHED", getWidth() / 2, (getHeight() * 7 / 8), white50);
                    canvas.drawText(df.format(getTime()), getWidth() / 2, (getHeight() * 3 / 8), white100);

                } else {
                    canvas.drawText("GAME OVER", getWidth() / 2, (getHeight() * 7 / 8), white50);
                    canvas.drawText("Are you", getWidth() / 2, (getHeight() * 2 / 8), white33);
                    canvas.drawText("colorblind?", getWidth() / 2, (getHeight() * 3 / 8), white33);

                }

            } else {
                generateRow1();
                generateRow2();

                if (!initialSets) {
                    getRectangles();
                    initialSets = true;
                }

                //Canvas (background) color
                canvas.drawColor(Color.BLACK);

                //Draw Row 1
                canvas.drawRect(r1, r1p);
                canvas.drawRect(r2, r2p);
                canvas.drawRect(r3, r3p);
                canvas.drawRect(r4, r4p);

                //Draw Row 2
                canvas.drawRect(r5, white50);
                canvas.drawRect(r6, white50);
                canvas.drawRect(r7, white50);
                canvas.drawRect(r8, white50);

                //Draw Color Block
                //canvas.drawRect(cbleft, cbtop, cbright, cbbottom, cbcolor);
                canvas.drawCircle(getWidth() / 2, getHeight() / 4, cbRadius, cbcolor);

                //Draw timer
                canvas.drawText(df.format(getTime()), getWidth() / 2, getHeight() / 4, black75);

                //Draw tile progress
                canvas.drawText(score + "/25", getWidth() / 2, getHeight() / 4 + 100, black33);
            }
        } else if (gameMode == 2){
            if (gameOver) {
                canvas.drawColor(Color.BLACK);
                canvas.drawText("Quit", quitX, quitY, red38);
                canvas.drawText("Replay", replayX, replayY, green38);

                canvas.drawText(Integer.toString(score), getWidth() / 2, (getHeight() * 3 / 8), white100);

                canvas.drawText("GAME OVER", getWidth() / 2, (getHeight() * 7 / 8), white50);

            } else {
                generateRow1();
                generateRow2();

                if (!initialSets) {
                    getRectangles();
                    initialSets = true;
                }

                //Canvas (background) color
                canvas.drawColor(Color.BLACK);

                //Draw Row 1
                canvas.drawRect(r1, r1p);
                canvas.drawRect(r2, r2p);
                canvas.drawRect(r3, r3p);
                canvas.drawRect(r4, r4p);

                //Draw Row 2
                canvas.drawRect(r5, white50);
                canvas.drawRect(r6, white50);
                canvas.drawRect(r7, white50);
                canvas.drawRect(r8, white50);

                //Draw Color Block
                //canvas.drawRect(cbleft, cbtop, cbright, cbbottom, cbcolor);
                canvas.drawCircle(getWidth() / 2, getHeight() / 4, cbRadius, cbcolor);

                //Draw timer
                canvas.drawText(df.format(getTime()), getWidth() / 2, getHeight() / 4, black75);

                //Draw total score progress
                canvas.drawText(Integer.toString(score), getWidth() / 2, getHeight() / 4 + 100, black33);
            }
        }
    }

    public void generateRow1() {
        if(!rectPainted){
            r1 = new Rect((int)row11left, (int)row1top, (int)row11right, (int)row1bottom);
            r2 = new Rect((int)row12left, (int)row1top, (int)row12right, (int)row1bottom);
            r3 = new Rect((int)row13left, (int)row1top, (int)row13right, (int)row1bottom);
            r4 = new Rect((int)row14left, (int)row1top, (int)row14right, (int)row1bottom);

            r1p = paints.get(g.nextInt(4));
            r2p = paints.get(g.nextInt(4));
            r3p = paints.get(g.nextInt(4));
            r4p = paints.get(g.nextInt(4));

            while(r1p.equals(r2p)) {
                r2p = paints.get(g.nextInt(4));
            }
            while(r3p.equals(r2p) || r3p.equals(r1p)) {
                r3p = paints.get(g.nextInt(4));
            }
            while(r4p.equals(r3p) || r4p.equals(r2p) || r4p.equals(r1p)) {
                r4p = paints.get(g.nextInt(4));
            }

            rectPainted = true;
        }
    }

    public void generateRow2() {
        r5 = new Rect((int)row11left, (int)row2top, (int)row11right, (int)row2bottom);
        r6 = new Rect((int)row12left, (int)row2top, (int)row12right, (int)row2bottom);
        r7 = new Rect((int)row13left, (int)row2top, (int)row13right, (int)row2bottom);
        r8 = new Rect((int)row14left, (int)row2top, (int)row14right, (int)row2bottom);


        r5p = white50;
        r6p = white50;
        r7p = white50;
        r8p = white50;
    }

    public void generateDimensions(){
        //Width dimensions
        float width2 = (getWidth() - 25) / 4;
        row11left = 5;
        row11right = row11left + width2;
        row12left = row11right + 5;
        row12right = row12left + width2;
        row13left = row12right + 5;
        row13right = row13left + width2;
        row14left = row13right + 5;
        row14right = row14left + width2;

        //Height dimensions
        float height2 = ((getHeight() / 2) - 15) / 2;
        row1top = getHeight() - 5 - height2;
        row1bottom = getHeight() - 5;
        row2bottom = row1top - 5;
        row2top = row2bottom - height2;

        //Color block dimensions
        cbleft = 5;
        cbtop = 5;
        cbright = getWidth() - 5;
        cbbottom = row2top - 5;
        cbRadius = (getHeight()/2 - 10) / 2;

        //Game Over Screen text
        replayX = (getWidth() * 3 / 4);
        replayY = (getHeight() * 5 / 8);
        quitX = (getWidth() / 4);
        quitY = (getHeight() * 5 / 8);
    }

    public void getNextColor(){
        cbcolor = paints.get(g.nextInt(4));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX(0);
        float y = event.getY(0);

        // MULTI TOUCH TESTING//
        float x2 = 0;
        float y2 = 0;
        if(event.getPointerCount() > 1){
            x2 = event.getX(1);
            y2 = event.getY(1);
        }

        System.out.println(x + "," + y);
        System.out.println(x2 + "," + y2);
        //--------------------//

        if(menu){
            if(x <= getWidth()/2 && x >= 0 && y >= 0 && y <= getHeight()/2){
                gameMode = 1;
            } else if (x >= getWidth()/2 && x <= getWidth() && y >= 0 && y <= getHeight()/2){
                gameMode = 2;
            }
            setMS();
            menu = false;
        } else if (gameMode == 1){
            if (!gameOver) {
                synchronized (getHolder()) {
                    if (y >= row1top) {
                        if (!timerStarted) {
                            startTimer();
                            timerStarted = true;
                        }
                        if (isRightColor(x, y)) {
                            getNextColor();
                            getNextRow();
                            setScore();
                        } else {
                            endGame();
                            getTime();
                        }
                    }
                }
            } else if (gameOver) {
                synchronized (getHolder()) {
                    if (checkReplay(x, y)) {
                        resetGame();
                    } else if (checkQuit(x, y)) {
                        quitGame();
                    }
                }
            }
        } else if (gameMode == 2){
            if (!gameOver) {
                synchronized (getHolder()) {
                    if (y >= row1top) {
                        if (!timerStarted) {
                            startReverseTimer();
                            timerStarted = true;
                        }
                        if (isRightColor(x, y)) {
                            getNextColor();
                            getNextRow();
                            setScore();
                            bonusTime++;
                        } else {
                            endGame();
                            getTime();
                        }
                    }
                }
            } else if (gameOver) {
                synchronized (getHolder()) {
                    if (checkReplay(x, y)) {
                        resetGame();
                    } else if (checkQuit(x, y)) {
                        quitGame();
                    }
                }
            }
        } else if (gameMode == 3){

        } else if (gameMode == 4){

        }

        //Leave as is. Do not return true because it causes 2 touches - one for press, one for release.
        return super.onTouchEvent(event);
    }

    public void startReverseTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ms--;
                if (ms == 0)
                    gameOver = true;
                if (bonusTime == 10) {
                    if(score <= 25)
                        ms += 5000.0;
                    else if(score > 25)
                        ms += 4000.0;
                    else if(score > 50)
                        ms += 3000.0;
                    else if(score > 75)
                        ms += 2000.0;
                    bonusTime = 0;
                }
            }
        }, 1, 1);
    }

    private void quitGame() {
        gameLoopThread.setRunning(false);
        System.exit(0);
    }

    private boolean checkQuit(float x, float y) {
        if(x >= quitX - 100 && x <= quitX + 100 && y >= quitY - 50 && y <= quitY + 50){
            return true;
        } else {
            return false;
        }
    }

    private boolean checkReplay(float x, float y) {
        if(x >= replayX - 100 && x <= replayX + 100 && y >= replayY - 50 && y <= replayY + 50){
            return true;
        } else {
            return false;
        }
    }

    public void endGame() {
        gameOver = true;
        timer.cancel();
        timer.purge();
        //timer = null;
    }

    public double getTime(){
        return ms / 1000;
    }

    public void getNextRow() {
        rectPainted = false;
    }

    public boolean isRightColor(float x, float y) {
        int count = 0;
        if (rectangles.size() == 4) {
            for (Rect r : rectangles) {
                count++;
                if (x <= r.right && x >= r.left){
                    switch (count){
                        case 1:
                            if(r1p.equals(cbcolor))
                                return true;
                            break;
                        case 2:
                            if(r2p.equals(cbcolor))
                                return true;
                            break;
                        case 3:
                            if(r3p.equals(cbcolor))
                                return true;
                            break;
                        case 4:
                            if(r4p.equals(cbcolor))
                                return true;
                            break;
                    }
                }
            }
        }
        return false;
    }

    public void getRectangles(){
        rectangles.add(0, r1);
        rectangles.add(1, r2);
        rectangles.add(2, r3);
        rectangles.add(3, r4);
    }

    public void resetGame(){
        gameOver = false;
        score = 0;
        bonusTime = 0;
        timerStarted = false;
        rectPainted = false;
        setMS();
    }

    public void setMS() {
        switch (gameMode){
            case 1:
                ms = 0.0;
                break;
            case 2:
                ms = 8000.0;
                break;
        }
    }

    public void setScore(){
        score++;
        switch (gameMode) {
            case 1:
                if (score == 25)
                    endGame();
                break;
            case 2:
                break;

        }
    }

    public void startTimer(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ms++;
            }
        }, 1, 1);
    }
}