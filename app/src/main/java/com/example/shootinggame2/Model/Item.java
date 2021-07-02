package com.example.shootinggame2.Model;

public abstract class Item {

    protected final int id;
    protected float dx;
    protected float dy;
    protected float x;
    protected float y;
    protected boolean alive;

    public Item(int id) {
        this.id = id;
        this.alive = true;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public abstract void move();
}
