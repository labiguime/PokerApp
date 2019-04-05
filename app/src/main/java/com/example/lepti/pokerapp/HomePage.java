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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity {
    ImageView app_logo;
    Animation fromtop;
    Integer avatarPictureId = 0;
    CardView join_button;
    ImageView avatarPicture;
    EditText nicknameTextBox;
    Button changePictureButton;
    int playerSpot = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        app_logo = (ImageView) findViewById(R.id.hp_app_logo);
        nicknameTextBox = findViewById(R.id.nicknameTextBox);
        changePictureButton = findViewById(R.id.changePictureButton);
        avatarPicture = (ImageView) findViewById(R.id.avatarPicture);
        fromtop = AnimationUtils.loadAnimation(this, R.anim.fromtop);

      /*
        // INITIALIZE GAME
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("game-1/table-spots/spot-0");
        myRef.setValue("0");
        myRef = database.getReference("game-1/table-spots/spot-1");
        myRef.setValue("0");
        myRef = database.getReference("game-1/table-spots/spot-2");
        myRef.setValue("0");
        myRef = database.getReference("game-1/table-spots/spot-3");
        myRef.setValue("0");

        myRef = database.getReference("game-1/variables/playerTurn");
        myRef.setValue("0");

        myRef = database.getReference("game-1/variables/totalBet");
        myRef.setValue("0");

        myRef = database.getReference("game-1/variables/roundBet");
        myRef.setValue("0");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("TRUC:", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TRUC:", "Failed to read value.", error.toException());
            }
        });*/


        app_logo.setAnimation(fromtop);
        join_button = findViewById(R.id.joinGameButton);
        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



// Attach a listener to read the data at our posts reference
               /*
                               FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("server/saving-data/fireblog/posts");
               ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post post = dataSnapshot.getValue(Post.class);
                        System.out.println(post);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });*/

               // testFunction();


                Intent myIntent = new Intent(HomePage.this, GamePage.class);
                String nickname = nicknameTextBox.getText().toString();

                if(nickname.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"You must choose a nickname!",Toast.LENGTH_SHORT).show();
                }
                else if(nickname.length() > 15) {
                    Toast.makeText(getApplicationContext(),"Your nickname is too long!",Toast.LENGTH_SHORT).show();
                }
                else {
                    lookForSpot(0,0, true);
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
    private void testFunction()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Integer[] spotAvailable = new Integer[1];
        String path = "game-1/table-spots/spot-" + Integer.toString(0);
        Log.d("SPOT", "CHECKING PATH" + path);
        DatabaseReference ref = database.getReference(path);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("SPOT", "Inside");
                spotAvailable[0] = dataSnapshot.getValue(Integer.class);
                Log.d("SPOT", "Reached here");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("VAR", "CANCELLED");
            }
        });
    }
    private void joinGame() {
        Intent myIntent = new Intent(HomePage.this, GamePage.class);
        String nickname = nicknameTextBox.getText().toString();
        avatarPictureId = avatarPictureId+1;
        String avatarFileName = "avatar" + Integer.toString(avatarPictureId);
        myIntent.putExtra("avatar", avatarFileName);
        myIntent.putExtra("nickname", nickname);
        myIntent.putExtra("playerSpot", playerSpot);
        startActivity(myIntent);
    }



    private void lookForSpot(final int spotId, Integer spotChecked, boolean checkNext) {
        if(checkNext) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final Integer[] spotAvailable = new Integer[1];
            String path = "game-1/table-spots/spot-" + Integer.toString(spotId);
            DatabaseReference ref = database.getReference(path);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    spotAvailable[0] = dataSnapshot.getValue(Integer.class);
                    lookForSpot(spotId, spotAvailable[0], false);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    lookForSpot(spotId, 0, false);
                }
            });
        }
        else {
            if(spotChecked == 1) {
                playerSpot = spotId;
                joinGame();
            }
            else {
                if(spotId == 3) {
                    playerSpot = -2; // no spot found
                }
                else {
                    lookForSpot(spotId+1, spotChecked, true);
                }
            }
        }
    }
}
