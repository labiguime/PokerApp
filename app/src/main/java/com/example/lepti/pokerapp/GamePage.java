package com.example.lepti.pokerapp;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class GamePage extends AppCompatActivity {

    String userNicknameExtra;
    String userAvatarExtra;
    ImageView userAvatar;
    Button userNicknameText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        userAvatar = findViewById(R.id.userAvatar);
        userNicknameText = findViewById(R.id.userNicknameText);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userNicknameExtra = extras.getString("nickname");
            userAvatarExtra = extras.getString("avatar");
        }
        int resID = getResources().getIdentifier(userAvatarExtra, "drawable", "com.example.lepti.pokerapp");
        userAvatar.setImageResource(resID);
        userNicknameText.setText(userNicknameExtra);
    }
}
