package com.example.shootinggame2.Model;

public class Enemy extends Item {
    //----------------------------------------------------------------------------
    // Constant definitions.
    //

    public static final int height = 10;
    public static final int width = 10;

    //----------------------------------------------------------------------------
    // Constructor.
    //

    public Enemy(int id) {
        super(id);

        //위치좌표
        this.x = (float) (Math.random() * (Game.virtualWidth - width));
        this.y = 0;

        //unit vector
        this.dx = 0;
        this.dy = (float) (Math.random() * 0.05 + 0.1);
    }

    //----------------------------------------------------------------------------
    // Public interface.
    //

    /**
     * Timertask 내에서 주기적으로 호출되며 enemy의 위치가 dx, dy 만큼 이동함
     */
    @Override
    public void move() {
        x += dx;
        y += dy;
    }

}
