package com.example.lepti.pokerapp;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class HomePage extends AppCompatActivity {
    ImageView app_logo;
    Animation fromtop;
    Integer avatarPictureId = 0;
    CardView join_button;
    ImageView avatarPicture;
    EditText nicknameTextBox;
    Button changePictureButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        app_logo = (ImageView) findViewById(R.id.hp_app_logo);
        nicknameTextBox = findViewById(R.id.nicknameTextBox);
        changePictureButton = findViewById(R.id.changePictureButton);
        avatarPicture = (ImageView) findViewById(R.id.avatarPicture);
        fromtop = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        app_logo.setAnimation(fromtop);
        join_button = findViewById(R.id.joinGameButton);
        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, GamePage.class);
                String nickname = nicknameTextBox.getText().toString();
                if(nickname.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"You must choose a nickname!",Toast.LENGTH_SHORT).show();
                }
                else if(nickname.length() > 15) {
                    Toast.makeText(getApplicationContext(),"Your nickname is too long!",Toast.LENGTH_SHORT).show();
                }
                else {
                    avatarPictureId = avatarPictureId+1;
                    String avatarFileName = "avatar" + Integer.toString(avatarPictureId);
                    myIntent.putExtra("avatar", avatarFileName);
                    myIntent.putExtra("nickname", nickname);
                    startActivity(myIntent);
                }

            }
        });

        changePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarPictureId = ((avatarPictureId+1) % 6) + 1;
                String avatarFileName = "avatar" + Integer.toString(avatarPictureId);
                avatarPictureId--;
                int resID = getResources().getIdentifier(avatarFileName, "drawable", "com.example.lepti.pokerapp");
                avatarPicture.setImageResource(resID);
            }
        });
    }
}
