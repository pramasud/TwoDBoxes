package com.anusha.twodboxes;

/**
 * Created by Lily on 9/25/2016.
 */
public class PointHandler {
    int x = 0,y = 0;
    boolean vertLine=false, horzLine=false;
    boolean isRectangle=false;
    int rectAvailLine=0;
    int rectOwner=10;

    public void DataHandler (int xAxis, int yAxis) {
        x=xAxis;
        y=yAxis;
    }

    public void resetAllValues() {
        x = 0;
        y = 0;
        vertLine=false;
        horzLine=false;
        isRectangle=false;
        rectAvailLine=0;
        rectOwner=10;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setX(int xAxis){
        x = xAxis;
    }

    public void setY(int yAxis){
        y = yAxis;
    }

    public void setVertLine(boolean vLine){
        vertLine = vLine;
    }

    public void setHorzLine(boolean hLine){
        horzLine = hLine;
    }

    public boolean getHorzLine(){
        return horzLine;
    }

    public boolean getVertLine(){
        return vertLine;
    }

    public void setIsRectangle(boolean rectangle){
        isRectangle = rectangle;
    }

    public boolean getIsRectangle(){
        return isRectangle;
    }

    public void setRectAvailLine(int rectLines){
        rectAvailLine = rectLines;
    }

    public int getRectAvailLine(){
        return rectAvailLine;
    }

    public void setRectOwner(int ownerRect){
        rectOwner = ownerRect;
    }

    public int getRectOwner(){
        return rectOwner;
    }
}
