package com.example.shootinggame2.Model;

import com.example.shootinggame2.MyDisplay;

import java.util.HashMap;

public class Game {
    //----------------------------------------------------------------------------
    // Constant definitions.
    //

    public static final int virtual_width = 100;
    public static final int virtual_height = virtual_width * (MyDisplay.height / MyDisplay.width);

    //----------------------------------------------------------------------------
    // class variables.
    //

    //----------------------------------------------------------------------------
    // Instance variables.
    //

    private Cannon cannon;
    private HashMap<Integer, Bullet> bulletHashMap;
    private HashMap<Integer, Enemy> enemyHashMap;
    private boolean running = false;

    private int life;
    private int bulletLimit;

    private int bulletId;
    private int enemyId;

    //----------------------------------------------------------------------------
    // Singleton Pattern.
    //

    private static Game game;

    public static Game getInstance() {
        if(game == null) {
            game = new Game();
        }
        return game;
    }

    //----------------------------------------------------------------------------
    // Life cycle management.
    //

    private Game() {
        cannon = Cannon.getInstance();
    };

    //-----------------------------------------------------------------------------
    // Public interface.
    //

    /**
     * 게임 시작시 호출 -> 초기화
     * @param lifeLimit
     * @param bulletLimit
     */
    public void start(int lifeLimit, int bulletLimit) {
        this.running = true;
        this.life = lifeLimit;
        this.bulletLimit = bulletLimit;
        this.bulletId = 0;
        this.enemyId = 0;
        this.bulletHashMap = new HashMap<>();
        this.enemyHashMap = new HashMap<>();
    }

    /**
     * TimerTask 내에서 호출되며 화면 상에 존재하는 bullet과 enemy 위치 조정
     */
    public void update() {
        //bullet 위치 이동
        updateBulletsPosition();
        //enemy 위치 이동
        updateEnemiesPosition();
        //충돌 감지
        checkConflict();
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Bullet getBullet(int id) {
        return bulletHashMap.get(id);
    }

    public Enemy getEnemy(int id) {
        return enemyHashMap.get(id);
    }

    /**
     * 현재 남은 생명 개수 리턴
     * @return
     */
    public int getLife() {
        return life;
    }

    /**
     * shoot버튼 클릭 시 호출 -> Bullet 추가
     * 단, 화면 상에 존재하는 bullet 개수가 특정 개수(bulletLimit) 미만일 때만 추가된다.
     */
    public void addBullet() {
        if(bulletHashMap.size() >= bulletLimit) return;
        int id = genBulletId();
        Bullet bullet = new Bullet(id, cannon.getAngle());
        bulletHashMap.put(id, bullet);
    }

    /**
     * 랜덤한 간격으로 timertask에서 호출 -> Enemy 추가
     */
    public void addEnemy() {
        int id = genEnemyId();
        Enemy enemy = new Enemy(id);
        enemyHashMap.put(id, enemy);
    }

    //----------------------------------------------------------------------
    // Internal support methods.
    //

    /**
     * Bullet 위치 이동 (단위 벡터 만큼 이동)
     */
    private void updateBulletsPosition() {
        for(int i = 0; i < 10; i++) {
            if(bulletHashMap.containsKey(i)) {
                Bullet b = bulletHashMap.get(i);
                b.move();
                if(b.getY() < 0) { //bullet이 화면 밖으로 벗어나면
                    bulletHashMap.remove(i);
                }
            }
        }
    }

    /**
     * Enemy 위치 이동 (단위 벡터 만큼 이동)
     */
    private void updateEnemiesPosition() {
        for(int i = 0; i < 30; i++) {
            if(enemyHashMap.containsKey(i)) {
                Enemy e = enemyHashMap.get(i);
                e.move();
                if(e.getY() > virtual_height) { //enemy가 땅에 닿으면
                    enemyHashMap.remove(i);
                    decreaseLife();
                }
            }
        }
    }

    /**
     * 충돌 감지
     */
    private void checkConflict() {
        for(int eid = 0; eid < 30; eid++) {
            if(enemyHashMap.containsKey(eid)) {
                Enemy e = enemyHashMap.get(eid);
                float ex = e.getX();
                float ey = e.getY();
                int bid = findConflictingBullet(ex, ey);
                if(bid >= 0) {
                    enemyHashMap.remove(eid);
                    bulletHashMap.remove(bid);
                }
            }
        }
    }

    /**
     * 입력 받은 enemy 좌표와 충돌하는 bullet이 존재하면 리턴
     * @param ex : enemy의 x좌표
     * @param ey : enemy의 y좌표
     * @return
     */
    private int findConflictingBullet(float ex, float ey) {
        for(int bid = 0; bid < 10; bid++) {
            if(bulletHashMap.containsKey(bid)) {
                Bullet b = bulletHashMap.get(bid);
                if(!b.bounced()) continue;
                float bx = b.getX();
                float by = b.getY();
                if(checkConflict(ex, ey, bx, by)) {
                    return bid;
                }
            }
        }
        return -1;
    }

    /**
     * 해당하는 enemy와 bullet의 좌표가 충돌하는지(중첩되는지) 확인
     * @param ex : enemy의 x좌표
     * @param ey : enemy의 y좌표
     * @param bx : bullet의 x좌표
     * @param by : bullet의 y좌표
     * @return
     */
    private boolean checkConflict(float ex, float ey, float bx, float by) {
        if(bx < ex + Enemy.width && bx + Bullet.width > ex) {
            if(by < ey + Enemy.height && by + Bullet.height > ey) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bullet id 생성
     * @return
     */
    private int genBulletId() {
        while(bulletHashMap.containsKey(bulletId)) {
            bulletId = (bulletId + 1) % 10;
        }
        return bulletId;
    }

    /**
     * Enemy id 생성
     * @return
     */
    private int genEnemyId() {
        while(enemyHashMap.containsKey(enemyId)) {
            enemyId = (enemyId + 1) % 30;
        }
        return enemyId;
    }

    private void decreaseLife() {
        life--;
        if(life == 0) {
            running = false;
        }
    }

    private boolean isRunning() {
        return running;
    }

}
