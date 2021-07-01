package com.example.shootinggame2.Model;

public class Enemy extends Item {
    public static final int height = 10;
    public static final int width = 10;
    public Enemy(int id) {
        super(id);
        this.dx = 0;
        this.dy = (float) (Math.random() * 0.05 + 0.1);
        this.x = (float) (Math.random() * (100 - width));
        this.y = 0;
    }

    @Override
    public void move() {
        x += dx;
        y += dy;
    }

}
