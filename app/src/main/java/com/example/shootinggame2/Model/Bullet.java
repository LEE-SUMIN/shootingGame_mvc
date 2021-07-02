package com.example.shootinggame2.Model;

public class Bullet extends Item {

    //----------------------------------------------------------------------------
    // Constant definitions.
    //

    public static final int height = 4; // 화면 너비를 100으로 했을 때 기준 크기
    public static final int width = 4;


    //----------------------------------------------------------------------------
    // Instance variables.
    //
    //반사 횟수
    private int reflection;

    //----------------------------------------------------------------------------
    // Constructor.
    //

    public Bullet(int id, int angle) {
        super(id);
        
        // 위치 좌표
        this.x = (Game.virtualWidth / 2f) - (width / 2f);
        this.y = Game.virtualHeight - height;
        
        // unit vector
        this.dx = angle > 90 ? -1 : 1;
        this.dy = (float) (-Math.tan(Math.toRadians(angle)) * dx);
        

        this.reflection = 0;
    }

    //----------------------------------------------------------------------------
    // Public interface.
    //

    /**
     * TimerTask 내에서 주기적으로 호출되며 bullet의 위치가 dx, dy 만큼 이동함
     */
    @Override
    public void move() {
        if(x == Game.virtualWidth - width || x == 0) { //벽에 부딪히면 이동 방향 바뀜
            dx = -dx;
            reflection++;
        }
        x += dx;
        y += dy;
    }

    /**
     * 1회 이상 반사되어 enemy를 죽일 수 있는 bullet인지 확인
     * @return
     */
    public boolean bounced() { return (reflection >= 1); }

}
