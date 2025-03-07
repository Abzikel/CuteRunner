package com.abzikel.pojos;

public class Cloud {
    public int positionX, positionY;
    public double scale, speed;

    public Cloud(int positionX, int positionY, double scale, double speed) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.scale = scale;
        this.speed = speed;
    }
}
