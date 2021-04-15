package com.anusha.twodboxes;

/**
 * Created by Lily on 9/25/2016.
 */
public class LineHandler {
    int x = 0,y = 0;
    int x1 = 0,y1 = 0;

    int boxNum = 999;

    int xP = 0,yP = 0;
    int x1P = 0,y1P = 0;

    public void LineHandler ()
    {
        x = 0;
        y = 0;
        x1 = 0;
        y1 = 0;
    }

    public void reSetLine() {
        x = 0;
        y = 0;
        x1 = 0;
        y1 = 0;
        xP = 0;
        yP = 0;
        x1P = 0;
        y1P = 0;
        boxNum = 999;
    }

    public void setLine(int startX, int startY, int endX, int endY) {
        x = startX;
        y = startY;
        x1 = endX;
        y1 = endY;
        boxNum = 999;
    }

    public void setLineNBox(int startX, int startY, int endX, int endY, int boxNo) {
        x = startX;
        y = startY;
        x1 = endX;
        y1 = endY;
        boxNum = boxNo;
    }

    public void setLineP(int startX, int startY, int endX, int endY) {
        xP = startX;
        yP = startY;
        x1P = endX;
        y1P = endY;
    }

    public int getX() {
        return x;
    }

    public int getBoxNum() {
        return boxNum;
    }

    public int getY() {
        return y;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getXP() {
        return xP;
    }

    public int getYP() {
        return yP;
    }

    public int getX1P() {
        return x1P;
    }

    public int getY1P() {
        return y1P;
    }
}
