package com.example.shootinggame2.Model;

public class Cannon {
    //----------------------------------------------------------------------------
    // Instance variables.
    //

    private int angle;


    //----------------------------------------------------------------------------
    // Singleton pattern.
    //

    private static Cannon cannon;

    public static Cannon getInstance() {
        if(cannon == null) {
            cannon = new Cannon();
        }
        return cannon;
    }


    //----------------------------------------------------------------------------
    // Constructor.
    //

    private Cannon() {
        this.angle = 90;
    }


    //----------------------------------------------------------------------------
    // Public interface.
    //

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }
}
