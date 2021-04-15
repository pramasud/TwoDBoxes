package com.anusha.twodboxes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.Random;

import static com.anusha.twodboxes.MainActivity.gameStopped;
import static java.lang.Thread.sleep;

/**
 * Class to handle the Game Logic and data Flow
 */
public class GameHandler extends Activity implements View.OnTouchListener, Runnable {
    // Static Variables to be Used by all Classes.
    //Stores the Information of the Points
    public static PointHandler[][] point = new PointHandler[50][50];
    //Stores the Information of the Points temporarily, Refreshed on each move
    static PointHandler[][] tempPoint = new PointHandler[50][50];
    //Stores the Information of the Lines
    public static LineHandler[] line = new LineHandler[2500];
    // Number of Lines created till now.
    public static int lineCount = 0;
    // Number of Dots for the Game. Will be changed as per the Settings.
    public static int numXDots = 8, numYDots = 8;
    // Actual Screen Width, Height and Density.
    public static int canvasHeight = 0, canvasWidth = 0;
    public static float canvasDensity = 0;
    // Shift the X and Y axis so that there is Space in the Left for Score display
    public static int shiftXAxis = 0, shiftYAxis = 0, shiftPercentage = 20;
    // Touch Tolerance -- should be based on the Distance between the Dots.
    public static int touchTolerance = 50;
    // The owner of the Move, 0 = User, 1 = System.
    public static int moveOwner = 0;
    // Number of Boxes already formed, and the Total Number of Boxes.
    // Game Over when FormedBoxes = TotalBoxes.
    public static int formedBoxes = 0, totalBoxes = 0;
    // User scores
    public static int user1Score = 0, user2Score = 0;
    // Set to true when the finger is placed on a valid dot.
    public static boolean startTempLine = false;
    // Different User Actions, Like Action Down, Action Up, drag etc.
    public static String userAction = "None";
    // Start and End position of the New Line.
    public static int startNewLineX = 0, startNewLineY = 0;
    public static int endNewLineX = 0, endNewLineY = 0;

    // User Touch Co Ordinates.
    public static float userTouchX = 0, userTouchY = 0;
    public static float StartUserTouchX = 0, StartUserTouchY = 0;
    // Set to true when the Display is Refreshed by the Display Handler Thread.
    public static boolean display_set = false;
    // Set to true to request a refresh of the screen.
    public static boolean refresh_display = true;
    // True, when the game is Over. Will have to display a Screen with details.
    public static boolean gameOver = false;

    // Object of the Display Handler Class. It will be Invoked from onCreate instance.
    DisplayHandler v;
    // Set to true, if the game has to be reset.
    public static boolean newGame = true;
    // Set to true, when the user removes focus from the app.
    //private boolean gameStopped = false;
    // Infinite Loop Variable for the Worker Thread.
    boolean doContinue = true;
    // The On Screen Button Needs to be Pressed Twice to work.
    // Counter for that.
    public static int gameRestartCount = 0;
    public static int gameMainMenuCount = 0;
    // Media Initialization.
    //public static MediaPlayer playBackgroundMusic;
    public static MediaPlayer playSoundNewLine;
    public static MediaPlayer playSoundNewLineSystem;
    public static MediaPlayer playApplause;
    //public static boolean backgroundSoundLoaded = false;

    // Initialize the Worker thread to null
    Thread worker = null;
    // To enable and Disable Logging
    // Log Level 0 = Error, 1 = Debug, 2 = Verbose Messages
    int LOG_LEVEL = 1;
    // A Flag to End the Current Process and Exit to Previous Activity
    boolean startMainMenuStatus = false;
    boolean gameMainMenuDown = false;
    boolean gameRestartDown = false;

    static int padding = 0;

    // When this Intent Is Called.
    // This Procedure will be the First to be executed.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = new DisplayHandler(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        log("Screen Display Details Available",2);
        playSoundNewLine = MediaPlayer.create(this, R.raw.select);
        playSoundNewLineSystem = MediaPlayer.create(this, R.raw.confirm);
        playApplause = MediaPlayer.create(this, R.raw.applause_2);
        log("Sound Created",2);
        // Get the Screen Height and Width in pixels.
        canvasHeight = metrics.heightPixels;
        canvasWidth = metrics.widthPixels;
        canvasDensity = metrics.density;
        padding = 12 * (int)canvasDensity;

        log("Canvas canvasHeight, canvasWidth, padding = " + canvasHeight + " , " + canvasWidth + " , " + padding,1);
        log("Canvas Density, Density DPI, Default Density = " + canvasDensity + " , " + metrics.densityDpi + " , " + DisplayMetrics.DENSITY_DEFAULT,1);

        if (canvasDensity <= 1.8) {
            canvasDensity = (float)(canvasDensity/1.15);
        } else if (canvasDensity > 1.8 && canvasDensity <= 2.2) {
            canvasDensity = (float)(canvasDensity/1.1);
        } else if (canvasDensity > 2.2 && canvasDensity <= 2.6) {
            canvasDensity = (float)(canvasDensity/1.05);
        } else if (canvasDensity > 3) {
            canvasDensity = (float)(canvasDensity/0.95);
        }

        // Calculate the actual shift in axes
        shiftXAxis = (canvasWidth * shiftPercentage) / 100;
        shiftYAxis = 0;

        log("(shiftXAxis , shiftYAxis) = (" + shiftXAxis + " , " + shiftYAxis + ")",1);
        v.setOnTouchListener(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        log("Set the On Touch Listener, and Full Screen",2);
        setContentView(v);
        log("Game Screen Should be Visible Now",2);
    }
    // Logging Procedure.
    private void log(String msg, int level) {
        if (level <= LOG_LEVEL) {
            Log.d("GameHandler : " + LOG_LEVEL, msg);
        }
    }
    // Things to do When the Process is being destroyed.
    // Called by Android System
    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("Called onDestroy",1);

        if (playApplause != null) {
            playApplause.stop();
            playApplause.reset();
            //playApplause.release();
            log("Called OnDestroy, playApplause Music Stopped and resources released",2);
        }
        if (playSoundNewLine != null) {
            playSoundNewLine.stop();
            playSoundNewLine.reset();
            //playSoundNewLine.release();
            log("Called OnDestroy, playSoundNewLine Stopped and resources released",2);
        }
        if (playSoundNewLineSystem != null) {
            playSoundNewLineSystem.stop();
            playSoundNewLineSystem.reset();
            //playSoundNewLineSystem.release();
            log("Called OnDestroy, playSoundNewLineSystem Stopped and resources released",2);
        }
        // If On Destroy is Called when Game is Over.
        // Then On Resume New Game will be Called.
        // Else Same Game will continue
        // gameStopped = !gameOver;
        if (gameStopped) {
            doContinue = false;
            display_set = false;
            refresh_display = false;
            //Intent mainMenuIntent = new Intent("android.intent.action.MAIN");
            //startActivity(mainMenuIntent);
            if (worker != null)
            {
                try {
                    log("Called OnDestroy, Trying to Stop Worker Thread",2);
                    worker.join();
                    log("Called OnDestroy, Worker thread Stopped",2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                worker = null;
            }
        }
        else {
            doContinue = false;
            display_set = false;
            refresh_display = false;
            if (worker != null)
            {
                try {
                    log("Called OnDestroy, Trying to Stop Worker Thread",2);

                    worker.join();
                    log("Called OnDestroy, Worker thread Stopped",2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                worker = null;
            }

            log("Called OnDestroy, Everything is done. Finishing the Activity",2);
            finish();
        }
    }

    // Things to do When the Process is being Stopped, Stage Before Being Destroyed.
    // Called by Android System
    @Override
    protected void onStop() {
        super.onStop();
        log("Called OnStop",1);
        if ( v != null) {
            v.stop();
        }
        log("Called OnStop, Display Handler Main Thread Stopped",2);
        display_set = false;
        doContinue = true;
        refresh_display = false;
        log("Called OnStop, (display_set = False,gameStopped,doContinue = True,refresh_display = False) = (" + "," + gameStopped + "," + "," + ")" ,1);
    }

    // Things to do When the Process is being Paused.
    // Called by Android System
    @Override
    public void onPause() {
        super.onPause();
        log("Called OnPause",1);
        log("B4 gameStopped + gameOver" + gameStopped + gameOver, 1);
        gameStopped = !gameOver;
        log("Af gameStopped + gameOver" + gameStopped + gameOver, 1);
//        if (playBackgroundMusic != null) {
//            if (playBackgroundMusic.isPlaying()) {
//                playBackgroundMusic.pause();
//            }
//        }
//        log("Called OnPause, Music Paused",2);
    }

    // Things to do When the Process is being Resumed.
    // Always Called at the start of the Activity, by Android System
    @Override
    public void onResume() {
        super.onResume();
        log("Called OnResume",2);
        log("Called OnResume,Canvas canvasHeight, canvasWidth, padding = " + canvasHeight + " , " + canvasWidth + " , " + padding,1);
        int outerVar, innerVar;
        int circleX, circleY;
        int loopIndex, x,y,x1,y1;

        refresh_display = true;
        DisplayHandler.refreshCanvas = true;
        // If game was stopped while in Progress, We will resume.
        log("Called OnResume, gameStopped, MainActivity.gameRefresh = " + gameStopped + MainActivity.gameRefresh, 1);
        if (gameStopped) {
            if (MainActivity.gameRefresh) {
                MainActivity.gameRefresh = false;
                newGame = true;
            } else {
                newGame = false;
            }
            //log("On resume B4 gameStopped + gameOver" + gameStopped + gameOver, 1);
            gameStopped = false;
            //log("On resume, AF gameStopped + gameOver" + gameStopped + gameOver, 1);
            display_set = true;
            refresh_display = true;
            DisplayHandler.refreshCanvas = true;
            for (outerVar = 0; outerVar < numXDots; outerVar++) {
                for (innerVar = 0; innerVar < numYDots; innerVar++) {
                    // Calculate the Actual X and Y coordinates for the Dots
                    //Calculate equally spaced coordinates for dots in the X Axis.
                    // We calculate for numXDots + 1 dots, as we will leave space in the End for 1 dot.
                    circleX = (((canvasWidth - shiftXAxis) * (outerVar+1)) / (numXDots+1)) + shiftXAxis;
                    circleY = (((canvasHeight - shiftYAxis) * (innerVar+1)) / (numYDots+1)) + shiftYAxis;

                    // Get a new Object of the Point Class. and initialize all values for this specific co ordinates.
                    // Get a new Object of the Point Class. and initialize all values for this specific co ordinates.
                    // This Object will hold temporary Values for Calculations
                    // Set the value of the point, Actual X and Y co ordinates
                    point[outerVar][innerVar].setX(circleX);
                    point[outerVar][innerVar].setY(circleY);
                    tempPoint[outerVar][innerVar].resetAllValues();
                }
            }
            log("Called OnResume, lineCount = " + lineCount, 1);
            for (loopIndex = 0; loopIndex < lineCount; loopIndex++) {
                x=line[loopIndex].getX();
                y=line[loopIndex].getY();
                x1=line[loopIndex].getX1();
                y1=line[loopIndex].getY1();
                line[loopIndex].setLineP(point[x][y].getX(),point[x][y].getY(),point[x1][y1].getX(),point[x1][y1].getY());
            }
            //log("Called OnResume, Requesting Display handler to resume", 2);
            //v.resume();
            //log("Called OnResume, Requesting Display handler to resume, Call Completed", 2);
        }
        else {
            log("Called OnResume, Since it is a New Game, Re Starting the worker thread with a New Game", 2);
            newGame = true;
            gameOver = false;
            doContinue = true;
        }
        if (worker == null) {
            doContinue = true;
            worker = new Thread(this);
            log("Called OnResume, Requesting Worker thread to resume.", 2);
            worker.start();
        }
        log("Called OnResume, Requesting Display handler to resume", 2);
        v.resume();
        log("Called OnResume, Requesting Display handler to resume, Call Completed", 2);
    }

    // Method that Finds and Sets Completed Boxes.
    public int findBoxes(int x, int y, int x1, int y1) {
        int numBoxesFound = 0;
        // Check if the New Line Competes a Box
        // Check if Vertical
        log("Called findBoxes, checking for Vertical Line", 3);
        if (x == x1) {
            // checking for box on the Right Side
            log("Called findBoxes, checking for box on the Right Side", 3);
            if (y < y1) {
                if (point[x][y].getHorzLine() && point[x1][y1].getHorzLine() && point[x + 1][y].getVertLine()) {
                    point[x][y].setIsRectangle(true);
                    point[x][y].setRectOwner(moveOwner);
                    numBoxesFound++;
                }
            } else {
                if (point[x1][y1].getHorzLine() && point[x][y].getHorzLine() && point[x1 + 1][y1].getVertLine()) {
                    point[x1][y1].setIsRectangle(true);
                    point[x1][y1].setRectOwner(moveOwner);
                    numBoxesFound++;
                }
            }
            // checking for box on the Left Side
            // no need to check if x=0
            log("Called findBoxes, checking for box on the Left Side, no need to check if x=0", 3);
            if (x > 0) {
                if (y < y1) {
                    if (point[x - 1][y].getHorzLine() && point[x1 - 1][y1].getHorzLine() && point[x - 1][y].getVertLine()) {
                        point[x - 1][y].setIsRectangle(true);
                        point[x - 1][y].setRectOwner(moveOwner);
                        numBoxesFound++;
                    }
                } else {
                    if (point[x1 - 1][y1].getHorzLine() && point[x - 1][y].getHorzLine() && point[x1 - 1][y1].getVertLine()) {
                        point[x1 - 1][y1].setIsRectangle(true);
                        point[x1 - 1][y1].setRectOwner(moveOwner);
                        numBoxesFound++;
                    }
                }
            }
        }
        // Check if Horizontal
        log("Called findBoxes, checking for Horizontal Line", 3);
        if (y == y1) {
            // checking for box below
            log("Called findBoxes, checking for box below", 3);
            if (x < x1) {
                if(point[x][y].getVertLine() && point[x1][y1].getVertLine() && point[x][y + 1].getHorzLine()) {
                    point[x][y].setIsRectangle(true);
                    point[x][y].setRectOwner(moveOwner);
                    numBoxesFound++;
                }
            } else {
                if (point[x1][y1].getVertLine() && point[x][y].getVertLine() && point[x1][y1 + 1].getHorzLine()) {
                    point[x1][y1].setIsRectangle(true);
                    point[x1][y1].setRectOwner(moveOwner);
                    numBoxesFound++;
                }
            }
            // checking for box above
            // No Need to check if y=0
            log("Called findBoxes, checking for box above, No Need to check if y=0", 3);
            if (y > 0) {
                if (x < x1) {
                    if (point[x][y - 1].getVertLine() && point[x][y - 1].getHorzLine() && point[x1][y1 - 1].getVertLine()) {
                        point[x][y - 1].setIsRectangle(true);
                        point[x][y - 1].setRectOwner(moveOwner);
                        numBoxesFound++;
                    }
                } else {
                    if (point[x1][y1 - 1].getVertLine() && point[x1][y1 - 1].getHorzLine() && point[x][y - 1].getVertLine()) {
                        point[x1][y1 - 1].setIsRectangle(true);
                        point[x1][y1 - 1].setRectOwner(moveOwner);
                        numBoxesFound++;
                    }
                }
            }
        }
        log("Called findBoxes, numBoxesFound = " + numBoxesFound, 3);
        return numBoxesFound;
    }

    public boolean setPointAndLineData(int x, int y, int x1, int y1) {
        // Vertical Line
        log("Called setPointAndLineData, checking for Vertical Line.", 3);
        if ((x - x1) == 0) {
            if ((y - y1) == -1) {
                // check if line already Exists.
                log("Called setPointAndLineData, check if line already Exists. Left Side", 3);
                if(!point[x][y].getVertLine()) {
                    point[x][y].setVertLine(true);
                    line[lineCount] = new LineHandler();
                    line[lineCount].setLine(x,y,x1,y1);
                    line[lineCount].setLineP(point[x][y].getX(),point[x][y].getY(),point[x1][y1].getX(),point[x1][y1].getY());
                    lineCount++;
                    return true;
                }
            } else if ((y - y1) == 1) {
                // check if line already Exists.
                log("Called setPointAndLineData, check if line already Exists. Right Side", 3);
                if (!point[x1][y1].getVertLine()) {
                    point[x1][y1].setVertLine(true);
                    line[lineCount] = new LineHandler();
                    line[lineCount].setLine(x,y,x1,y1);
                    line[lineCount].setLineP(point[x][y].getX(),point[x][y].getY(),point[x1][y1].getX(),point[x1][y1].getY());
                    lineCount++;
                    return true;
                }
            }
        } // Horizontal Line
        else if ((y - y1) == 0) {
            log("Called setPointAndLineData, Checking for Horizontal Line", 3);
            if ((x - x1) == -1) {
                // check if line already Exists.
                log("Called setPointAndLineData, check if line already Exists. Below", 3);
                if (!point[x][y].getHorzLine()) {
                    point[x][y].setHorzLine(true);
                    line[lineCount] = new LineHandler();
                    line[lineCount].setLine(x,y,x1,y1);
                    line[lineCount].setLineP(point[x][y].getX(),point[x][y].getY(),point[x1][y1].getX(),point[x1][y1].getY());
                    lineCount++;
                    return true;
                }
            } else if ((x - x1) == 1) {
                // check if line already Exists.
                log("Called setPointAndLineData, check if line already Exists. Above", 3);
                if (!point[x1][y1].getHorzLine()) {
                    point[x1][y1].setHorzLine(true);
                    line[lineCount] = new LineHandler();
                    line[lineCount].setLine(x,y,x1,y1);
                    line[lineCount].setLineP(point[x][y].getX(),point[x][y].getY(),point[x1][y1].getX(),point[x1][y1].getY());
                    lineCount++;
                    return true;
                }
            }
        }
        return false;
    }

    //private void processGameOver() {

    //}

    @Override
    public void run() {
        // Object of Auto Move Handler Class to Handle Automatic Move.
        AutoMoveHandlerEasy autoMoveEasy = new AutoMoveHandlerEasy();
        AutoMoveHandler autoMove = new AutoMoveHandler();
        // Hard Mode Not Available
        // AutoMoveHandlerHard autoMoveHard = new AutoMoveHandlerHard();
        // Looping Variables.
        int outerVar, innerVar;
        // Co Ordinates of the Center of the DOT.
        int circleX, circleY;
        // Sleep time
        //int sleepTimer = 10;
        // Local Variable to hold the Number of Boxes Created.
        // This Variable increments the Global Variable formedBoxes
        int numBoxesCreated;
        // Padding Between Objects in the Screen

        // Set to true if the Connected Dots form a valid Line.
        boolean newLine = false;
        boolean applausePlayed = false;
        boolean playAgainDown = false, mainMenuDown = false;

        // Setting the Grid Size as per the Settings
        numXDots = MainActivity.gridSize;
        numYDots = MainActivity.gridSize;
        // Total number of Boxes Possible.
        totalBoxes = (numXDots - 1) * (numYDots - 1);

        log("Worker Thread, (numXDots,numYDots,totalBoxes) = (" + numXDots + "," + numYDots + "," + totalBoxes + ")",1);

        // The Actual processing is done here.
        while (doContinue) {
            // Sleep 10ms just so that the system remains stable
            try {
                sleep(MainActivity.sleepTimer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Code here to check if the User has clicked Restart game or Exit to main Menu

            // Processing to be done when game is over.
            // will have to modify, as of now, just sleeps.
            if (gameOver) {
                try {
                    sleep(MainActivity.sleepTimer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (user1Score > user2Score) {
                    if (!MainActivity.silent && !applausePlayed) {
                        if (playApplause != null) {
                            if (!playApplause.isPlaying()) {
                                playApplause.start();
                                applausePlayed = true;
                                log("Worker Thread, Game Over Processing, User won, Applause Played",2);
                            }
                        }
                    }
                }
                // Processing for Play Again from the Game Over Screen.
                if (userTouchX >= (GameHandler.canvasWidth/4) - (44 * canvasDensity)
                        && userTouchX <= (GameHandler.canvasWidth/4) + (89.5 * canvasDensity)) {
                    if (userTouchY >= GameHandler.canvasHeight*3/5 + (5.7 * canvasDensity)
                            && userTouchY <= (GameHandler.canvasHeight*3/5) + (70.4 * canvasDensity)) {
                        if (userAction.equals("ACTION_DOWN")) {
                            playAgainDown = true;
                            log("Worker Thread, Game Over Processing, Play Again Action Down, playAgainDown = " + playAgainDown,1);
                        }
                        if (userAction.equals("ACTION_UP")) {
                            log("Worker Thread, Game Over Processing, Play Again Action Up.",2);
                            if (playAgainDown) {
                                log("Worker Thread, Game Over Processing, Play Again, Click Completed.",2);
                                newGame = true;
                                gameOver = false;
                                playAgainDown = false;
                                log("Worker Thread, Game Over Processing, Play Again  Action Up, Restarting Game.",1);
                            }
                        }
                    }
                }
                // Processing for back to Main Menu from the Game Over Screen
                if (userTouchX >= (GameHandler.canvasWidth*3/4) - (101 * canvasDensity)
                        && userTouchX <= (GameHandler.canvasWidth*3/4) + (32.3 * canvasDensity)) {
                    if (userTouchY >= GameHandler.canvasHeight*3/5 + (5.71 * canvasDensity)
                            && userTouchY <= (GameHandler.canvasHeight*3/5) + (70.47 * canvasDensity)) {
                        if (userAction.equals("ACTION_DOWN")) {
                            mainMenuDown = true;
                            log("Worker Thread, Game Over Processing, Main Menu Action Down, mainMenuDown = " + mainMenuDown,1);
                        }
                        if (userAction.equals("ACTION_UP")) {
                            log("Worker Thread, Game Over Processing, Main Menu Action Up.",2);
                            if (mainMenuDown) {
                                log("Worker Thread, Game Over Processing, Main Menu Click Completed.",2);
                                startMainMenuStatus = true;
                                doContinue = false;
                                mainMenuDown = false;
                                log("Worker Thread, Game Over Processing, Main Menu Action Down, startMainMenuStatus = " + startMainMenuStatus,1);
                            }
                        }
                    }
                }
            }
            // This Part will execute when the Game is in progress.
            else {
                // UI and Game backend Logic is handled by different threads.
                // This is to sync the Display and game logic processes.
                // So, if the display is not ready, Just Sleep and wait for the Display to be Ready.
                if (!DisplayHandler.waitForDisplay) {
                    // Processing to be done when a new game is started.
                    if (newGame) {
                        // First set the dots in the Screen.
                        // The Number of Dots will be based on the User Preference
                        numXDots = MainActivity.gridSize;
                        numYDots = MainActivity.gridSize;
                        // Total number of Boxes Possible.
                        totalBoxes = (numXDots - 1) * (numYDots - 1);
                        log("Worker Thread, Game Loop, New Game Started.",2);
                        for (outerVar = 0; outerVar < numXDots; outerVar++) {
                            for (innerVar = 0; innerVar < numYDots; innerVar++) {
                                // Calculate the Actual X and Y coordinates for the Dots

                                //Calculate equally spaced coordinates for dots in the X Axis.
                                // We calculate for numXDots + 1 dots, as we will leave space in the End for 1 dot.
                                circleX = (((canvasWidth - shiftXAxis) * (outerVar+1)) / (numXDots+1)) + shiftXAxis;
                                circleY = (((canvasHeight - shiftYAxis) * (innerVar+1)) / (numYDots+1)) + shiftYAxis;

                                // Get a new Object of the Point Class. and initialize all values for this specific co ordinates.
                                point[outerVar][innerVar] = new PointHandler();
                                point[outerVar][innerVar].resetAllValues();
                                // Get a new Object of the Point Class. and initialize all values for this specific co ordinates.
                                // This Object will hold temporary Values for Calculations
                                tempPoint[outerVar][innerVar] = new PointHandler();
                                tempPoint[outerVar][innerVar].resetAllValues();
                                // Set the value of the point, Actual X and Y co ordinates
                                point[outerVar][innerVar].setX(circleX);
                                point[outerVar][innerVar].setY(circleY);
                            }
                        }
                        log("Worker Thread, Game Loop, Created the points based on the Settings.",2);
                        // get the width and height of the Boxes
                        int boxWidth = point[1][0].getX() - point[0][0].getX();
                        int boxHeight = point[0][1].getY() - point[0][0].getY();
                        log("Worker Thread, Game Loop, Each Box Height and Width : " + boxWidth + ", " + boxHeight,1);

                        // Need to check if it works fine this way
                        touchTolerance = boxWidth/3;
                        log("Worker Thread, Game Loop, touchTolerance = boxWidth/3 = " + touchTolerance,1);
                        // Initializing other Game Parameters.
                        log("Worker Thread, Game Loop, Initializing other Game Parameters",3);
                        lineCount = 0;
                        moveOwner = -1;
                        formedBoxes = 0;
                        user1Score = 0;
                        user2Score = 0;
                        startTempLine = false;
                        userAction = "None";
                        startNewLineX = 0;
                        startNewLineY = 0;
                        endNewLineX = 0;
                        endNewLineY = 0;
                        newGame = false;
                        refresh_display = true;
                        gameMainMenuCount = 0;
                        gameRestartCount = 0;
                        applausePlayed = false;

                        log("Worker Thread, Game Loop, Generating the initial Lines",3);
                        // Generate the Initial Lines
                        int initNumLines = 0;
                        int initLines = 1;
                        int loopCount = 0;
                        // Generate Initial Lines Randomly
                        Random rand = new Random();
                        int randNumberX, randNumberY;
                        int randInitVertHorz;
                        // Calculate the Number Of Lines Based on Fill Percentage
                        if (MainActivity.fillPerc > 0) {
                            //initNumLines = (MainActivity.gridSize - 1) * (MainActivity.gridSize - 1) / MainActivity.fillPerc;
                            initNumLines = (MainActivity.gridSize - 1) * (MainActivity.gridSize - 1) * MainActivity.fillPerc / 4;
                            log("Worker Thread, Game Loop, Generating the initial Lines, initNumLines, MainActivity.fillPerc = " + initNumLines + "," + MainActivity.fillPerc,1);
                        }
                        // While Loop to generate the Lines.
                        // Will Exit when the Required Number of Lines are Generated.
                        // Or, Will exit after 20,000 iterations, If it could not find the Required Lines.
                        // The Lines will be such that there should be no ready boxes.
                        while (initLines <= initNumLines) {
                            // X and Y co ordinates of the new Line
                            randNumberX = rand.nextInt(MainActivity.gridSize - 1);
                            randNumberY = rand.nextInt(MainActivity.gridSize - 1);
                            // To decide if a horizontal line or vertical. 0 = Horizontal, 1 = Vertical
                            randInitVertHorz = (rand.nextInt(100)%2);
                            // Horizontal Line starting from (randNumberX,randNumberY)
                            if (randInitVertHorz == 0) {
                                if (randNumberX < MainActivity.gridSize - 1) {
                                    // Create a Temp Line, and Check if the Next Move Creates a Box.
                                    tempPoint[randNumberX][randNumberY].setHorzLine(true);
                                    // Using the Check ready Boxes of AutoMoveHandler, as it checks both point and tempPoint
                                    if (autoMove.checkReadyBoxes()) {
                                        log("Worker Thread, Game Loop, Generating the initial Lines, The Co Ordinate Cannot be Used - Horz" + randNumberX + "," + randNumberY,2);
                                    } else {
                                        newLine = setPointAndLineData(randNumberX, randNumberY, randNumberX + 1, randNumberY);
                                        if (newLine) {
                                            log("Worker Thread, Game Loop, Generating the initial Lines, Got a Valid Line, Horz initNumLines = " + initNumLines,2);
                                            initLines++;
                                        }
                                    }
                                }
                            } else {
                                if (randNumberY < MainActivity.gridSize - 1) {
                                    tempPoint[randNumberX][randNumberY].setVertLine(true);
                                    if (autoMove.checkReadyBoxes()) {
                                        log("The Co Ordinate Cannot be Used - Vert" + randNumberX + "," + randNumberY,1);
                                        log("Worker Thread, Game Loop, Generating the initial Lines,The Co Ordinate Cannot be Used - Vert" + randNumberX + "," + randNumberY,2);
                                    } else {
                                        newLine = setPointAndLineData(randNumberX, randNumberY, randNumberX, randNumberY + 1);
                                        if (newLine) {
                                            initLines++;
                                            log("Worker Thread, Game Loop, Generating the initial Lines, Got a Valid Line, Vert initNumLines = " + initNumLines,2);
                                        }
                                    }
                                }
                            }
                            // Reset the Temp Boxes.
                            autoMove.resetTempBoxes();
                            loopCount++;
                            // Force Exit after 20000 attempts to create lines.
                            if (loopCount > 20000) {
                                initLines = initNumLines + 1;
                                log("Too Much Loop, Exiting",0);
                                log("Worker Thread, Game Loop, Too Much Loop, loopCount = " + loopCount,0);
                            }
                        }
                        // First Move is Always User's
                        moveOwner = 0;
                        newLine = false;
                        log("Worker Thread, Game Loop, lineCount = " + lineCount + ", moveOwner = " + moveOwner,1);
                    }

                    // Check if Pause or Restart Game is Touched.

                    // Processing User's Move.
                    log("Worker Thread, Game Loop, Processing User's Move.",3);
                    if (moveOwner == 0) {

                        if (!startTempLine) {
                            for (outerVar = 0; outerVar < numXDots; outerVar++) {
                                for (innerVar = 0; innerVar < numYDots; innerVar++) {
                                    // Check the location of the dot for which touch is performed.
                                    if (userTouchX >= point[outerVar][innerVar].getX() - touchTolerance
                                            && userTouchX <= point[outerVar][innerVar].getX() + touchTolerance) {
                                        if (userTouchY >= point[outerVar][innerVar].getY() - touchTolerance
                                                && userTouchY <= point[outerVar][innerVar].getY() + touchTolerance) {
                                            // Start touch Performed for the Dot (outerVar,innerVar)
                                            // Process action down. Store the Position of the Dot in the Matrix.
                                            if (userAction.equals("ACTION_DOWN")) {
                                                startNewLineX = outerVar;
                                                startNewLineY = innerVar;
                                                StartUserTouchX = point[outerVar][innerVar].getX();
                                                StartUserTouchY = point[outerVar][innerVar].getY();
                                                // Valid Start Point. Temp Line Can be Started
                                                startTempLine = true;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            for (outerVar = 0; outerVar < numXDots; outerVar++) {
                                for (innerVar = 0; innerVar < numYDots; innerVar++) {
                                    if (userAction.equals("ACTION_UP")) {
                                        // Vertical Line
                                        if (userTouchX >= StartUserTouchX - touchTolerance
                                                && userTouchX <= StartUserTouchX + touchTolerance) {
                                            if (userTouchY > StartUserTouchY) {
                                                if (userTouchX >= point[outerVar][innerVar].getX() - touchTolerance
                                                        && userTouchX <= point[outerVar][innerVar].getX() + touchTolerance) {
                                                    if (userTouchY >= point[outerVar][innerVar].getY() - (touchTolerance / 2)
                                                            && userTouchY <= point[outerVar][innerVar].getY() + (touchTolerance * 2)) {
                                                        endNewLineX = outerVar;
                                                        endNewLineY = innerVar;
                                                        newLine = setPointAndLineData(startNewLineX, startNewLineY, endNewLineX, endNewLineY);
                                                        if (newLine) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (userTouchX >= point[outerVar][innerVar].getX() - touchTolerance
                                                        && userTouchX <= point[outerVar][innerVar].getX() + touchTolerance) {
                                                    if (userTouchY >= point[outerVar][innerVar].getY() - (touchTolerance * 2)
                                                            && userTouchY <= point[outerVar][innerVar].getY() + (touchTolerance / 2)) {
                                                        endNewLineX = outerVar;
                                                        endNewLineY = innerVar;
                                                        newLine = setPointAndLineData(startNewLineX, startNewLineY, endNewLineX, endNewLineY);
                                                        if (newLine) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else if (userTouchY >= StartUserTouchY - touchTolerance
                                                && userTouchY <= StartUserTouchY + touchTolerance) {
                                            if (userTouchX > StartUserTouchX) {
                                                if (userTouchX >= point[outerVar][innerVar].getX() - (touchTolerance / 2)
                                                        && userTouchX <= point[outerVar][innerVar].getX() + (touchTolerance * 2)) {
                                                    if (userTouchY >= point[outerVar][innerVar].getY() - touchTolerance
                                                            && userTouchY <= point[outerVar][innerVar].getY() + touchTolerance) {
                                                        endNewLineX = outerVar;
                                                        endNewLineY = innerVar;
                                                        newLine = setPointAndLineData(startNewLineX, startNewLineY, endNewLineX, endNewLineY);
                                                        if (newLine) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (userTouchX >= point[outerVar][innerVar].getX() - (touchTolerance * 2)
                                                        && userTouchX <= point[outerVar][innerVar].getX() + (touchTolerance / 2)) {
                                                    if (userTouchY >= point[outerVar][innerVar].getY() - touchTolerance
                                                            && userTouchY <= point[outerVar][innerVar].getY() + touchTolerance) {
                                                        endNewLineX = outerVar;
                                                        endNewLineY = innerVar;
                                                        newLine = setPointAndLineData(startNewLineX, startNewLineY, endNewLineX, endNewLineY);
                                                        if (newLine) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (newLine) {
                                    log("Worker Thread, Game Loop, Processing User's Move, A New Line Created.",3);
                                    log("(startNewLineX, startNewLineY), (endNewLineX, endNewLineY) = (" + startNewLineX + "," + startNewLineY + "),(" + endNewLineX + "," + endNewLineY + ")",2);
                                    gameRestartCount = 0;
                                    gameMainMenuCount = 0;
                                    log("For User : moveOwner = " + moveOwner,1);
                                    if (!MainActivity.silent) {
                                        if (playSoundNewLine != null) {
                                            if (!playSoundNewLine.isPlaying()) {
                                                playSoundNewLine.start();
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (moveOwner == 1) {
                        // Long Sleep. To pause between user and Auto Moves.
                        log("For Automove : moveOwner = " + moveOwner,1);
                        try {
                            sleep(MainActivity.longSleepTimer);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        log("For Automove : long sleep, moveOwner  = " + moveOwner,1);
                        if (MainActivity.gameMode.equals("C")) {
                            log("Worker Thread, Game Loop, Processing Auto Move, Complex Mode.",3);
                            autoMove.getAutoMove();
                        }
                        else if (MainActivity.gameMode.equals("A")) {
                            log("Worker Thread, Game Loop, Processing Auto Move, Advanced Mode.",3);
                            // Hard Mode Not Available. So start in Normal Mode Only.
                            // autoMoveHard.getAutoMove();
                            autoMove.getAutoMove();
                        }
                        else {
                            log("Worker Thread, Game Loop, Processing Auto Move, Normal Mode.",3);
                            log("For Automove : long sleep, moveOwner, MainActivity.gameMode  = " + moveOwner + "," + MainActivity.gameMode,1);
                            autoMoveEasy.getAutoMove();
                        }

                        log("Worker Thread, Game Loop, Values Obtained from Automove, Checking if it creates a Valid line",1);
                        newLine = setPointAndLineData(startNewLineX, startNewLineY, endNewLineX, endNewLineY);
                        if (newLine) {
                            log("Worker Thread, Game Loop, Values Obtained from Auto move, Waiting for Animation performed in UI",3);
                            //DisplayHandler.waitForDisplay = true;
                            //if (!MainActivity.silent) {
                            //    if (playSoundNewLineSystem != null) {
                            //        if (!playSoundNewLineSystem.isPlaying()) {
                            //            playSoundNewLineSystem.start();
                            //        }
                            //    }
                            //}
                        }
                    }

                    if (userAction.equals("ACTION_UP")) {
                        log("Worker Thread, Game Loop, Generic Action Up Section.",3);
                        if (startTempLine) {
                            startTempLine = false;
                        }
                        playAgainDown = false;
                        mainMenuDown = false;
                    }
                    // Further Processing Once the New Line Is Created.
                    if (newLine) {
                        numBoxesCreated = findBoxes(startNewLineX, startNewLineY, endNewLineX, endNewLineY);
                        log("Worker Thread, Game Loop, Boxes Created, numBoxesCreated = " + numBoxesCreated,2);
                        if (numBoxesCreated > 0) {
                            formedBoxes = formedBoxes + numBoxesCreated;
                            if (moveOwner == 0) {
                                user1Score = user1Score + numBoxesCreated;
                            } else {
                                user2Score = user2Score + numBoxesCreated;
                                DisplayHandler.waitForDisplay = true;
                                if (!MainActivity.silent) {
                                    if (playSoundNewLineSystem != null) {
                                        if (!playSoundNewLineSystem.isPlaying()) {
                                            playSoundNewLineSystem.start();
                                        }
                                    }
                                }
                            }

                            log("Worker Thread, Game Loop, GameStatus : formedBoxes totalBoxes = " + formedBoxes + " " + totalBoxes,2);

                            if (formedBoxes == totalBoxes) {
                                // Sleeping some time before game over.
                                try {
                                    sleep(MainActivity.longSleepTimer);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                gameOver = true;
                                if (user1Score > user2Score) {
                                    SharedPreferences updSettings = getSharedPreferences(SettingsHandler.PREFS_NAME, 0);
                                    SharedPreferences.Editor editor = updSettings.edit();

                                    if (MainActivity.gameMode.equals("C")) {
                                        MainActivity.complexGameWon = MainActivity.complexGameWon + 1;
                                        editor.putInt("complexGameWon", MainActivity.complexGameWon);
                                        editor.apply();
                                        if (!MainActivity.advancedModeEnabled) {
                                            if (MainActivity.complexGameWon >= 5) {
                                                MainActivity.advancedModeEnabled = true;
                                                editor.putBoolean("advancedModeEnabled", MainActivity.advancedModeEnabled);
                                                editor.apply();
                                            }
                                        }
                                        log("MainActivity.complexGameWon = " + MainActivity.complexGameWon,0);
                                    } else if (MainActivity.gameMode.equals("A")) {
                                        MainActivity.advancedGameWon = MainActivity.advancedGameWon + 1;
                                        editor.putInt("advancedGameWon", MainActivity.advancedGameWon);
                                        editor.apply();
                                    }
                                }

                                log("Worker Thread, Game Loop, Game Over = True, Move Owner = -1",1);
                                //sleepTimer = 100;
                                moveOwner = -1;
                            }
                        } else {
                            if (moveOwner == 0) {
                                moveOwner = 1;
                                log("moveOwner 0->1 = " + moveOwner,1);
                            } else {
                                moveOwner = 0;
                                log("moveOwner 1->0 = " + moveOwner,1);
                                DisplayHandler.waitForDisplay = true;
                                if (!MainActivity.silent) {
                                    if (playSoundNewLineSystem != null) {
                                        if (!playSoundNewLineSystem.isPlaying()) {
                                            playSoundNewLineSystem.start();
                                        }
                                    }
                                }
                            }
                        }
                        newLine = false;
                    }
                    //refresh_display = false;
                    display_set = true;
                }
                else {
                    try {
                        sleep(MainActivity.sleepTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Out of Loop, and checking if the Process has to be finished.
        if (!doContinue) {
            if (startMainMenuStatus) {
                log("Worker Thread, Outside Game Loop, Finishing Game Activity",1);
                startMainMenuStatus = false;
                //gameOver = false;
                log("Worker Thread, Outside Game Loop, Finishing Game Activity",1);
                v.stop();
                log("Worker Thread, Outside Game Loop, Finishing Game Activity",1);
                finish();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        refresh_display = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                userAction = "ACTION_DOWN";
                userTouchX = event.getX();
                userTouchY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                userAction = "ACTION_UP";
                userTouchX = event.getX();
                userTouchY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                userAction = "ACTION_MOVE";
                userTouchX = event.getX();
                userTouchY = event.getY();
                break;
        }

        processScreenButtons();
        return true;
    }

    private void processScreenButtons() {
        // Check for Main Menu.
        double touchXValuesMin = (shiftXAxis/2) + (padding * 3) - (15.23 * canvasDensity);
        double touchXValueMax = (shiftXAxis/2) + (padding * 3) + (15.23 * canvasDensity);
        double touchYValuesMin = ((canvasHeight*3)/5) + (padding * 10) - (15.23 * canvasDensity);
        double touchYValueMax = ((canvasHeight*3)/5) + (padding * 10) + (15.23 * canvasDensity);
        log("Called Process Main Menu : userTouchX,userTouchY,touchXValuesMin,touchXValueMax,touchYValuesMin,touchYValueMax,userAction = ("
                + userTouchX + "," + userTouchY + "," + touchXValuesMin + "," + touchXValueMax + "," + touchYValuesMin + "," + touchYValueMax + "," + userAction +")",2);
        if (userTouchX >= (shiftXAxis/2) + (padding * 3) - (15.23 * canvasDensity)
                && userTouchX <= (shiftXAxis/2) + (padding * 3) + (15.23 * canvasDensity)) {
            if (userTouchY >= ((canvasHeight*3)/5) + (padding * 10) - (15.23 * canvasDensity)
                    && userTouchY <= ((canvasHeight*3)/5) + (padding * 10) + (15.23 * canvasDensity)) {
                log("Inside If, gameMainMenuDown = " + gameMainMenuDown,1);
                // Start touch Performed for the Dot (outerVar,innerVar)
                // Process action down. Store the Position of the Dot in the Matrix.
                if (userAction.equals("ACTION_DOWN")) {
                    gameMainMenuDown = true;
                    //log("Worker Thread, Game Loop, Game Menu, Action Down, gameMainMenuDown = " + gameMainMenuDown,1);
                }
                if (userAction.equals("ACTION_UP")) {
                    log("Worker Thread, Game Loop, Game Menu, Action Up",3);
                    if (gameMainMenuDown) {
                        gameMainMenuCount++;
                        log("Worker Thread, Game Loop, Game Menu, click completed, gameMainMenuCount = " + gameMainMenuCount,1);
                        gameMainMenuDown = false;
                        if (gameMainMenuCount == 2) {
                            //newGame = true;
                            gameMainMenuCount = 0;
                            onPause();
                            onStop();
                            finish();
                        }
                    }
                }
            }
        }

        // Check for Restart Game.
        if (userTouchX >= (shiftXAxis/2) - (padding * 3) - (15.23 * canvasDensity)
                && userTouchX <= (shiftXAxis/2) - (padding * 3) + (15.23 * canvasDensity)) {
            if (userTouchY >= ((canvasHeight*3)/5) + (padding*10) - (15.23 * canvasDensity)
                    && userTouchY <= ((canvasHeight*3)/5) + (padding*10) + (15.23 * canvasDensity)) {
                if (userAction.equals("ACTION_DOWN")) {
                    gameRestartDown = true;
                    //log("Worker Thread, Game Loop, Game Menu, Action Down, gameRestartDown = " + gameRestartDown,1);
                }
                if (userAction.equals("ACTION_UP")) {
                    log("Worker Thread, Game Loop, Game Menu Restart, Action Up",3);
                    if (gameRestartDown) {
                        gameRestartCount++;
                        log("Worker Thread, Game Loop, Game Restart, click completed, gameRestartCount = " + gameRestartCount,1);
                        gameRestartDown = false;
                        if (gameRestartCount == 2) {
                            newGame = true;
                            log("Worker Thread, Game Loop, Game Restart, click completed, Restarting, gameRestartCount = " + gameRestartCount,2);
                            gameRestartCount = 0;
                            // A continue should be good here.
                        }
                    }
                }
            }
        }
    }
}
