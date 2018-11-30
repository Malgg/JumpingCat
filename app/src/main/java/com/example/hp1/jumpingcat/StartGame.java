package com.example.hp1.jumpingcat;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class StartGame extends Activity {

    GameView gameView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);

    }

    @Override
    protected void onPause(){
        super.onPause();
        GameView.ost.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        GameView.ost.start();
    }

    @Override
    public void onBackPressed() {
        // el vacio;
    }
}
