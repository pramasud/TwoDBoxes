package com.anusha.twodboxes;

import android.util.Log;

import java.util.Random;


/**
 * Created by Lily on 9/26/2016.
 */
public class AutoMoveHandler {

    private int LOG_LEVEL = 0;

    private void log(String msg, int level) {
        if (level <= LOG_LEVEL) {
            Log.d("AutoMoveHandler:Log lvl" + LOG_LEVEL, msg);
        }
    }

    public boolean checkReadyBoxes() {
        int outerVar , innerVar;
        int boxesFound;
        for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
            for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                // Horizontal Line
                if ( outerVar < GameHandler.numXDots - 1 ) {
                    if (!GameHandler.point[outerVar][innerVar].getHorzLine()) {
                        boxesFound = findBoxes(outerVar,innerVar,outerVar+1,innerVar);
                        if (boxesFound > 0) {
                            return true;
                        }
                    }
                }
                // vertical Line
                if (innerVar < GameHandler.numYDots - 1) {
                    if (!GameHandler.point[outerVar][innerVar].getVertLine()) {
                        boxesFound = findBoxes(outerVar,innerVar,outerVar,innerVar+1);
                        if (boxesFound > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void getAutoMove() {
        int outerVar, innerVar;
        int boxesFound;
        int lineCount = 0;
        LineHandler[][] lineBoxHorz = new LineHandler[50][50];
        LineHandler[][] lineBoxVert = new LineHandler[50][50];
        LineHandler[] lineMinBox = new LineHandler[2500];
        Random rand = new Random();
        int randNumber;
        //boolean autoLinesFound = false;

        resetTempBoxes();
        log("Searching for Ready Boxes",1);
        for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
            for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                // Horizontal Line
                if ( outerVar < GameHandler.numXDots - 1 ) {
                    if (!GameHandler.point[outerVar][innerVar].getHorzLine()) {
                        boxesFound = findBoxes(outerVar,innerVar,outerVar+1,innerVar);
                        if (boxesFound > 0) {
                            GameHandler.startNewLineX = outerVar;
                            GameHandler.startNewLineY = innerVar;
                            GameHandler.endNewLineX = outerVar + 1;
                            GameHandler.endNewLineY = innerVar;
                            //autoLinesFound = true;
                            return;
                        }
                    }
                }
                // vertical Line
                if (innerVar < GameHandler.numYDots - 1) {
                    if (!GameHandler.point[outerVar][innerVar].getVertLine()) {
                        //log("Vertical BoxesFound = " + boxesFound + " outerVar " + outerVar + " innerVar " + innerVar, 1);
                        boxesFound = findBoxes(outerVar,innerVar,outerVar,innerVar+1);
                        if (boxesFound > 0) {
                            GameHandler.startNewLineX = outerVar;
                            GameHandler.startNewLineY = innerVar;
                            GameHandler.endNewLineX = outerVar;
                            GameHandler.endNewLineY = innerVar + 1;
                            //autoLinesFound = true;
                            return;
                        }
                    }
                }
            }
        }
        log("No Ready Boxes Found. Will have to it the hard way",1);
        int minBoxes = 1000;
        int newBoxesByTempLine;

        for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
            for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                lineBoxHorz[outerVar][innerVar] = new LineHandler();
                lineBoxVert[outerVar][innerVar] = new LineHandler();

                if ( outerVar < GameHandler.numXDots - 1 ) {
                    if (!GameHandler.point[outerVar][innerVar].getHorzLine()) {
                        GameHandler.tempPoint[outerVar][innerVar].setHorzLine(true);
                        newBoxesByTempLine = checkBoxCreation();
                        lineBoxHorz[outerVar][innerVar].setLineNBox(outerVar,innerVar,outerVar + 1,innerVar,newBoxesByTempLine);

                        if (minBoxes > newBoxesByTempLine) {
                            minBoxes = newBoxesByTempLine;
                        }
                    }
                }
                resetTempBoxes();
                if (innerVar < GameHandler.numYDots - 1) {
                    if (!GameHandler.point[outerVar][innerVar].getVertLine()) {
                        GameHandler.tempPoint[outerVar][innerVar].setVertLine(true);
                        newBoxesByTempLine = checkBoxCreation();
                        lineBoxVert[outerVar][innerVar].setLineNBox(outerVar,innerVar,outerVar,innerVar + 1,newBoxesByTempLine);
                        if (minBoxes > newBoxesByTempLine) {
                            minBoxes = newBoxesByTempLine;
                        }
                    }
                }
                resetTempBoxes();
            }
        }
        log("Found Some Lines. Min Boxes = " + minBoxes,1);
        for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
            for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                lineMinBox[lineCount] = new LineHandler();
                if (lineBoxHorz[outerVar][innerVar].getBoxNum() == minBoxes) {
                    lineMinBox[lineCount] = lineBoxHorz[outerVar][innerVar];
                    lineCount++;
                }
                if (lineBoxVert[outerVar][innerVar].getBoxNum() == minBoxes) {
                    lineMinBox[lineCount] = lineBoxVert[outerVar][innerVar];
                    lineCount++;
                }
            }
        }
        log("Number of Lines having Min Boxes = " + lineCount,0);
        if (lineCount > 0) {
            randNumber = rand.nextInt(lineCount);
            GameHandler.startNewLineX = lineMinBox[randNumber].getX();
            GameHandler.startNewLineY = lineMinBox[randNumber].getY();
            GameHandler.endNewLineX = lineMinBox[randNumber].getX1();
            GameHandler.endNewLineY = lineMinBox[randNumber].getY1();
        }
    }

    public int checkBoxCreation() {
        int totalBoxesAllMoves = 0;
        int boxesOneMove;
        int outerVar, innerVar;
        for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
            for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                if ( outerVar < GameHandler.numXDots - 1 ) {
                    if (!(GameHandler.point[outerVar][innerVar].getHorzLine() || GameHandler.tempPoint[outerVar][innerVar].getHorzLine())) {
                        boxesOneMove = findBoxes(outerVar,innerVar,outerVar+1,innerVar);
                        if (boxesOneMove > 0) {
                            totalBoxesAllMoves = totalBoxesAllMoves + boxesOneMove;
                            outerVar = 0;
                            innerVar = -1;
                            continue;
                        }
                    }
                }
                // vertical Line
                if (innerVar < GameHandler.numYDots - 1) {
                    if (!(GameHandler.point[outerVar][innerVar].getVertLine() || GameHandler.tempPoint[outerVar][innerVar].getVertLine())) {
                        boxesOneMove = findBoxes(outerVar,innerVar,outerVar,innerVar+1);
                        if (boxesOneMove > 0) {
                            totalBoxesAllMoves = totalBoxesAllMoves + boxesOneMove;
                            outerVar = 0;
                            innerVar = -1;
                        }
                    }
                }
            }
        }
        return totalBoxesAllMoves;
    }

    public void resetTempBoxes() {
        int outerVar, innerVar;
        for (outerVar = 0; outerVar < GameHandler.numXDots; outerVar++) {
            for (innerVar = 0; innerVar < GameHandler.numYDots; innerVar++) {
                GameHandler.tempPoint[outerVar][innerVar].resetAllValues();
            }
        }
    }

    public int findBoxes(int x, int y, int x1, int y1) {
        int numBoxesFound = 0;

        // Check if the passed Line Competes a Box
        // Check if Vertical
        if (x == x1) {
            // checking for box on the Right Side
            if (x < GameHandler.numXDots) {
                if (y < y1) {
                    if ((GameHandler.point[x][y].getHorzLine() || GameHandler.tempPoint[x][y].getHorzLine())
                            && (GameHandler.point[x1][y1].getHorzLine() || GameHandler.tempPoint[x1][y1].getHorzLine())
                            && (GameHandler.point[x + 1][y].getVertLine() || GameHandler.tempPoint[x + 1][y].getVertLine())) {
                        GameHandler.tempPoint[x][y].setVertLine(true);
                        GameHandler.tempPoint[x][y].setIsRectangle(true);
                        GameHandler.tempPoint[x][y].setRectOwner(GameHandler.moveOwner);
                        numBoxesFound++;
                    }
                } else {
                    if ((GameHandler.point[x1][y1].getHorzLine() || GameHandler.tempPoint[x1][y1].getHorzLine())
                            && (GameHandler.point[x][y].getHorzLine() || GameHandler.tempPoint[x][y].getHorzLine())
                            && (GameHandler.point[x1 + 1][y1].getVertLine() || GameHandler.tempPoint[x1 + 1][y1].getVertLine())) {
                        GameHandler.tempPoint[x1][y1].setVertLine(true);
                        GameHandler.tempPoint[x1][y1].setIsRectangle(true);
                        GameHandler.tempPoint[x1][y1].setRectOwner(GameHandler.moveOwner);
                        numBoxesFound++;
                    }
                }
            }
            // checking for box on the Left Side
            // no need to check if x=0
            if (x > 0) {
                if (y < y1) {
                    if ((GameHandler.point[x - 1][y].getHorzLine() || GameHandler.tempPoint[x - 1][y].getHorzLine())
                            && (GameHandler.point[x1 - 1][y1].getHorzLine() || GameHandler.tempPoint[x1 - 1][y1].getHorzLine())
                            && (GameHandler.point[x - 1][y].getVertLine() || GameHandler.tempPoint[x - 1][y].getVertLine())) {
                        GameHandler.tempPoint[x][y].setVertLine(true);
                        GameHandler.tempPoint[x - 1][y].setIsRectangle(true);
                        GameHandler.tempPoint[x - 1][y].setRectOwner(GameHandler.moveOwner);
                        numBoxesFound++;
                    }
                } else {
                    if ((GameHandler.point[x1 - 1][y1].getHorzLine() || GameHandler.tempPoint[x1 - 1][y1].getHorzLine())
                            && (GameHandler.point[x - 1][y].getHorzLine() || GameHandler.tempPoint[x - 1][y].getHorzLine())
                            && (GameHandler.point[x1 - 1][y1].getVertLine() || GameHandler.tempPoint[x1 - 1][y1].getVertLine())) {
                        GameHandler.tempPoint[x1][y1].setVertLine(true);
                        GameHandler.tempPoint[x1 - 1][y1].setIsRectangle(true);
                        GameHandler.tempPoint[x1 - 1][y1].setRectOwner(GameHandler.moveOwner);
                        numBoxesFound++;
                    }
                }
            }
        }
        // Check if Horizontal
        if (y == y1) {
            // checking for box below
            if (x < x1) {
                if((GameHandler.point[x][y].getVertLine() || GameHandler.tempPoint[x][y].getVertLine())
                        && (GameHandler.point[x1][y1].getVertLine() || GameHandler.tempPoint[x1][y1].getVertLine())
                        && (GameHandler.point[x][y + 1].getHorzLine() || GameHandler.tempPoint[x][y + 1].getHorzLine())) {
                    GameHandler.tempPoint[x][y].setIsRectangle(true);
                    GameHandler.tempPoint[x][y].setHorzLine(true);
                    GameHandler.tempPoint[x][y].setRectOwner(GameHandler.moveOwner);
                    numBoxesFound++;
                }
            }
            else {
                if ((GameHandler.point[x1][y1].getVertLine() || GameHandler.tempPoint[x1][y1].getVertLine())
                        && (GameHandler.point[x][y].getVertLine() || GameHandler.tempPoint[x][y].getVertLine())
                        && (GameHandler.point[x1][y1 + 1].getHorzLine() || GameHandler.tempPoint[x1][y1 + 1].getHorzLine())) {
                    GameHandler.tempPoint[x1][y1].setIsRectangle(true);
                    GameHandler.tempPoint[x1][y1].setHorzLine(true);
                    GameHandler.tempPoint[x1][y1].setRectOwner(GameHandler.moveOwner);
                    numBoxesFound++;
                }
            }
            // checking for box above
            // No Need to check if y=0
            if (y > 0) {
                if (x < x1) {
                    if ((GameHandler.point[x][y - 1].getVertLine() || GameHandler.tempPoint[x][y - 1].getVertLine())
                            && (GameHandler.point[x][y - 1].getHorzLine() || GameHandler.tempPoint[x][y - 1].getHorzLine())
                            && (GameHandler.point[x1][y1 - 1].getVertLine() || GameHandler.tempPoint[x1][y1 - 1].getVertLine())) {
                        GameHandler.tempPoint[x][y - 1].setIsRectangle(true);
                        GameHandler.tempPoint[x][y].setHorzLine(true);
                        GameHandler.tempPoint[x][y - 1].setRectOwner(GameHandler.moveOwner);
                        numBoxesFound++;
                    }
                } else {
                    if ((GameHandler.point[x1][y1 - 1].getVertLine() || GameHandler.tempPoint[x1][y1 - 1].getVertLine())
                            && (GameHandler.point[x1][y1 - 1].getHorzLine() || GameHandler.tempPoint[x1][y1 - 1].getHorzLine())
                            && (GameHandler.point[x][y - 1].getVertLine() || GameHandler.tempPoint[x][y - 1].getVertLine())) {
                        GameHandler.tempPoint[x1][y1 - 1].setIsRectangle(true);
                        GameHandler.tempPoint[x1][y1].setHorzLine(true);
                        GameHandler.tempPoint[x1][y1 - 1].setRectOwner(GameHandler.moveOwner);
                        numBoxesFound++;
                    }
                }
            }
        }
        return numBoxesFound;
    }
}
