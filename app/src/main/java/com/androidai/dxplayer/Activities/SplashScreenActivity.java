package com.androidai.dxplayer.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.androidai.dxplayer.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_splash_screen);
        try {
            getSupportActionBar().hide();
        }catch (Exception e){

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(getIntent().getDataString()!=null){
                    SharedPreferences sp = getSharedPreferences("BASE_APP",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    Log.d("playback_url",getIntent().getDataString());
                    editor.putString("playback_url",getIntent().getDataString().substring(11));
                    editor.apply();
                    startActivity(new Intent(SplashScreenActivity.this, VideoPlayerActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    finish();
                }
            }
        },3000);
    }
    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}