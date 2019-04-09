package com.example.lepti.pokerapp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import classes.GameVariables;
import classes.PlayerVariables;
import classes.TableCards;

public class HomePage extends AppCompatActivity {
    ImageView app_logo;
    Animation fromtop;
    Integer avatarPictureId = 0;
    ImageView join_button;
    ImageView avatarPicture;
    EditText nicknameTextBox;
    ImageView changePictureButton;
    Map<String, Boolean> freeSpots = new HashMap<>();
    GameVariables gVars = new GameVariables();
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       // View decorView = getWindow().getDecorView();
// Hide the status bar.
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
       // decorView.setSystemUiVisibility(uiOptions);

        //
        mp = MediaPlayer.create(HomePage.this, R.raw.background_music);
        mp.setLooping(true);
        mp.start();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            getWindow().getDecorView().setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        } else {
                        }
                    }
                });

        app_logo = (ImageView) findViewById(R.id.hp_app_logo);
        nicknameTextBox = findViewById(R.id.nicknameTextBox);
        changePictureButton = findViewById(R.id.changePictureButton);
        avatarPicture = (ImageView) findViewById(R.id.avatarPicture);
        fromtop = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        /* Keep global variables up-to-date */
        /*DatabaseReference cardsRef = database.getReference("game-1/table-cards");
        cardsRef.setValue(new TableCards());

        cardsRef = database.getReference("game-1/free-spots");
        freeSpots.put("0", true);
        freeSpots.put("1", true);
        freeSpots.put("2", true);
        freeSpots.put("3", true);
        cardsRef.setValue(freeSpots);
        freeSpots.clear();

        cardsRef = database.getReference("game-1/variables");
        cardsRef.setValue(new GameVariables());*/
        app_logo.setAnimation(fromtop);
        join_button = findViewById(R.id.joinGameButton);
        join_button.setClickable(true);
        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nicknameTextBox.getText().toString();
                join_button.setClickable(false);
                if(nickname.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"You must choose a nickname!",Toast.LENGTH_SHORT).show();
                    join_button.setClickable(true);
                }
                else if(nickname.length() > 15) {
                    Toast.makeText(getApplicationContext(),"Your nickname is too long (more than 15 char.)!",Toast.LENGTH_SHORT).show();
                    join_button.setClickable(true);
                }
                else {
                    DatabaseReference myRef = database.getReference("game-1/free-spots");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            freeSpots.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                freeSpots.put(snapshot.getKey(), snapshot.getValue(Boolean.class));
                            }
                            joinGame(checkFreeSpots());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            joinGame(-1);
                        }
                    });
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.stop();
        mp.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mp.stop();
        //mp.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        join_button.setClickable(true);
        //if(mp == null) mp = MediaPlayer.create(HomePage.this, R.raw.background_music);
        //mp.setLooping(true);
        //mp.start();
        //mp.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mp.stop();
        //mp.release();
    }

    private void joinGame(final int gameSpot) {
        if(gameSpot == -1) {
            Toast.makeText(HomePage.this, "The room is full (4/4)...", Toast.LENGTH_SHORT).show();
            join_button.setClickable(true);
            return;
        }
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameVariables = database.getReference("game-1/variables");
        gameVariables.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gVars = dataSnapshot.getValue(GameVariables.class);

                if(gVars.getNumberPlayers() == gVars.getReadyPlayers() && gVars.getNumberPlayers() > 1) {
                    Toast.makeText(HomePage.this, "The game has already started, you cannot join the room!", Toast.LENGTH_SHORT).show();
                    join_button.setClickable(true);
                    return;
                }

                Intent myIntent = new Intent(HomePage.this, GamePage.class);
                String nickname = nicknameTextBox.getText().toString();
                String avatarFileName = "avatar" + Integer.toString(avatarPictureId+1);
                PlayerVariables user;

                DatabaseReference playerVariables = database.getReference("game-1/player-variables/"+Integer.toString(gameSpot));
                DatabaseReference reference = database.getReference("game-1/free-spots");

                gVars.setNumberPlayers(gVars.getNumberPlayers()+1);
                DatabaseReference gameVariables = database.getReference("game-1/variables/numberPlayers");
                gameVariables.setValue(gVars.getNumberPlayers());

                user = new PlayerVariables(nickname, avatarFileName);
                playerVariables.setValue(user);
                reference.setValue(freeSpots);

                myIntent.putExtra("avatar", avatarFileName);
                myIntent.putExtra("nickname", nickname);
                myIntent.putExtra("playerSpot", gameSpot);
                startActivity(myIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomePage.this, "Try again...", Toast.LENGTH_SHORT).show();
                join_button.setClickable(true);

                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    private int checkFreeSpots() {
        for (Map.Entry<String, Boolean> entry : freeSpots.entrySet()) {
            if(entry.getValue()) {
                entry.setValue(false);
                return Integer.parseInt(entry.getKey());
            }
        }
        return -1;
    }
}
