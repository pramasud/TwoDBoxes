package com.anusha.twodboxes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
//import android.util.Log;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static java.lang.Thread.sleep;

/**
 * Created by Lily on 9/25/2016.
 */
public class DisplayHandler extends SurfaceView implements Runnable{
    public static boolean waitForDisplay = false;
    //GameHandler game = new GameHandler();
    int radius = 10, lineWidth = 10, thickLineWidth = 20, radiusOnSelect = 13;
    // Height and Width of Canvas in Number of Pixels
    int maxXAxis = 0, maxYAxis = 0;
    // Height and width of the Box created by completing all 4 lines
    int rectangleWidth = 0, rectangleHeight = 0;
    // Level of Log. 1 is debug.
    int LOG_LEVEL = 3;
    // Paint Object for Circles
    Paint circlePaint = new Paint();
    // Paint Object for the Line.
    Paint linePaint = new Paint();
    //Special Paint object
    Paint SpecialLinePaint = new Paint();
    // Thick line for Boundry between User Score and Play Area
    Paint thickLinePaint = new Paint();
    // Paint object for the Text
    Paint textPaint = new Paint();
    // Special Paint object for the Text
    Paint specialTextPaint = new Paint();
    // Set to true when the Values of the Canvas needs to be ReCalculated
    public static boolean refreshCanvas = true;

    // Paint for the Boxes Owned by User and System
    Paint user1Paint = new Paint();
    Paint user2Paint = new Paint();
    Paint nullUserPaint = new Paint();
    Paint circlePaintButton = new Paint();
    Paint linePaintButton = new Paint();
    Paint textPaintButton = new Paint();

    // Worker thread. This Thread actually refreshes the Screen
    Thread worker1 = null;
    // Surface holder object, to get a lock on the screen
    SurfaceHolder screenHolder;
    // Loop Variable of the Worker thread
    boolean doContinue = false;

    // Initialize the Parameters of the User Details.
    //int user1Score = 0, user2Score = 0;
    int user1PositionX = 0, user1PositionY = 0;
    int user2PositionX = 0, user2PositionY = 0;
    int user1MoveIndicatorX = 0, user1MoveIndicatorY = 0;
    int user1ScorePositionX = 0, user1ScorePositionY = 0;
    int user2ScorePositionX = 0, user2ScorePositionY = 0;
    int user2MoveIndicatorX = 0, user2MoveIndicatorY = 0;

    // Bitmap to store the Background
    Bitmap gameBackground;

    // Private Logging Procedure
    private void log(String msg, int level) {
        if (level <= LOG_LEVEL) {
            Log.d("DisplayHandler :" + LOG_LEVEL, msg);
        }
    }

    // Display handler constructor to get hold of the screen
    public DisplayHandler(Context context) {
        super(context);
        screenHolder = getHolder();
    }

    public void stop() {
        // I don't suppose this needs to be done on stop.
        // May be, On Destroy. But we will have to check.
        doContinue = false;
        log("Trying to stop thread,worker1 = " + worker1,1);
        if (worker1 != null) {
            log("Trying to stop thread",1);
            try {
                worker1.join();
                log("Trying to stop thread, Success",1);
            } catch (InterruptedException e) {
                log("Trying to stop thread, Error",1);
                e.printStackTrace();
            }
            worker1 = null;
        }
        this.destroyDrawingCache();
    }

    public void resume() {
        doContinue = true;
        worker1 = new Thread(this);
        log("Trying to start Thread Started",1);
        worker1.start();
        log("Thread Started",1);
    }

    @Override
    public void run() {
        int outerVar, innerVar;
        int loopIndex;
        int padding;
        Bitmap resized;
        boolean requestRefresh = true;
        int animateAutoLineStep = 10, animateAutoLineStepCount = 1;
        String userName = MainActivity.storedUserName;
        String systemUser = "System";
        String userShortName = userName.substring(0,1).toUpperCase();
        String systemShortName = "AI";
        String blockedBoxes = "X";
        gameBackground = BitmapFactory.decodeResource(getResources(), R.drawable.bkg);
        resized = Bitmap.createScaledBitmap(gameBackground, GameHandler.canvasWidth, GameHandler.canvasHeight, true);

        radius = 4 * (int)GameHandler.canvasDensity;
        lineWidth = 4 * (int)GameHandler.canvasDensity;
        thickLineWidth = 8 * (int)GameHandler.canvasDensity;
        radiusOnSelect = 6 * (int)GameHandler.canvasDensity;
        // if Modified, the Variable in GameHandler also has to be Changed to be in sync.
        padding = GameHandler.padding;
        // To Log when display_set variable is changed.
        boolean state_change = false;
        int shiftXAxisVal = 0, shiftYAxisVal = 0;

        if (userName.length() > 12) {
            userName = MainActivity.storedUserName.substring(0,12);
        }

        // Main Working Loop
        while (doContinue)
        {
            try {
                sleep(MainActivity.sleepTimer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Processing will be done only when display_set variable is set
            if ( state_change  != GameHandler.display_set) {
                log("Main Loop, GameHandler.display_set = " + GameHandler.display_set,1);
                state_change = GameHandler.display_set;
            }

            if (GameHandler.display_set) {
                if (requestRefresh) {
                    if (!screenHolder.getSurface().isValid()) {
                        log("Main Loop, Could Not get hold of the Screen.",0);
                        continue;
                    }

                    Canvas canvas = screenHolder.lockCanvas();
                    if ( canvas == null) {
                        continue;
                    }
                    log("Main Loop, Got hold of the Screen, Locked Canvas.",4);

                    if (refreshCanvas) {
                        log("Main Loop, Inside Refresh Canvas.",4);
                        maxXAxis = GameHandler.canvasWidth;
                        maxYAxis = GameHandler.canvasHeight;

                        rectangleWidth = (maxXAxis - GameHandler.shiftXAxis) / (GameHandler.numXDots + 1);
                        rectangleHeight = (maxYAxis - GameHandler.shiftYAxis) / (GameHandler.numYDots + 1);
                        log("Main Loop, (rectangleWidth,rectangleHeight) = (" + rectangleWidth + "," + rectangleHeight +")",1);
                        user1PositionX = GameHandler.shiftXAxis/2;
                        user1PositionY = maxYAxis/5;
                        log("Main Loop, user1PositionX, user1PositionY = " + user1PositionX + "," + user1PositionY, 1);

                        user1ScorePositionX = GameHandler.shiftXAxis/2;
                        user1ScorePositionY = user1PositionY + (padding*5);
                        user1MoveIndicatorX = padding;
                        user1MoveIndicatorY = ((maxYAxis)/5) + (padding);

                        user2PositionX = GameHandler.shiftXAxis/2;
                        user2PositionY = (maxYAxis*3)/5;
                        user2ScorePositionX = GameHandler.shiftXAxis/2;
                        user2ScorePositionY = user2PositionY + (padding*5);
                        user2MoveIndicatorX = padding;
                        user2MoveIndicatorY = ((maxYAxis*3)/5) + (padding);

                        refreshCanvas = false;

                        shiftXAxisVal = (maxXAxis*GameHandler.shiftPercentage)/100;
                        shiftYAxisVal = (maxYAxis*GameHandler.shiftPercentage)/100;

                        log("Main Loop, All Variables Set.",3);
                    }

                    //canvas.drawARGB(255,200,200,150);
                    if (resized != null && doContinue) {
                        try {
                            log("Main Loop, Draw bitmap, Trying to Draw Background",4);
                            canvas.drawBitmap(resized,0,0,null);
                        } catch (NullPointerException e) {
                            log("Main Loop, Draw bitmap, Error Again, Dont Know whats happening",0);
                        }
                    }

                    circlePaint.setColor(Color.BLUE);
                    circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

                    user1Paint.setAntiAlias(true);
                    user1Paint.setStyle(Paint.Style.FILL);
                    user1Paint.setColor(0x9900CC99);
                    //user1Paint.setColor(Color.GREEN);

                    user2Paint.setAntiAlias(true);
                    user2Paint.setStyle(Paint.Style.FILL);
                    user2Paint.setColor(0x99FFB7C5);
                    //user2Paint.setColor(Color.MAGENTA);

                    nullUserPaint.setAntiAlias(true);
                    nullUserPaint.setStyle(Paint.Style.FILL);
                    nullUserPaint.setColor(Color.BLACK);

                    // Draw the Completed Boxes First.
                    // Because all other Drawing will be done on top of that.
                    for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
                        for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                            if (GameHandler.point[outerVar][innerVar].getRectOwner() == 0) {
                                canvas.drawRect(GameHandler.point[outerVar][innerVar].getX()
                                        ,GameHandler.point[outerVar][innerVar].getY()
                                        ,GameHandler.point[outerVar][innerVar].getX() + rectangleWidth
                                        ,GameHandler.point[outerVar][innerVar].getY() + rectangleHeight
                                        , user1Paint);
                            }
                            else if (GameHandler.point[outerVar][innerVar].getRectOwner() == 1) {
                                canvas.drawRect(GameHandler.point[outerVar][innerVar].getX()
                                        ,GameHandler.point[outerVar][innerVar].getY()
                                        ,GameHandler.point[outerVar][innerVar].getX() + rectangleWidth
                                        ,GameHandler.point[outerVar][innerVar].getY() + rectangleHeight
                                        , user2Paint);
                            }
                            else if (GameHandler.point[outerVar][innerVar].getRectOwner() == -1) {
                                canvas.drawRect(GameHandler.point[outerVar][innerVar].getX(),
                                        GameHandler.point[outerVar][innerVar].getY()
                                        ,GameHandler.point[outerVar][innerVar].getX() + rectangleWidth,
                                        GameHandler.point[outerVar][innerVar].getY() + rectangleHeight,
                                        nullUserPaint);
                            }
                            // Draw the Dots.
                            canvas.drawCircle((float)GameHandler.point[outerVar][innerVar].getX(),
                                    (float)GameHandler.point[outerVar][innerVar].getY(),
                                    radius,
                                    circlePaint);
                        }
                    }

                    // On Screen Re Play Button
                    circlePaintButton.setAntiAlias(true);
                    circlePaintButton.setColor(0xff007FFF);
                    circlePaintButton.setStyle(Paint.Style.FILL_AND_STROKE);

                    canvas.drawCircle(user2ScorePositionX - (padding*3),
                            user2ScorePositionY + (padding*5),
                            (float)(15.23 * GameHandler.canvasDensity),
                            circlePaintButton);

                    RectF rectF = new RectF(user2ScorePositionX - (padding*3) - (float)(10.66 * GameHandler.canvasDensity),
                            user2ScorePositionY + (padding*5) - (float)(9.52 * GameHandler.canvasDensity),
                            user2ScorePositionX - (padding*3) + (float)(10.66 * GameHandler.canvasDensity),
                            user2ScorePositionY + (padding*5) + (float)(9.52 * GameHandler.canvasDensity));

                    linePaintButton.setStrokeWidth((float)(lineWidth*1.5));
                    linePaintButton.setColor(Color.RED);
                    linePaintButton.setStyle(Paint.Style.STROKE);

                    canvas.drawArc (rectF, 20, 140, false, linePaintButton);

                    canvas.drawArc (rectF, 200, 140, false, linePaintButton);

                    // On Screen Home Button
                    //int abc = user2ScorePositionY + (padding*5);
                    //log("abc = " + abc, 1);
                    canvas.drawCircle(user2ScorePositionX + (padding*3),
                            user2ScorePositionY + (padding*5),
                            (float)(15.23 * GameHandler.canvasDensity),
                            circlePaintButton);

//                    linePaintButton.setStrokeWidth(lineWidth);
//                    linePaintButton.setColor(Color.RED);
//                    linePaintButton.setStyle(Paint.Style.FILL_AND_STROKE);

                    // Drawing The Home Symbol in the Button
                    Path pathRePlay = new Path();
                    pathRePlay.moveTo(user2ScorePositionX + (padding*3), user2ScorePositionY + (padding*5) - (float)(8.38 * GameHandler.canvasDensity)); // top
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3) - (float)(6.47 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5)); // Left Bottom
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3) + (float)(6.47 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5)); // Right Bottom
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3), user2ScorePositionY + (padding*5) - (float)(8.38 * GameHandler.canvasDensity)); // top
                    pathRePlay.close();
                    canvas.drawPath(pathRePlay, linePaintButton);

//                    linePaintButton.setStrokeWidth(lineWidth);
//                    linePaintButton.setColor(0xffE32666);
//                    linePaintButton.setStyle(Paint.Style.FILL_AND_STROKE);

                    pathRePlay.moveTo(user2ScorePositionX + (padding*3) - (float)(5.71 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5)); // Left Bottom
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3) - (float)(5.71 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5) + (float)(8.38 * GameHandler.canvasDensity)); // Left Below Bottom
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3) + (float)(5.71 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5) + (float)(8.38 * GameHandler.canvasDensity)); // Right Below Bottom
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3) + (float)(5.71 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5)); // Right Bottom
                    pathRePlay.lineTo(user2ScorePositionX + (padding*3) - (float)(5.71 * GameHandler.canvasDensity), user2ScorePositionY + (padding*5)); // Left Bottom
                    pathRePlay.close();
                    canvas.drawPath(pathRePlay, linePaintButton);

                    textPaintButton.setAntiAlias(true);
                    textPaintButton.setStyle(Paint.Style.FILL);
                    textPaintButton.setColor(Color.BLACK);
                    textPaintButton.setTextSize(15*GameHandler.canvasDensity);
                    textPaintButton.setTextAlign(Paint.Align.CENTER);

                    if (GameHandler.gameRestartCount == 1 || GameHandler.gameMainMenuCount == 1) {
                        // Text Style for Click to confirm
                        canvas.drawText("Tap to Confirm.",
                                shiftXAxisVal/2,
                                user2ScorePositionY + (padding*6) + (float)(24.5 * GameHandler.canvasDensity),
                                textPaintButton);
                    }

                    // Draw a Line from Start Position to the User's Current Touch Location
                    // if a temp line has started.
                    linePaint.setStrokeWidth(lineWidth);
                    linePaint.setColor(Color.BLUE);
                    linePaint.setStyle(Paint.Style.FILL_AND_STROKE);

                    textPaint.setAntiAlias(true);
                    textPaint.setStyle(Paint.Style.FILL);
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextSize(21*GameHandler.canvasDensity);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    textPaint.setTypeface(Typeface.create("cursive", Typeface.BOLD));

                    if (GameHandler.userAction.equals("ACTION_MOVE")) {
                        if (GameHandler.startTempLine) {
                            canvas.drawLine(GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getX()
                                    , GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getY()
                                    , GameHandler.userTouchX
                                    , GameHandler.userTouchY
                                    , linePaint);
                        }
                    }

                    // Some Animation in Progress
                    // So game will wait for the animation to complete.
                    specialTextPaint.setAntiAlias(true);
                    specialTextPaint.setStyle(Paint.Style.FILL);
                    specialTextPaint.setColor(Color.BLACK);
                    specialTextPaint.setTextSize(20*GameHandler.canvasDensity);
                    specialTextPaint.setTextAlign(Paint.Align.CENTER);
                    specialTextPaint.setTypeface(Typeface.create("cursive", Typeface.BOLD));


                    SpecialLinePaint.setStrokeWidth(lineWidth);
                    SpecialLinePaint.setColor(Color.RED);
                    SpecialLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);

                    if (waitForDisplay) {
                        int lineEndX = 0, lineEndY = 0;
                        if (GameHandler.startNewLineX == GameHandler.endNewLineX) {
                            // Then Vertical Line. Y Axis should change
                            lineEndX = GameHandler.point[GameHandler.endNewLineX][GameHandler.endNewLineY].getX();
                            lineEndY = GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getY() +
                                    ((GameHandler.point[GameHandler.endNewLineX][GameHandler.endNewLineY].getY() -
                                            GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getY()) * animateAutoLineStepCount / animateAutoLineStep);
                            animateAutoLineStepCount++;
                        }
                        else if (GameHandler.startNewLineY == GameHandler.endNewLineY) {
                            // Then Horizontal Line. X Axis should change
                            lineEndY = GameHandler.point[GameHandler.endNewLineX][GameHandler.endNewLineY].getY();
                            lineEndX = GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getX() +
                                    ((GameHandler.point[GameHandler.endNewLineX][GameHandler.endNewLineY].getX() -
                                            GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getX()) * animateAutoLineStepCount / animateAutoLineStep);
                            animateAutoLineStepCount++;
                        }
                        canvas.drawLine(GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getX()
                                , GameHandler.point[GameHandler.startNewLineX][GameHandler.startNewLineY].getY()
                                , lineEndX
                                , lineEndY
                                , SpecialLinePaint);

                        if ( animateAutoLineStepCount == animateAutoLineStep) {
                            waitForDisplay = false;
                            animateAutoLineStepCount = 1;
                        }
                    }
                    // Major Divider Line for Score and Game Play Area.
                    thickLinePaint.setStrokeWidth(thickLineWidth);
                    thickLinePaint.setColor(Color.BLUE);
                    thickLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawLine(shiftXAxisVal, 0, shiftXAxisVal , maxYAxis, thickLinePaint);

                    for (loopIndex = 0; loopIndex < GameHandler.lineCount - 1; loopIndex++) {
                        canvas.drawLine(GameHandler.line[loopIndex].getXP()
                                , GameHandler.line[loopIndex].getYP()
                                , GameHandler.line[loopIndex].getX1P()
                                , GameHandler.line[loopIndex].getY1P()
                                , linePaint);

                        canvas.drawCircle(GameHandler.line[loopIndex].getXP()
                                , GameHandler.line[loopIndex].getYP()
                                , radiusOnSelect
                                , circlePaint);

                        canvas.drawCircle(GameHandler.line[loopIndex].getX1P()
                                , GameHandler.line[loopIndex].getY1P()
                                , radiusOnSelect
                                , circlePaint);
                    }

                    if (!waitForDisplay) {
                        if (GameHandler.lineCount > 0) {
                            canvas.drawLine(GameHandler.line[GameHandler.lineCount - 1].getXP()
                                    , GameHandler.line[GameHandler.lineCount - 1].getYP()
                                    , GameHandler.line[GameHandler.lineCount - 1].getX1P()
                                    , GameHandler.line[GameHandler.lineCount - 1].getY1P()
                                    , SpecialLinePaint);
                        }
                    }



                    Rect bounds = new Rect();
                    // DrawText Draws the Text around the Center of the CoOrdinate, if setTextAlign = CENTER
                    canvas.drawText(userName, user1PositionX, user1PositionY, textPaint);
                    canvas.drawText(systemUser, user2PositionX, user2PositionY, textPaint);

                    canvas.drawText("("+GameHandler.user1Score+")", user1ScorePositionX, user1ScorePositionY, textPaint);
                    canvas.drawText("("+GameHandler.user2Score+")", user2ScorePositionX, user2ScorePositionY, textPaint);

                    if (GameHandler.moveOwner == 0) {
                        canvas.drawLine(user1MoveIndicatorX
                                , user1MoveIndicatorY
                                , ((maxXAxis * GameHandler.shiftPercentage / 100) - padding)
                                , user1MoveIndicatorY
                                , linePaint);
                    } else {
                        canvas.drawLine(user2MoveIndicatorX
                                , user2MoveIndicatorY
                                , ((maxXAxis * GameHandler.shiftPercentage / 100) - padding)
                                , user2MoveIndicatorY
                                , linePaint);
                    }

                    for (outerVar = 0; outerVar < GameHandler.numXDots-1; outerVar++) {
                        for (innerVar = 0; innerVar < GameHandler.numYDots-1; innerVar++) {
                            if (GameHandler.point[outerVar][innerVar].getRectOwner() == 0) {
                                specialTextPaint.getTextBounds(userShortName,0,userShortName.length(),bounds);
                                canvas.drawText(userShortName, (GameHandler.point[outerVar][innerVar].getX()) + (rectangleWidth / 2) , (GameHandler.point[outerVar][innerVar].getY()) + (rectangleHeight/2) + (bounds.height()/2), specialTextPaint);
                            }
                            else if (GameHandler.point[outerVar][innerVar].getRectOwner() == 1) {
                                specialTextPaint.getTextBounds(systemShortName,0,systemShortName.length(),bounds);
                                canvas.drawText(systemShortName, (GameHandler.point[outerVar][innerVar].getX()) + (rectangleWidth / 2) , (GameHandler.point[outerVar][innerVar].getY()) + (rectangleHeight/2) + (bounds.height()/2), specialTextPaint);
                            }
                            else if (GameHandler.point[outerVar][innerVar].getRectOwner() == -1) {
                                specialTextPaint.getTextBounds(blockedBoxes,0,blockedBoxes.length(),bounds);
                                canvas.drawText(blockedBoxes, (GameHandler.point[outerVar][innerVar].getX()) + (rectangleWidth / 2) , (GameHandler.point[outerVar][innerVar].getY()) + (rectangleHeight/2) + (bounds.height()/2), specialTextPaint);
                            }
                        }
                    }

                    // to be done in game over
                    if (GameHandler.gameOver) {
                        Paint gameOver1Paint = new Paint();
                        gameOver1Paint.setAntiAlias(true);
                        gameOver1Paint.setStyle(Paint.Style.FILL);
                        gameOver1Paint.setColor(0xFFE7D7CE);
                        Paint gameOver2Paint = new Paint();
                        gameOver2Paint.setAntiAlias(true);
                        gameOver2Paint.setStyle(Paint.Style.FILL);
                        gameOver2Paint.setColor(0xFF3B444B);

                        Paint gameOverButtonPaint = new Paint();
                        gameOverButtonPaint.setAntiAlias(true);
                        gameOverButtonPaint.setStyle(Paint.Style.FILL);
                        gameOverButtonPaint.setColor(0xFFED872D);

                        RectF rect = new RectF((GameHandler.canvasWidth/5) - (float)(76.12 * GameHandler.canvasDensity)
                                , GameHandler.canvasHeight/5
                                , (GameHandler.canvasWidth*4/5) + (float)(76.12 * GameHandler.canvasDensity)
                                , GameHandler.canvasHeight*4/5);

                        canvas.drawRoundRect(rect,
                                (float)(11.42 * GameHandler.canvasDensity),
                                (float)(11.42 * GameHandler.canvasDensity),
                                gameOver2Paint);

                        rect.set((GameHandler.canvasWidth/5) - (float)(83.80 * GameHandler.canvasDensity)
                                ,GameHandler.canvasHeight/5 + (float)(7.62 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasWidth*4/5) + (float)(68.57 * GameHandler.canvasDensity)
                                ,GameHandler.canvasHeight*4/5 + (float)(7.62 * GameHandler.canvasDensity));

                        canvas.drawRoundRect(rect,
                                (float)(11.42 * GameHandler.canvasDensity),
                                (float)(11.42 * GameHandler.canvasDensity),
                                gameOver1Paint);

                        // Button Play-Again
                        rect.set((GameHandler.canvasWidth/4) - (float)(38.1 * GameHandler.canvasDensity)
                                ,GameHandler.canvasHeight*3/5
                                ,(GameHandler.canvasWidth/4) + (float)(95.23 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasHeight*3/5) + (float)(64.76 * GameHandler.canvasDensity));

                        canvas.drawRoundRect(rect,
                                (float)(11.42 * GameHandler.canvasDensity),
                                (float)(11.42 * GameHandler.canvasDensity),
                                gameOver2Paint);

                        rect.set((GameHandler.canvasWidth/4) - (float)(43.80 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasHeight*3/5) + (float)(5.714 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasWidth/4) + (float)(89.52 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasHeight*3/5) + (float)(70.47 * GameHandler.canvasDensity));

                        canvas.drawRoundRect(rect,
                                (float)(11.42 * GameHandler.canvasDensity),
                                (float)(11.42 * GameHandler.canvasDensity),
                                gameOverButtonPaint);

                        canvas.drawText("Play Again",
                                (GameHandler.canvasWidth/4) + (float)(22.85 * GameHandler.canvasDensity),
                                (GameHandler.canvasHeight*3/5) + (float)(45.71 * GameHandler.canvasDensity),
                                textPaint);

                        // Button Main Menu
                        rect.set((GameHandler.canvasWidth*3/4) - (float)(95.23 * GameHandler.canvasDensity)
                                ,GameHandler.canvasHeight*3/5
                                ,(GameHandler.canvasWidth*3/4) + (float)(38.10 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasHeight*3/5) + (float)(64.76 * GameHandler.canvasDensity));

                        canvas.drawRoundRect(rect,
                                (float)(11.42 * GameHandler.canvasDensity),
                                (float)(11.42 * GameHandler.canvasDensity),
                                gameOver2Paint);

                        rect.set((GameHandler.canvasWidth*3/4) - (float)(100.95 * GameHandler.canvasDensity)
                                ,GameHandler.canvasHeight*3/5 + (float)(5.71 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasWidth*3/4) + (float)(32.38 * GameHandler.canvasDensity)
                                ,(GameHandler.canvasHeight*3/5) + (float)(70.47 * GameHandler.canvasDensity));

                        canvas.drawRoundRect(rect,
                                (float)(11.42 * GameHandler.canvasDensity),
                                (float)(11.42 * GameHandler.canvasDensity),
                                gameOverButtonPaint);

                        canvas.drawText("Home",
                                (GameHandler.canvasWidth*3/4) - (float)(34.28 * GameHandler.canvasDensity),
                                (GameHandler.canvasHeight*3/5) + (float)(45.71 * GameHandler.canvasDensity),
                                textPaint);

                        if (GameHandler.user1Score > GameHandler.user2Score) {
                            canvas.drawText("Congrats.... You Win..."
                                    , (GameHandler.canvasWidth/2)
                                    , GameHandler.canvasHeight/5 + (float)(49.52 * GameHandler.canvasDensity)
                                    , textPaint);
                        } else {
                            canvas.drawText("Lets Play Again..."
                                    , (GameHandler.canvasWidth/2)
                                    , GameHandler.canvasHeight/5 + (float)(49.52 * GameHandler.canvasDensity)
                                    , textPaint);
                        }

                        canvas.drawText(userName + "'s Score = " + GameHandler.user1Score + " Boxes."
                                , (GameHandler.canvasWidth/2)
                                , GameHandler.canvasHeight/5 + (float)(83.80 * GameHandler.canvasDensity)
                                , textPaint);
                        canvas.drawText(systemUser + "'s Score = " + GameHandler.user2Score + " Boxes."
                                , (GameHandler.canvasWidth/2)
                                , GameHandler.canvasHeight/5 + (float)(114.285 * GameHandler.canvasDensity)
                                , textPaint);

                        if (GameHandler.user1Score > GameHandler.user2Score) {
                            canvas.drawText("You Win by " + (GameHandler.user1Score - GameHandler.user2Score) + " Boxes."
                                    , (GameHandler.canvasWidth/2)
                                    , GameHandler.canvasHeight/5 + (float)(144.76 * GameHandler.canvasDensity)
                                    , textPaint);
                        } else {
                            canvas.drawText("Difference = " + (GameHandler.user2Score - GameHandler.user1Score) + " Boxes."
                                    , (GameHandler.canvasWidth/2)
                                    , GameHandler.canvasHeight/5 + (float)(144.76 * GameHandler.canvasDensity)
                                    , textPaint);
                        }
                    }
                    screenHolder.unlockCanvasAndPost(canvas);
                    requestRefresh = false;
                }
                else {
                    //Log.d("Refresh Display","False");
                    if (GameHandler.refresh_display) {
                        //Log.d("Refresh Display","True");
                        requestRefresh = true;
                    }
                }
            }
            if (LOG_LEVEL > 0) {
                LOG_LEVEL--;
            }
        }
    }
}
