package com.example.shootinggame2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
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
    // Constant definitions.
    //

    private final int lifeLimit = 3; // 게임 내에서 갖는 생명 개수
    private final int bulletLimit = 5; // 한 화면 상에 존재할 수 있는 최대 bullet 개수


    //----------------------------------------------------------------------------
    // UI References.
    //

    private LinearLayout infoLayout;
    private Button start;
    private SeekBar seekBar;
    private Button btnShoot;
    private ImageView spaceship;
    private FrameLayout skyLayout;

    private ImageView[] lifeViews;
    private HashMap<Integer, ImageView> enemyViews;
    private HashMap<Integer, ImageView> bulletViews;

    private int realDisplayHeight;
    private int realDisplayWidth;


    //----------------------------------------------------------------------------
    // Instance variables.
    //

    private Game game; // 게임 객체
    private Timer timer; // 게임 실행용 Timer


    //----------------------------------------------------------------------------
    // Life cycle.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game = Game.getInstance();

        // 실제 화면 크기 변수 세팅
        setDisplayVariables();

        // 가상 좌표계 세팅 : 가로 크기 100을 기준으로 화면 크기에 맞게 설정한다.
        float displayRatio = realDisplayHeight / realDisplayWidth;
        game.setVirtualCoordinates(displayRatio);

        // UI 세팅
        setUpUI();


        // start버튼 클릭 -> 게임 시작
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setVisibility(View.GONE);

                // 게임 시작 준비
                game.setGameStart(lifeLimit, bulletLimit);

                // 게임 시작
                TimerTask stepTimerTask = genStepTimerTask();
                timer.schedule(stepTimerTask, 0, 10);
            }
        });



        // seekBar 조정 -> cannon 각도 변경
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //cannon 각도 변경
                game.getCannon().setAngle(180 - progress);

                //cannon 이미지뷰 회전
                spaceship.setRotation(progress - 90);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        // shoot 버튼 클릭 -> bullet 생성
        btnShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.addBullet();
            }
        });
    }



    //----------------------------------------------------------------------
    // Internal support methods.
    //

    /**
     * 화면 가로 & 세로 크기 변수 세팅
     */
    private void setDisplayVariables() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        realDisplayWidth = size.x;
        realDisplayHeight = (int) (size.y * 0.75);
    }


    /**
     * UI 세팅
     */
    private void setUpUI() {
        infoLayout = (LinearLayout) findViewById(R.id.info);
        start = (Button) findViewById(R.id.start);
        spaceship = (ImageView) findViewById(R.id.spaceship);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        btnShoot = (Button) findViewById(R.id.btnShoot);
        skyLayout = (FrameLayout) findViewById(R.id.sky_layout);

        setUpDynamicUI();
    }


    /**
     * 동적 UI 관련 세팅
     */
    private void setUpDynamicUI() {
        // 동적 ImageView 관리
        setUpLifeViewList();
        enemyViews = new HashMap<>();
        bulletViews = new HashMap<>();
    }


    /**
     * 생명 개수를 나타내는 하트 ImageView 리스트 세팅
     */
    private void setUpLifeViewList() {
        lifeViews = new ImageView[lifeLimit];
        for(int i = 0; i < lifeLimit; i++) {
            lifeViews[i] = addLifeImageView();
        }
    }


    /**
     * 특정 시간 마다 호출되는 TimerTask 생성
     * @return
     */
    private TimerTask genStepTimerTask() {
        TimerTask stepTimerTask = new TimerTask() {
            @Override
            public void run() {
                //(1) game update : bullet, enemy 위치 update
                game.update();
                //TODO: [1] game.step() 이 좀 더 명확한 이름일 것 같음.
                //TODO: [2] game.update() 하면서 어떤 bullet이 사라지고, 어떤 enemy가 사라지고 생기는지 리턴 값으로 받아올 수 있으면
                //TODO:     updateImageViews()가 좀 더 깔끔해 질 수 있을 것.

                //(2) bullet, enemy ImageView update
                updateImageViews();

                //(3) 게임 실행 여부 확인
                if(!game.isRunning()) {
                    timer.cancel();
                    readyForRestart();
                }
            }
        };
        timer = new Timer();

        return stepTimerTask;
    }


    /**
     * 각종 ImageView update
     */
    private void updateImageViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateLifeImageView();
                updateBulletPosition();
                updateEnemyPosition();
            }
        });
    }


    /**
     * 현재 남은 생명 개수만큼 하트 ImageView 보이도록 설정
     */
    private void updateLifeImageView() {
        int life = game.getLife();
        for(int i = 0; i < lifeLimit; i++) {
            // 남은 생명 개수 만큼 보이게
            if(i < life) {
                lifeViews[i].setVisibility(View.VISIBLE);
            }
            // 나머지는 안 보이도록
            else {
                lifeViews[i].setVisibility(View.GONE);
            }
        }
    }


    /**
     * 현재 Bullet 위치로 ImageView 이동
     */
    private void updateBulletPosition() {
        for(int i = 0; i < Game.maxBulletId; i++) {
            Bullet b = game.getBullet(i);

            // id == i 인 Bullet이
            // 화면 상에 존재하는 경우,
            if(b != null) {

                // 이번에 새롭게 생성된 bullet이어서 원래 존재하던 ImageView가 없는 경우 -> 새 ImageView 생성함
                if(!bulletViews.containsKey(i)) {
                    bulletViews.put(i, addBulletImageView());
                }

                // 원래 ImageView가 존재하던 경우 -> 원래 있던 ImageView를 이동 시킴
                ImageView bulletImage = bulletViews.get(i);
                bulletImage.setX(virtualPositionToRealPosition_X(b.getX()));
                bulletImage.setY(virtualPositionToRealPosition_Y(b.getY()));
            }

            // 화면 상에 존재하지 않는 경우,
            else {

                // 이번에 삭제된 bullet이어서 ImageView가 존재하는 경우 -> ImageView 제거함
                if(bulletViews.containsKey(i)) {
                    bulletViews.get(i).setVisibility(View.GONE);
                    bulletViews.remove(i);
                }
            }
        }
    }


    /**
     * 현재 Enemy 위치로 ImageView 이동
     */
    private void updateEnemyPosition() {
        for(int i = 0; i < Game.maxEnemyId; i++) {
            Enemy e = game.getEnemy(i);

            // id == i 인 Enemy가
            // 화면 상에 존재하는 경우,
            if(e != null) {

                // 이번 step에 새롭게 생성된 enemy여서 원래 존재하던 ImageView가 없는 경우 -> 새 ImageView 생성
                if(!enemyViews.containsKey(i)) {
                    enemyViews.put(i, addEnemyImageView());
                }

                // 원래 ImageView가 존재하던 경우 -> 원래 있던 ImageView 이동
                ImageView enemyImage = enemyViews.get(i);
                enemyImage.setX(virtualPositionToRealPosition_X(e.getX()));
                enemyImage.setY(virtualPositionToRealPosition_Y(e.getY()));
            }

            // 화면 상에 존재하지 않는 경우,
            else {

                // 이번 step에 삭제된 enemy여서 ImageView가 존재하는 경우 -> ImageView 제거
                if(enemyViews.containsKey(i)) {
                    enemyViews.get(i).setVisibility(View.GONE);
                    enemyViews.remove(i);
                }
            }
        }
    }


    /**
     * 생명 ImageView 생성
     * @return : 생성된 ImageView
     */
    private ImageView addLifeImageView() {
        int virtualLifeSize = 7;
        int lifeWidth = (int) (realDisplayWidth / Game.virtualWidth * virtualLifeSize);
        int lifeHeight = (int) (realDisplayHeight / Game.virtualHeight * virtualLifeSize);

        ImageView lifeImage = new ImageView(getApplicationContext());
        lifeImage.setImageResource(R.drawable.heart);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(lifeWidth, lifeHeight);

        int padding = (int) (realDisplayWidth / Game.virtualWidth);
        lifeImage.setPadding(padding, padding, padding, padding);

        infoLayout.addView(lifeImage, params);
        return lifeImage;
    }


    /**
     * Bullet ImageView 생성
     * @return : 생성된 ImageView
     */
    private ImageView addBulletImageView() {
        int bulletWidth = (int) (realDisplayWidth / Game.virtualWidth * Bullet.width);
        int bulletHeight = (int) (realDisplayHeight / Game.virtualHeight * Bullet.height);

        ImageView bulletImage = new ImageView(getApplicationContext());
        bulletImage.setImageResource(R.drawable.bullet);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bulletWidth, bulletHeight);

        skyLayout.addView(bulletImage, params);
        return bulletImage;
    }


    /**
     * Enemy ImageView 생성
     * @return : 생성된 ImageView
     */
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
     * 가상 X좌표 -> 실제 X좌표로 변환
     * @param virtualX : 가상 X 좌표
     * @return : 변환된 실제 X 좌표
     */
    private float virtualPositionToRealPosition_X(float virtualX) {
        return (realDisplayWidth / Game.virtualWidth) * virtualX;
    }


    /**
     * 가상 Y좌표 -> 실제 Y좌표로 변환
     * @param virtualY : 가상 Y 좌표
     * @return : 변환된 실제 Y 좌표
     */
    private float virtualPositionToRealPosition_Y(float virtualY) {
        return (realDisplayHeight / Game.virtualHeight) * virtualY;
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