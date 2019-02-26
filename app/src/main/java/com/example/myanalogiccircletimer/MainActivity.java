package com.example.myanalogiccircletimer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CustomCircularProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progressBar);

        findViewById(R.id.doProgress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setProgressTo(30000, new CustomCircularProgressBar.Callback() {
                    @Override
                    public void countFinished() {
                        Toast.makeText(getApplicationContext(), "tempo acabou", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
