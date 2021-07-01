package com.example.shootinggame2.Model;

public class Bullet extends Item {
    public static final int height = 4;
    public static final int width = 4;
    private int angle;
    private int reflection;

    public Bullet(int id, int angle) {
        super(id);
        this.angle = angle;
        this.x = 50 - (width / 2);
        this.y = 100 - height;
        this.dx = angle > 90 ? -1 : 1;
        this.dy = (float) (-Math.tan(Math.toRadians(angle)) * dx);
        this.reflection = 0;
    }

    @Override
    public void move() {
        if(x == 100 - width || x == 0) { //벽에 부딪히면
            dx = -dx;
            reflection++;
        }
        x += dx;
        y += dy;
    }

    public boolean bounced() { return (reflection >= 1); }

}
