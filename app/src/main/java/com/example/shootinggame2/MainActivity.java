package com.example.shootinggame2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.shootinggame2.Model.Bullet;
import com.example.shootinggame2.Model.Enemy;
import com.example.shootinggame2.Model.Game;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //----------------------------------------------------------------------------
    // UI References.
    //

    private LinearLayout infoLayout;
    private Button start;
    private SeekBar seekBar;
    private Button btnShoot;
    private ImageView spaceship;
    private FrameLayout skyLayout;
    private int realDisplayHeight;
    private int realDisplayWidth;

    private Game game;

    // Game
    private int lifeLimit = 3;
    private int bulletLimit = 5;

    private Timer timer;
    private ImageView[] lifeViews;
    private HashMap<Integer, ImageView> enemyViews;
    private HashMap<Integer, ImageView> bulletViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer = new Timer();
        game = Game.getInstance();

        //UI 세팅
        setUpUI();
        //실제 화면 크기 변수 세팅
        setDisplayVariables();

        /**
         * start 버튼 클릭 -> 게임 시작
         */
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);
                //가상 좌표 세팅
                float displayRatio = realDisplayHeight / realDisplayWidth;
                game.setVirtualDisplay(displayRatio);
                //게임 시작 상태 전환
                game.setGameStart(lifeLimit, bulletLimit);
                //게임 진행
                TimerTask stepTimerTask = genStepTimerTask();
                timer.schedule(stepTimerTask, 0, 10);
            }
        });

        /**
         * seekBar 조정 -> cannon 각도 변경
         */
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //cannon 각도 변경
                game.getCannon().setAngle(180 - progress);

                //cannon 이미지뷰 변경
                spaceship.setRotation(progress - 90);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        /**
         * shoot 버튼 클릭 -> bullet 생성
         */
        btnShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.addBullet();
            }
        });
    }

    /**
     *
     */
    private void setDisplayVariables() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        realDisplayWidth = size.x;
        realDisplayHeight = (int) (size.y * 0.8);
    }

    /**
     *
     */
    private void setUpUI() {
        infoLayout = (LinearLayout) findViewById(R.id.info);
        start = (Button) findViewById(R.id.start);
        spaceship = (ImageView) findViewById(R.id.spaceship);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        btnShoot = (Button) findViewById(R.id.btnShoot);
        skyLayout = (FrameLayout) findViewById(R.id.sky_layout);

        setUpLifeViews();
        enemyViews = new HashMap<>();
        bulletViews = new HashMap<>();

    }

    private void setUpLifeViews() {
        lifeViews = new ImageView[lifeLimit];
        for(int i = 0; i < lifeLimit; i++) {
            lifeViews[i] = addLifeImageView();
        }
    }

    /**
     * 각종 ImageView 생성
     */
    private ImageView addLifeImageView() {
        int virtualLifeSize = 7;
        int lifeWidth = (int) (realDisplayWidth / Game.virtualWidth * virtualLifeSize);
        int lifeHeight = (int) (realDisplayHeight / Game.virtualHeight * virtualLifeSize);
        ImageView lifeImage = new ImageView(getApplicationContext());
        lifeImage.setImageResource(R.drawable.heart);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(lifeWidth, lifeHeight);
        lifeImage.setPadding(realDisplayWidth / 100, realDisplayWidth / 100, realDisplayWidth / 100, realDisplayWidth / 100);
        infoLayout.addView(lifeImage, params);
        return lifeImage;
    }

    private ImageView addBulletImageView() {
        int bulletWidth = (int) (realDisplayWidth / Game.virtualWidth * Bullet.width);
        int bulletHeight = (int) (realDisplayHeight / Game.virtualHeight * Bullet.height);
        ImageView bulletImage = new ImageView(getApplicationContext());
        bulletImage.setImageResource(R.drawable.bullet);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bulletWidth, bulletHeight);
        skyLayout.addView(bulletImage, params);
        return bulletImage;
    }

    private ImageView addEnemyImageView() {
        int enemyWidth = (int) (realDisplayWidth / Game.virtualWidth * Enemy.width);
        int enemyHeight = (int) (realDisplayHeight / Game.virtualHeight * Enemy.height);
        ImageView enemyImage = new ImageView(getApplicationContext());
        enemyImage.setImageResource(R.drawable.monster);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(enemyWidth, enemyHeight);
        skyLayout.addView(enemyImage, params);
        return enemyImage;
    }

    /**
     * 특정 시간 마다 호출되는 TimerTask 생성
     * @return
     */
    private TimerTask genStepTimerTask() {
        TimerTask stepTimerTask = new TimerTask() {
            @Override
            public void run() {

                //(2) game update : bullet, enemy 위치 업데이트
                game.update();

                //(3) bullet, enemy view update
                updateViews();

                //(4) 게임 종료 조건 확인
                if(!game.isRunning()) {
                    timer.cancel();
                    readyForRestart();
                }
            }
        };
        return stepTimerTask;
    }

    /**
     * 화면 정리
     */
    private void clearViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                skyLayout.removeAllViews();
                infoLayout.removeAllViews();
                start.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 각종 ImageView update
     */
    private void updateViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateLifeImageView();
                updateBulletPosition();
                updateEnemyPosition();
            }
        });
    }

    private void updateLifeImageView() {
        int life = game.getLife();
        for(int i = 0; i < lifeLimit; i++) {
            if(i < life) {
                lifeViews[i].setVisibility(View.VISIBLE);
            }
            else {
                lifeViews[i].setVisibility(View.GONE);
            }
        }
    }

    private void updateBulletPosition() {
        for(int i = 0; i < Game.maxBulletId; i++) {
            Bullet b = game.getBullet(i);
            if(b != null) {
                if(!bulletViews.containsKey(i)) {
                    bulletViews.put(i, addBulletImageView());
                }
                ImageView bulletImage = bulletViews.get(i);
                bulletImage.setX(positionXtoViewPosition(b.getX()));
                bulletImage.setY(positionYtoViewPosition(b.getY()));
            }
            else {
                if(bulletViews.containsKey(i)) {
                    bulletViews.get(i).setVisibility(View.GONE);
                    bulletViews.remove(i);
                }
            }
        }
    }

    private void updateEnemyPosition() {
        for(int i = 0; i < Game.maxEnemyId; i++) {
            Enemy e = game.getEnemy(i);
            if(e != null) {
                if(!enemyViews.containsKey(i)) {
                    enemyViews.put(i, addEnemyImageView());
                }
                ImageView enemyImage = enemyViews.get(i);
                enemyImage.setX(positionXtoViewPosition(e.getX()));
                enemyImage.setY(positionYtoViewPosition(e.getY()));
            }
            else {
                if(enemyViews.containsKey(i)) {
                    enemyViews.get(i).setVisibility(View.GONE);
                    enemyViews.remove(i);
                }
            }
        }
    }



    /**
     * 가상 X좌표 -> 실제 X좌표로 변환
     * @param beforeX
     * @return
     */
    private float positionXtoViewPosition(float beforeX) {
        float afterX = (realDisplayWidth / 100) * beforeX;
        return afterX;
    }

    /**
     * 가상 Y좌표 -> 실제 Y좌표로 변환
     * @param beforeY
     * @return
     */
    private float positionYtoViewPosition(float beforeY) {
        float afterY = (realDisplayHeight / 100) * beforeY;
        return afterY;
    }

    /**
     * 재시작 선택 화면 준비
     */
    private void readyForRestart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                start.setText("restart");
                start.setVisibility(View.VISIBLE);
            }
        });
    }
}