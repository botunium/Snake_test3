package com.example.snake_test3;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    //Ds chứa những cục độ dài rắn
    List<SnakeSegments> lstSnakeSegments = new ArrayList<>();

    SurfaceView surfaceView;        //Bề mặt nơi snake sẽ được vẽ.
    TextView txtScore;
    int score = 0;
    AppCompatImageButton btnUp, btnDown, btnLeft, btnRight;

    //SurfaceHolder để vẽ snake lên bề mặt để vẽ đồ họa của SurfaceView. Chứa bề mặt đồ họa nơi rắn được vẽ.
    SurfaceHolder surfaceHolder;

    //SNAKE:
    String currentDirection = "right";   //mặc định khi bắt đầu sẽ di chuyển sang bên "phải"
    static final int segmentSize = 28;  //kích cỡ từng đoạn snake (có thể đổi gtri)
    static final int defaultLength = 3; //mặc định con rắn sẽ dài 3 đoạn
    static final int snakeColor = Color.GREEN;
    static final int snakeSpeed = 800;
    Paint segmentColor = null;          //Màu của từng đoạn rắn, được vẽ bằng đối tượng Paint.


    //Tọa độ của mồi trên màn hình.
    int positionX, positionY;

    //Dùng để điều khiển chuyển động của rắn theo chu kỳ. (giong nhu 1 bo dem)
    Timer timer;

    //Bề mặt để vẽ đồ họa lên surfaceView.
    Canvas canvas = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        txtScore = findViewById(R.id.txtScore);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        //Gọi addCallback(this) để đăng ký lắng nghe sự kiện vẽ lên surfaceView.
        surfaceView.getHolder().addCallback(this);
        initControl();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initControl(){
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!currentDirection.equals("down")){
                    currentDirection = "up";
                }
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!currentDirection.equals("up")){
                    currentDirection = "down";
                }
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!currentDirection.equals("right")){
                    currentDirection = "left";
                }
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!currentDirection.equals("left")){
                    currentDirection = "right";
                }
            }
        });
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;

        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    //Hàm reset mọi thứ về lại default
    private void init(){
        lstSnakeSegments.clear();
        txtScore.setText("0");
        score = 0;
        currentDirection = "right";

        //vị trí mặc định của snake khi bắt đầu (28x3)
        int startPositionX = (segmentSize) * defaultLength;

        //Khởi tạo chiều dài mặc định của rắn khi bắt đầu game
        for(int i = 0; i < defaultLength; i++){
            SnakeSegments snakeSegments = new SnakeSegments(startPositionX, segmentSize);
            lstSnakeSegments.add(snakeSegments);
            //Làm cho các đoạn không chồng lên nhau.
            startPositionX -= (segmentSize * 2);
        }
        addFood();
        moveSnake();
    }

    //Hàm mồi xuất hiện trên màn hình
    private void addFood() {
        //đảm bảo mồi không xuất hiện sát rìa của màn hình
        int surfaceWidth = surfaceView.getWidth() - (segmentSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (segmentSize * 2);

        int randomPositionX = new Random().nextInt(surfaceWidth / segmentSize);
        int randomPositionY = new Random().nextInt(surfaceHeight / segmentSize);

        if((randomPositionX % 2) != 0){
            randomPositionX = randomPositionX + 1;
        }

        if((randomPositionY % 2) != 0){
            randomPositionY = randomPositionY + 1;
        }

        positionX = (segmentSize * randomPositionX) + segmentSize;
        positionY = (segmentSize * randomPositionY) + segmentSize;
    }

    //25:58
    private void moveSnake() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //lấy tọa độ cái đầu con rắn
                int head_positionX = lstSnakeSegments.get(0).getPositionX();
                int head_positionY = lstSnakeSegments.get(0).getPositionY();

                //check neu snake an 1 Point
                if(head_positionX == positionX && head_positionY == positionY){
                    growSnake();
                    addFood();
                }

                //Điều khiển snake (cái đầu của snake)
                switch (currentDirection){
                    case "up":
                        lstSnakeSegments.get(0).setPositionX(head_positionX);
                        lstSnakeSegments.get(0).setPositionY(head_positionY - (segmentSize * 2));
                        break;
                    case "down":
                        lstSnakeSegments.get(0).setPositionX(head_positionX);
                        lstSnakeSegments.get(0).setPositionY(head_positionY + (segmentSize * 2));
                        break;
                    case "right":
                        lstSnakeSegments.get(0).setPositionX(head_positionX + (segmentSize * 2));
                        lstSnakeSegments.get(0).setPositionY(head_positionY);
                        break;
                    case "left":
                        lstSnakeSegments.get(0).setPositionX(head_positionX - (segmentSize * 2));
                        lstSnakeSegments.get(0).setPositionY(head_positionY);
                        break;
                }

                //hàm làm cho phần head xhien ở phía đối diện nếu đâm tường
                if (lstSnakeSegments.get(0).getPositionX() < 0) {
                    lstSnakeSegments.get(0).setPositionX(surfaceView.getWidth() - (segmentSize * 2) - 10);  // Xuất hiện ở bên phải
                } else if (lstSnakeSegments.get(0).getPositionX() >= surfaceView.getWidth()) {
                    lstSnakeSegments.get(0).setPositionX(segmentSize);                                      // Xuất hiện ở bên trái
                } else if (lstSnakeSegments.get(0).getPositionY() < 0) {
                    lstSnakeSegments.get(0).setPositionY(surfaceView.getHeight() - (segmentSize * 2) - 4);  // Xuất hiện ở phía dưới
                } else if (lstSnakeSegments.get(0).getPositionY() >= surfaceView.getHeight()) {
                    lstSnakeSegments.get(0).setPositionY(segmentSize);                                      // Xuất hiện ở phía trên
                }

                //check gameOver
                if(checkGameOver(head_positionX, head_positionY) == true){
                    timer.cancel();
                    timer.purge();

                    showGameOverDialog();
                } else{
                    //lock canvas on surfaceHolder to draw on it
                    canvas = surfaceHolder.lockCanvas();

                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);       //làm sạch canvas
                    canvas.drawColor(Color.parseColor("#3D9558"));    //Tô màu lên canvas

                    //Chỉ cần vẽ vị trị cục đầu snake thôi, mấy cục sau sẽ tự theo cục đầu
                    canvas.drawCircle(lstSnakeSegments.get(0).getPositionX(), lstSnakeSegments.get(0).getPositionY(), segmentSize, createPointColor());

                    //Vẽ random cục mồi
                    canvas.drawCircle(positionX, positionY, segmentSize, createPointColor());

                    //Vòng for để những cục phần thân đi theo cục đầu
                    for(int i = 1; i < lstSnakeSegments.size(); i++){    //i = 1 vì cục đầu = 0
                        int tempPositionX = lstSnakeSegments.get(i).getPositionX();
                        int tempPositionY = lstSnakeSegments.get(i).getPositionY();

                        //di chuyển theo head
                        lstSnakeSegments.get(i).setPositionX(head_positionX);
                        lstSnakeSegments.get(i).setPositionY(head_positionY);
                        canvas.drawCircle(lstSnakeSegments.get(i).getPositionX(), lstSnakeSegments.get(i).getPositionY(), segmentSize, createPointColor());

                        //change head position
                        head_positionX = tempPositionX;
                        head_positionY = tempPositionY;
                    }
                    //mở Canvas để cập nhật giao diện và hiển thị những gì đã vẽ
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }, 1000 - snakeSpeed, 1000 - snakeSpeed);
    }

    // Vẽ màu cho snake và mồi
    private Paint createPointColor(){
        //check if color not defined before
        if(segmentColor == null){
            segmentColor = new Paint();
            segmentColor.setColor(snakeColor);
            segmentColor.setStyle(Paint.Style.FILL);
            segmentColor.setAntiAlias(true); //Kích hoạt khử răng cưa, giúp các hình tròn của rắn trông mượt hơn.

            return segmentColor;
        }
        return segmentColor;
    }

    private void growSnake() {
        SnakeSegments snakeSegments = new SnakeSegments(0,0);
        lstSnakeSegments.add(snakeSegments);
        score++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Code thay đổi giao diện
                txtScore.setText(String.valueOf(score));
            }
        });
    }

    private boolean checkGameOver(int head_PositionX, int head_PositionY){
        boolean gameOver = false;

        for (int i = 1; i < lstSnakeSegments.size(); i++){
            if(head_PositionX == lstSnakeSegments.get(i).getPositionX()
                    && head_PositionY == lstSnakeSegments.get(i).getPositionY()){
                gameOver = true;
                break;
            }
        }
        return gameOver;
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Điểm của bạn là: " + score);
        builder.setTitle("Game Over!");
        builder.setCancelable(false);
        builder.setPositiveButton("Chơi lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                init();
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }
}