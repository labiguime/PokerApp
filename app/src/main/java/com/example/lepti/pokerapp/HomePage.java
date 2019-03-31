package com.example.lepti.pokerapp;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class HomePage extends AppCompatActivity {
    ImageView app_logo;
    Animation fromtop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        app_logo = (ImageView) findViewById(R.id.hp_app_logo);
        fromtop = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        app_logo.setAnimation(fromtop);
    }
}
