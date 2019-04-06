package com.example.lepti.pokerapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Image;
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
    CardView join_button;
    ImageView avatarPicture;
    EditText nicknameTextBox;
    Button changePictureButton;
    Map<String, Boolean> freeSpots = new HashMap<>();
    GameVariables gVars;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        app_logo = (ImageView) findViewById(R.id.hp_app_logo);
        nicknameTextBox = findViewById(R.id.nicknameTextBox);
        changePictureButton = findViewById(R.id.changePictureButton);
        avatarPicture = (ImageView) findViewById(R.id.avatarPicture);
        fromtop = AnimationUtils.loadAnimation(this, R.anim.fromtop);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        /* Keep global variables up-to-date */
        DatabaseReference gameVariables = database.getReference("game-1/variables");
        gameVariables.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gVars = dataSnapshot.getValue(GameVariables.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });
        DatabaseReference cardsRef = database.getReference("game-1/table-cards");
        cardsRef.setValue(new TableCards());

        cardsRef = database.getReference("game-1/variables");
        cardsRef.setValue(new GameVariables());
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
    private void joinGame(int gameSpot) {
        if(gameSpot == -1) {
            // Cannot join the game
            return;
        }
        Intent myIntent = new Intent(HomePage.this, GamePage.class);
        String nickname = nicknameTextBox.getText().toString();
        String avatarFileName = "avatar" + Integer.toString(avatarPictureId+1);
        PlayerVariables user;


        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
