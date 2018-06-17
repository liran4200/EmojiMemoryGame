package com.example.liranyehudar.emojimemorygame;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class GameActivity extends AppCompatActivity {
    final static int INTERVAL = 1000;
    private int numOfImages;
    // refernceses to front images for this game
    private Integer[] frontImagesReferences;
    // referenceses to all the back images
    private Integer[] backImagesReferences = {R.drawable.image1,R.drawable.image2,
                                              R.drawable.image3,R.drawable.image4,
                                              R.drawable.image5,R.drawable.image6,
                                              R.drawable.image7,R.drawable.image8,
                                              R.drawable.image9,R.drawable.image10,
                                              R.drawable.image11,R.drawable.image12,
                                              R.drawable.image13,R.drawable.image14,
                                              R.drawable.image15,R.drawable.image16,
                                              R.drawable.image17,R.drawable.image18};
    // refernceses to back images for this game up to the num of images
    private Integer[] currentBackImagesReferences;

    private GameBoard board;
    private int frontImageId = R.drawable.question;
    private boolean isBusy = false;

    private String name;
    private int rowNum,colNum;
    private int points;
    private int results;

    private ImageView imageViewSelected1;
    private ImageView imageViewSelected2;
    private TextView textTimer;
    private TextView textName;
    private ConstraintLayout constLayout;
    private GridView myGrid;
    private CountDownTimer mCountDownTimer;
    private ProgressBar progBar;
    private int i = 0;
    private int currentSecond;
    private int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getParametersByIntent();
        frontImagesReferences = new Integer[numOfImages];
        currentBackImagesReferences = new Integer[numOfImages/2]; // num Of Images/2 - every image appear 2 times
        board = new GameBoard(numOfImages);

        initCurrentBackImages();
        initFrontImages();
        addImagesToGameBoard();
        board.shuffle();

        bindUI();
        currentSecond = time/INTERVAL;
        int height = constLayout.getLayoutParams().height;
        int width = constLayout.getLayoutParams().width;

        textName.setText(name);
        myGrid.setNumColumns(rowNum);
        myGrid.setAdapter(new ImageAdapter(this,height,width,rowNum,colNum,frontImagesReferences));

        myGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ImageView img = (ImageView)v;
                checkBackImagesMatching(img,position);
            }
        });

        textTimer.setText(""+currentSecond);
        progBar.setProgress(i);
        mCountDownTimer = new CountDownTimer(time,INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentSecond--;
                i++;
                int timer = i*100/(time/INTERVAL);
                progBar.setProgress(timer);
                textTimer.setText(""+currentSecond);

            }

            @Override
            public void onFinish() {
                i++;
                progBar.setProgress(100);
                textTimer.setText(""+0);
                showDialogMessage("Time over, please try again","Timer");
            }
        };

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCountDownTimer.start();
            }
        },900);
    }

    public void getParametersByIntent(){
        Intent intent = getIntent();
        rowNum = intent.getIntExtra("row",-1);
        colNum = intent.getIntExtra("col", -1);
        name = intent.getStringExtra("name");
        time = intent.getIntExtra("time",-1);
        points = intent.getIntExtra("point",-1);
        numOfImages = rowNum*colNum;
    }

    public void bindUI(){
        constLayout = findViewById(R.id.constraint_board);
        textTimer   = findViewById(R.id.txt_timer);
        textName   = findViewById(R.id.txt_name);
        progBar     = findViewById(R.id.progressBar);
        myGrid      = findViewById(R.id.gridview);
    }

    /**
     * This function check if 2 selected images on the board are match.
     * Checks if the player win.
     * @param img
     * @param position
     */
    public void checkBackImagesMatching(ImageView img,int position){
            if(isBusy)
                return;

            if(!board.isImageSelected(0)) {
                board.setImageSelectedFirstByIndex(position);
                if(!board.flipFirst()) {
                    imageViewSelected1 = img;
                    imageViewSelected1.setImageResource(board.getDrawableId(0));
                }
                else return;
            }
            if(!board.isImageSelected(1)) {
                board.setImageSelectedSecondByIndex(position);
                if (!board.flipSecond()) {
                    results -= points/4; // not matching
                    imageViewSelected2 = img;
                    imageViewSelected2.setImageResource(board.getDrawableId(1));
                }
                else return;



                if (board.isSelectedImagesMatching()) {
                    results += points; // matching
                    if (checkWinGame()) {
                        mCountDownTimer.cancel(); // cancel timer
                        Intent resultIntent = new Intent();
                        if(results<0)// only postive points should be in the table
                            results=0;
                        resultIntent.putExtra("result", results+"");
                        setResult(RESULT_OK, resultIntent);
                        showDialogMessage("You win!, your score is "+results,"Congratulations");
                    }
                } else {
                    // do it in the background ;
                    isBusy = true;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            board.resetSelectedImages();
                            imageViewSelected1.setImageResource(frontImageId);
                            imageViewSelected2.setImageResource(frontImageId);
                            isBusy = false;
                        }
                    },800);
                }
            }
    }

    public boolean checkWinGame(){
        return board.isAllImagesMatch();
    }

    public void addImagesToGameBoard(){
        int countCurrentBackImages = 0;
        for(int i = 0; i < numOfImages; i ++){
            if(countCurrentBackImages == currentBackImagesReferences.length)
                countCurrentBackImages = 0;
            board.addImage(currentBackImagesReferences[countCurrentBackImages]);
            countCurrentBackImages++;
        }
    }
    
    public void initCurrentBackImages(){
        for(int i = 0; i < currentBackImagesReferences.length; i ++){
            currentBackImagesReferences[i] = backImagesReferences[i];
        }
    }

    // This function initialize refernces to front image by num of images.
    public void initFrontImages(){
        for(int i = 0 ; i < numOfImages; i ++)
            frontImagesReferences[i] = frontImageId;
    }

    public void onBackPressed() {
        mCountDownTimer.cancel();
       super.onBackPressed();
    }

    public void showDialogMessage(String message,String title){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(GameActivity.this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
}
