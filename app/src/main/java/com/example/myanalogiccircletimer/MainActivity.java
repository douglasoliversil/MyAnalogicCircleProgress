package com.example.myanalogiccircletimer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private CustomCircularProgressBar mProgressBar;
    private Integer countProgress = 0;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private Runnable mUiTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progressBar);

       /* mTimer = new Timer();
        mUiTask = new Runnable() {
            @Override
            public void run() {
                mProgressBar.setProgress(countProgress += 1, new TimeCallBack() {
                    @Override
                    public void timeOut() {
                        stopTask();
                    }
                });
            }
        };
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(mUiTask);
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 1000);*/

//       mProgressBar.startAnimation(60000);
        findViewById(R.id.doProgress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setProgressTo(3000, new CustomCircularProgressBar.Callback() {
                    @Override
                    public void countFinished() {
                        Toast.makeText(getApplicationContext(),"tempo acabou",Toast.LENGTH_SHORT).show();
                    }
                });
//                mProgressBar.getCounterText().setText(countProgress.toString() + "%");
            }
        });
    }

    /*private void stopTask() {
        mTimerTask.cancel();
        mTimer.cancel();
    }*/

}
