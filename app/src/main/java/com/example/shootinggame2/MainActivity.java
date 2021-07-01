package com.example.shootinggame2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.shootinggame2.Model.Bullet;
import com.example.shootinggame2.Model.Enemy;
import com.example.shootinggame2.Model.Game;

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

    private Game game;

    // Game
    private Timer timer;

    //
    private int step = 0;
    private int nextEnemyStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpUI();

        MyDisplay.setup(getWindowManager().getDefaultDisplay());

        timer = new Timer();
        game = Game.getInstance();

        /**
         * start 버튼 클릭 -> 게임 시작
         */
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //게임 세팅
                game.start(3, 5);

                TimerTask stepTimerTask = getStepTimerTask();
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

    private void setUpUI() {
        infoLayout = (LinearLayout) findViewById(R.id.info);
        start = (Button) findViewById(R.id.start);
        spaceship = (ImageView) findViewById(R.id.spaceship);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        btnShoot = (Button) findViewById(R.id.btnShoot);
        skyLayout = (FrameLayout) findViewById(R.id.sky_layout);
    }

    /**
     * 10ms 마다 호출되는 TimerTask
     * @return
     */
    private TimerTask getStepTimerTask() {
        TimerTask stepTimerTask = new TimerTask() {
            @Override
            public void run() {
                //(1) 화면 초기화
                clearViews();

                //(2) game update : bullet, enemy 위치 업데이트
                game.update();

                //(3) enemy 생성 여부 결정
                if(step == nextEnemyStep) {
                    game.addEnemy();
                    nextEnemyStep = (int)(Math.random() * 300 + 50) + nextEnemyStep; //랜덤한 간격으로 호출하기 위함
                }

                //(4) bullet, enemy view update
                updateViews();

                //(5) 게임 종료 조건 확인
                if(checkEndCondition()) {
                    this.cancel();
                    readyForRestart();
                }
                step++;
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
        for(int i = 0; i < life; i++) {
            addLifeImageView();
        }
    }

    private void updateBulletPosition() {
        for(int i = 0; i < 10; i++) {
            Bullet b = game.getBullet(i);
            if(b != null) {
                ImageView bulletImageView = addBulletImageView();
                bulletImageView.setX(positionXtoViewPosition(b.getX()));
                bulletImageView.setY(positionYtoViewPosition(b.getY()));
            }
        }
    }

    private void updateEnemyPosition() {
        for(int i = 0; i < 30; i++) {
            Enemy e = game.getEnemy(i);
            if(e != null) {
                ImageView enemyImageView = addEnemyImageView();
                enemyImageView.setX(positionXtoViewPosition(e.getX()));
                enemyImageView.setY(positionYtoViewPosition(e.getY()));
            }
        }
    }

    /**
     * 각종 ImageView 생성
     */
    private void addLifeImageView() {
        ImageView heart = new ImageView(getApplicationContext());
        heart.setImageResource(R.drawable.heart);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(MyDisplay.width / 100 * 7, MyDisplay.width / 100 * 7);
        heart.setPadding(MyDisplay.width / 100, MyDisplay.width / 100, MyDisplay.width / 100, MyDisplay.width / 100);
        infoLayout.addView(heart, params);
    }

    private ImageView addBulletImageView() {
        ImageView bulletImage = new ImageView(getApplicationContext());
        bulletImage.setImageResource(R.drawable.bullet);
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(MyDisplay.width / 100 * Bullet.width, MyDisplay.width / 100 * Bullet.width);
        skyLayout.addView(bulletImage, param);
        return bulletImage;
    }

    private ImageView addEnemyImageView() {
        ImageView enemyImage = new ImageView(getApplicationContext());
        enemyImage.setImageResource(R.drawable.monster);
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(MyDisplay.width / 100 * Enemy.width, MyDisplay.width / 100 * Enemy.width);

        skyLayout.addView(enemyImage, param);
        return enemyImage;
    }

    /**
     * 종료 조건 확인 : 생명 개수 확인
     * @return
     */
    private boolean checkEndCondition() {
        int life = game.getLife();
        if(life <= 0) {
            return true;
        }
        return false;
    }

    /**
     * 가상 X좌표 -> 실제 X좌표로 변환
     * @param beforeX
     * @return
     */
    private float positionXtoViewPosition(float beforeX) {
        float afterX = (MyDisplay.width / 100) * beforeX;
        return afterX;
    }

    /**
     * 가상 Y좌표 -> 실제 Y좌표로 변환
     * @param beforeY
     * @return
     */
    private float positionYtoViewPosition(float beforeY) {
        float afterY = (MyDisplay.height / 100) * beforeY;
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