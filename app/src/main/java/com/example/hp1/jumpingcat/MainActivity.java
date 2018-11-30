package com.example.hp1.jumpingcat;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    MediaPlayer ostmain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ostmain=MediaPlayer.create(this,R.raw.sistercomplex);
        ostmain.start();
    }

    public void startGame(View view){
        Intent intent = new Intent(this,StartGame.class);
        startActivity(intent);
        finish();
        ostmain.stop();

    }

    @Override
    protected void onPause(){
        super.onPause();
        ostmain.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        ostmain.start();
    }

    @Override
    public void onBackPressed() {
        // el vacio;
    }

}
