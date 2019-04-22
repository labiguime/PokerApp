package com.example.lepti.pokerapp;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import classes.GameVariables;
import classes.PlayerVariables;
import classes.TableCards;

public class GamePage extends AppCompatActivity {

    int userSpot;
    ConstraintLayout userAvatar;
    TextView userNicknameText;
    TextView userMoneyText;
    ImageView readyButton;

    ConstraintLayout[] pAvatar = new ConstraintLayout[3];
    TextView[] pNicknameText = new TextView[3];
    TextView[] pMoneyText = new TextView[3];

    ImageView foldButton;
    ImageView userCard1View;
    ImageView userCard2View;
    ImageView tableCard1View;
    ImageView tableCard2View;
    ImageView tableCard3View;
    ImageView tableCard4View;
    ImageView tableCard5View;
    ImageView p2Card1;
    ImageView p2Card2;
    TextView readyTextId;
    ImageView p3Card1;
    ImageView p3Card2;

    ImageView p4Card1;
    ImageView p4Card2;

    TextView totalBetText;
    TextView currentBetText;
    LinearLayout raiseLayout;
    LinearLayout tableCardsLayout;
    LinearLayout totalBetBox;
    LinearLayout currentBetBox;
    LinearLayout readyBox;
    TextView raiseText;
    LinearLayout[] playerAvatarLayout = new LinearLayout[3];
    ImageView checkButton;
    ImageView raiseButton;
    ImageView increaseBetButton;
    ImageView decreaseBetButton;
    MediaPlayer mp;
    int localRaiseAmount = 0;
    int[] userDeckOfCards = new int[7];
    int[] userBestHand = new int[5];
    int[] pId = new int[3];

    List<Integer> cardsUsed  = new ArrayList<>();
    List<Integer> winningPlayers = new ArrayList<>();
    PlayerVariables user;
    GameVariables gVars;
    TableCards tCards = new TableCards();
    List<Integer> nextPlayerInLine = new ArrayList<>();

    int lastRoundRecorded = -1;
    int lastPlayerTurnRecorded = -1;
    Map<String, Boolean> freeSpots = new HashMap<>();
    ValueEventListener gameVariablesListener;
    DatabaseReference gameVariablesDR;

    DatabaseReference playerVariablesDR;
    ValueEventListener playerVariablesListener;
    DatabaseReference tableCardVariablesDR;
    ValueEventListener tableCardVariablesListener;
    DatabaseReference gameSpotsDR;
    ValueEventListener gameSpotsListener;
    DatabaseReference cardsRefDR;
    ValueEventListener cardsRefListener;
    DatabaseReference winnersRefDR;
    DatabaseReference pQueueRefDR;
    ValueEventListener pQueueRefListener;
    ValueEventListener winnersRefListener;

    boolean alreadyCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if(alreadyCreated) return;
        Intent svc=new Intent(this, BackgroundService.class);
        startService(svc);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
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

        userCard2View = findViewById(R.id.userCard2);
        tableCard1View = findViewById(R.id.tableCard1);
        tableCard2View = findViewById(R.id.tableCard2);
        tableCard3View = findViewById(R.id.tableCard3);
        tableCard4View = findViewById(R.id.tableCard4);
        tableCard5View = findViewById(R.id.tableCard5);
        readyBox = findViewById(R.id.readyBox);
        totalBetText = findViewById(R.id.totalBetText);
        currentBetText = findViewById(R.id.currentBetText);
        currentBetBox = findViewById(R.id.currentBetBox);
        totalBetBox = findViewById(R.id.totalBetBox);
        pNicknameText[0] = findViewById(R.id.p2NicknameText);
        pNicknameText[1] = findViewById(R.id.p3NicknameText);
        pNicknameText[2] = findViewById(R.id.p4NicknameText);
        pMoneyText[0] = findViewById(R.id.p2MoneyText);
        pMoneyText[1] = findViewById(R.id.p3MoneyText);
        pMoneyText[2] = findViewById(R.id.p4MoneyText);
        readyTextId = findViewById(R.id.readyTextId);
        raiseText = findViewById(R.id.raiseEditText);
        tableCardsLayout = findViewById(R.id.tableCardsLayout);
        raiseLayout = findViewById(R.id.raiseLayout);
        decreaseBetButton = findViewById(R.id.decreaseRaise);
        increaseBetButton = findViewById(R.id.increaseBet);

        userAvatar = findViewById(R.id.userAvatar);
        userNicknameText = findViewById(R.id.userNicknameText);
        userMoneyText = findViewById(R.id.userMoneyText);
        userCard1View = findViewById(R.id.userCard1);

        p2Card1 = findViewById(R.id.p2Card1);
        p2Card2 = findViewById(R.id.p2Card2);

        p3Card1 = findViewById(R.id.p3Card1);
        p3Card2 = findViewById(R.id.p3Card2);

        p4Card1 = findViewById(R.id.p4Card1);
        p4Card2 = findViewById(R.id.p4Card2);
        playerAvatarLayout[0] = findViewById(R.id.layout_player_1);
        playerAvatarLayout[1] = findViewById(R.id.layout_player_2);
        playerAvatarLayout[2] = findViewById(R.id.layout_player_3);

        checkButton = findViewById(R.id.checkButton);
        foldButton = findViewById(R.id.foldButton);
        readyButton = findViewById(R.id.readyButton);
        raiseButton = findViewById(R.id.raiseButton);

        /* Assigning a variable to each view */
        pAvatar[0] = findViewById(R.id.p2Avatar);
        pAvatar[1] = findViewById(R.id.p3Avatar);
        pAvatar[2] = findViewById(R.id.p4Avatar);
        /* Recovering the extras */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = new PlayerVariables(extras.getString("nickname"), extras.getString("avatar"));
            userSpot = extras.getInt("playerSpot");
        }

        /* Setting up the UI */
        int resID = getResources().getIdentifier(user.getAvatar()+ "_notfolded", "drawable", "com.example.lepti.pokerapp");
        userAvatar.setBackgroundResource(resID);
        userNicknameText.setText(user.getNickname());
        userMoneyText.setText("$"+Integer.toString(user.getMoney()));
        foldButton.setVisibility(View.INVISIBLE);
        checkButton.setVisibility(View.INVISIBLE);
        readyBox.setVisibility(View.VISIBLE);
        readyButton.setVisibility(View.VISIBLE);
        raiseButton.setVisibility(View.INVISIBLE);
        currentBetBox.setVisibility(View.INVISIBLE);
        totalBetBox.setVisibility(View.INVISIBLE);

        tableCardsLayout.setVisibility(View.INVISIBLE);
        foldButton.setClickable(false);
        checkButton.setClickable(false);
        raiseButton.setClickable(false);
        increaseBetButton.setClickable(false);
        decreaseBetButton.setClickable(false);
        increaseBetButton.setVisibility(View.INVISIBLE);
        decreaseBetButton.setVisibility(View.INVISIBLE);
        raiseText.setVisibility(View.INVISIBLE);
        raiseLayout.setVisibility(View.INVISIBLE);
        playerAvatarLayout[0].setVisibility(View.INVISIBLE);
        playerAvatarLayout[1].setVisibility(View.INVISIBLE);
        playerAvatarLayout[2].setVisibility(View.INVISIBLE);

        tableCardsLayout.setVisibility(View.INVISIBLE);

        p2Card1.setVisibility(View.INVISIBLE);
        p2Card2.setVisibility(View.INVISIBLE);

        p3Card1.setVisibility(View.INVISIBLE);
        p3Card2.setVisibility(View.INVISIBLE);

        p4Card1.setVisibility(View.INVISIBLE);
        p4Card2.setVisibility(View.INVISIBLE);

        userCard1View.setVisibility(View.INVISIBLE);
        userCard2View.setVisibility(View.INVISIBLE);
        hideAllButtons();

        increaseBetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gVars.getRoundBet() == 0) {
                    if(localRaiseAmount == 0) {
                        localRaiseAmount = 200;
                        raiseText.setText(Integer.toString(localRaiseAmount));
                    }
                    else {
                        localRaiseAmount = localRaiseAmount+200;
                        raiseText.setText(Integer.toString(localRaiseAmount));
                    }
                }
                else {
                    if(localRaiseAmount < gVars.getRoundBet()) {
                        localRaiseAmount = gVars.getRoundBet();
                        raiseText.setText(Integer.toString(localRaiseAmount));
                        // update text
                    }
                    else {
                        localRaiseAmount = localRaiseAmount+200;
                        raiseText.setText(Integer.toString(localRaiseAmount));
                        // update text
                    }
                }
            }
        });

        decreaseBetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(localRaiseAmount > 0) {
                    localRaiseAmount = localRaiseAmount-200;
                    raiseText.setText(Integer.toString(localRaiseAmount));
                    // update text
                }
            }
        });



        /* Keep global variables up-to-date */
        gameVariablesDR = database.getReference("game-1/variables");
        gameVariablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gVars = dataSnapshot.getValue(GameVariables.class);
                totalBetText.setText("$"+Integer.toString(gVars.getTotalBet()));
                currentBetText.setText("$"+Integer.toString(gVars.getRoundBet()));
                if(gVars.getRoundBet() > user.getCurrentBet()) {
                    currentBetText.setTextColor(getResources().getColor(R.color.colorAccent));
                }
                else {
                    currentBetText.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_default));
                }
                if(lastRoundRecorded == gVars.getCurrentRound() && lastPlayerTurnRecorded == gVars.getPlayerTurn()) return;

                if(gVars.getPlayerTurn() != userSpot+1 && gVars.getCurrentRound() != -1) {
                    lastRoundRecorded = gVars.getCurrentRound();
                    lastPlayerTurnRecorded = gVars.getPlayerTurn();
                    int resID = getResources().getIdentifier(user.getAvatar()+ "_notfolded", "drawable", "com.example.lepti.pokerapp");
                    if(user.getHasPlayerFolded()) resID = getResources().getIdentifier(user.getAvatar()+ "_folded", "drawable", "com.example.lepti.pokerapp");
                    userAvatar.setBackgroundResource(resID);

                    DatabaseReference gameSpots = database.getReference("game-1/free-spots");
                    gameSpots.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if(Integer.parseInt(snapshot.getKey()) != userSpot) {
                                    if(snapshot.getValue(Boolean.class) == true) {
                                        hidePlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                    }
                                    else {
                                        showPlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    if(gVars.getCurrentRound() == 0 && gVars.getPlayerTurn() > 0 && user.getLastRoundPlayed() != 0 ) {
                        int resId = getResources().getIdentifier("backside_old", "drawable", "com.example.lepti.pokerapp");
                        userCard1View.setImageResource(resId);
                        userCard2View.setImageResource(resId);
                        userCard1View.setVisibility(View.VISIBLE);
                        userCard2View.setVisibility(View.VISIBLE);
                    }
                }
                if(gVars.getCurrentRound() == -1) {
                    lastRoundRecorded = gVars.getCurrentRound();
                    lastPlayerTurnRecorded = gVars.getPlayerTurn();
                    /* Fading out the user's card */
                    fadeCardAway(userCard1View);
                    fadeCardAway(userCard2View);

                    /* Fading away the table cards */
                    fadeCardAway(tableCard1View);
                    fadeCardAway(tableCard2View);
                    fadeCardAway(tableCard3View);
                    fadeCardAway(tableCard4View);
                    fadeCardAway(tableCard5View);

                    /* Fading away the players cards */
                    if(playerAvatarLayout[0].getVisibility() == View.VISIBLE) {
                        fadeCardAway(p2Card1);
                        fadeCardAway(p2Card2);
                    }

                    if(playerAvatarLayout[1].getVisibility() == View.VISIBLE) {
                        fadeCardAway(p3Card1);
                        fadeCardAway(p3Card2);
                    }

                    if(playerAvatarLayout[2].getVisibility() == View.VISIBLE) {
                        fadeCardAway(p4Card1);
                        fadeCardAway(p4Card2);
                    }

                    /* Reset the players' variables */
                    user.setCard1(-1);
                    user.setCard2(-1);
                    user.setBestHandName("No Pair");
                    user.setCurrentBet(0);
                    user.setLastRoundPlayed(-1);
                    user.setHasPlayerFolded(false);
                    DatabaseReference db = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                    db.setValue(user);


                    /* Reset the bet texts */
                    localRaiseAmount = 0;
                    raiseText.setText("0");
                    totalBetText.setText("$0");
                    currentBetText.setText("$0");
                    currentBetText.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_default));
                    int resID = getResources().getIdentifier(user.getAvatar()+ "_notfolded", "drawable", "com.example.lepti.pokerapp");
                    userAvatar.setBackgroundResource(resID);

                    DatabaseReference gameSpots = database.getReference("game-1/free-spots");
                    gameSpots.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if(Integer.parseInt(snapshot.getKey()) != userSpot) {
                                    if(snapshot.getValue(Boolean.class) == true) {
                                        hidePlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                    }
                                    else {
                                        showPlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Toast.makeText(GamePage.this, "Shuffling the new deck...", Toast.LENGTH_SHORT).show();
                }
                else if(gVars.getPlayerTurn() == userSpot+1) {
                    lastRoundRecorded = gVars.getCurrentRound();
                    lastPlayerTurnRecorded = gVars.getPlayerTurn();
                    if((user.getLastRoundPlayed() != gVars.getCurrentRound())) {
                        user.setCurrentBet(0);
                    }

                    user.setLastRoundPlayed(gVars.getCurrentRound());
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference update = db.getReference("game-1/player-variables/" + Integer.toString(userSpot));
                    update.setValue(user);

                    DatabaseReference gameSpots = database.getReference("game-1/free-spots");
                    gameSpots.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if(Integer.parseInt(snapshot.getKey()) != userSpot) {
                                    if(snapshot.getValue(Boolean.class) == true) {
                                        hidePlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                    }
                                    else {
                                        showPlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    int resID = getResources().getIdentifier(user.getAvatar()+ "_yourturn", "drawable", "com.example.lepti.pokerapp");
                    userAvatar.setBackgroundResource(resID);


                    if(!gVars.getHasSomeonePlayed()) {
                        triggerRoundStartEvent(gVars.getCurrentRound());
                    }
                    else {
                        if(gVars.getCurrentRound() == 4) {
                            hideAllButtons();

                            String hand = getUserHand();
                            user.setBestHandName(hand);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference updateGlobal = database.getReference("game-1/player-variables/" + Integer.toString(userSpot));
                            updateGlobal.setValue(user);

                            updateHands();

                        }
                        else if(gVars.getCurrentRound() < 4) {
                            displayAllButtons();
                            if(gVars.getCurrentRound() == 0 && (user.getCard1() == -1 || user.getCard2() == -1)) {
                                generateCardsForPlayer();
                            }
                        }

                    }
                }
                else if(gVars.getHasSomeonePlayed() == false && gVars.getCurrentRound() == 0 && gVars.getPlayerTurn() > 0) {
                    lastRoundRecorded = gVars.getCurrentRound();
                    lastPlayerTurnRecorded = gVars.getPlayerTurn();
                    hideAllButtons();
                    if(playerAvatarLayout[0].getVisibility() == View.VISIBLE) {
                        Animation fade_in = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
                        p2Card1.setVisibility(View.VISIBLE);
                        p2Card1.startAnimation(fade_in);
                        p2Card2.setVisibility(View.VISIBLE);
                        p2Card2.startAnimation(fade_in);
                    }

                    if(playerAvatarLayout[1].getVisibility() == View.VISIBLE) {
                        Animation fade_in = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
                        p3Card1.setVisibility(View.VISIBLE);
                        p3Card1.startAnimation(fade_in);
                        p3Card2.setVisibility(View.VISIBLE);
                        p3Card2.startAnimation(fade_in);
                    }

                    if(playerAvatarLayout[2].getVisibility() == View.VISIBLE) {
                        Animation fade_in = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
                        p4Card1.setVisibility(View.VISIBLE);
                        p4Card1.startAnimation(fade_in);
                        p4Card2.setVisibility(View.VISIBLE);
                        p4Card2.startAnimation(fade_in);
                    }
                    readyTextId.setText("Click when ready");
                    readyBox.setVisibility(View.INVISIBLE);
                    //raiseText.setText("0");
                    totalBetText.setText("$0");
                    currentBetText.setText("$0");
                    currentBetText.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_default));
                    totalBetBox.setVisibility(View.VISIBLE);
                    currentBetBox.setVisibility(View.VISIBLE);
                }
                else if(gVars.getCurrentRound() == 5) {
                    lastRoundRecorded = gVars.getCurrentRound();
                    lastPlayerTurnRecorded = gVars.getPlayerTurn();
                    hideAllButtons();

                    if(playerAvatarLayout[0].getVisibility() == View.VISIBLE) {
                        DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(pId[0]));
                        pReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                                pMoneyText[0].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                                int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                                if(layoutPlayerVariables.getHasPlayerFolded()) return;
                                pAvatar[0].setBackgroundResource(resID);
                                revealCard(p2Card1, returnCardName(layoutPlayerVariables.getCard1()));
                                revealCard(p2Card2, returnCardName(layoutPlayerVariables.getCard2()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                            }
                        });
                    }

                    if(playerAvatarLayout[1].getVisibility() == View.VISIBLE) {
                        DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(pId[1]));
                        pReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                                pMoneyText[1].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                                int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                                if(layoutPlayerVariables.getHasPlayerFolded()) return;
                                pAvatar[1].setBackgroundResource(resID);
                                revealCard(p3Card1, returnCardName(layoutPlayerVariables.getCard1()));
                                revealCard(p3Card2, returnCardName(layoutPlayerVariables.getCard2()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                            }
                        });
                    }

                    if(playerAvatarLayout[2].getVisibility() == View.VISIBLE) {
                        DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(pId[2]));
                        pReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                                pMoneyText[2].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                                int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                                if(layoutPlayerVariables.getHasPlayerFolded()) return;
                                pAvatar[2].setBackgroundResource(resID);
                                revealCard(p4Card1, returnCardName(layoutPlayerVariables.getCard1()));
                                revealCard(p4Card2, returnCardName(layoutPlayerVariables.getCard2()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                            }
                        });
                    }
                }
                else {
                    lastRoundRecorded = gVars.getCurrentRound();
                    lastPlayerTurnRecorded = gVars.getPlayerTurn();
                    hideAllButtons();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        };
        gameVariablesDR.addValueEventListener(gameVariablesListener);

        playerVariablesDR = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
        playerVariablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(PlayerVariables.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playerVariablesDR.addValueEventListener(playerVariablesListener);

        /* Keep track of the cards on the table */

        tableCardVariablesDR = database.getReference("game-1/table-cards");

        tableCardVariablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tCards = dataSnapshot.getValue(TableCards.class);
                if(tCards.getState() == 1) {
                    // state 1 update 3 cards

                    final Animation fromtop = AnimationUtils.loadAnimation(GamePage.this, R.anim.fromtop);
                    final Animation fromtop2 = AnimationUtils.loadAnimation(GamePage.this, R.anim.fromtop);
                    final Animation fromtop3 = AnimationUtils.loadAnimation(GamePage.this, R.anim.fromtop);
                    tableCard1View.setVisibility(View.VISIBLE);
                    tableCard1View.startAnimation(fromtop);

                    final AnimatorSet set = new AnimatorSet();
                    Animator animator1 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_out);
                    Animator animator2 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_in);

                    final AnimatorSet set2 = new AnimatorSet();
                    Animator animator12 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_out);
                    Animator animator22 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_in);

                    set2.playSequentially(animator12, animator22);
                    set2.setTarget(tableCard2View);
                    set.playSequentially(animator1, animator2);
                    set.setTarget(tableCard1View);

                    fromtop.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tableCard2View.setVisibility(View.VISIBLE);
                            tableCard2View.startAnimation(fromtop2);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    fromtop2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tableCard3View.setVisibility(View.VISIBLE);
                            tableCard3View.startAnimation(fromtop3);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    fromtop3.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            set.start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });


                    animator1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //userCard1View.setImageDrawable(getDrawable(returnCardName(user.getCard1())));
                            tableCard1View.setVisibility(View.VISIBLE);
                            int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard1()), "drawable", "com.example.lepti.pokerapp");
                            tableCard1View.setImageResource(resID);

                            set2.start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                    animator12.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tableCard2View.setVisibility(View.VISIBLE);
                            int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard2()), "drawable", "com.example.lepti.pokerapp");
                            tableCard2View.setImageResource(resID);
                            revealCard(tableCard3View, returnCardName(tCards.getTableCard3()));
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });


                }
                else if(tCards.getState() == 2) {

                    final int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard4()), "drawable", "com.example.lepti.pokerapp");


                    final Animation appear = AnimationUtils.loadAnimation(GamePage.this, R.anim.appear);
                    tableCard4View.setVisibility(View.VISIBLE);
                    tableCard4View.startAnimation(appear);

                    final AnimatorSet set = new AnimatorSet();
                    Animator animator1 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_out);
                    Animator animator2 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_in);

                    set.playSequentially(animator1, animator2);
                    set.setTarget(tableCard4View);

                    appear.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            set.start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    animator1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tableCard4View.setImageResource(resID);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });


                }
                else if(tCards.getState() == 3) {
                    // state 3 update 1 card
                    final int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard5()), "drawable", "com.example.lepti.pokerapp");

                    final Animation appear = AnimationUtils.loadAnimation(GamePage.this, R.anim.appear);
                    tableCard5View.setVisibility(View.VISIBLE);
                    tableCard5View.startAnimation(appear);

                    final AnimatorSet set = new AnimatorSet();
                    Animator animator1 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_out);
                    Animator animator2 = AnimatorInflater.loadAnimator(GamePage.this,
                            R.animator.flip_in);

                    set.playSequentially(animator1, animator2);
                    set.setTarget(tableCard5View);

                    appear.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            set.start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    animator1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tableCard5View.setImageResource(resID);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
                else if(tCards.getState() == 5) { // time to get your winnings
                    if(user.getLastRoundPlayed() != 8) {
                        //hideAllButtons();
                        user.setLastRoundPlayed(8);

                        TreeSet<Integer> tree = new TreeSet<Integer>();
                        for(int i = 0; i < winningPlayers.size(); i++) {
                            tree.add(winningPlayers.get(i));
                        }
                        for(int i = 0; i < winningPlayers.size(); i++) {
                            if(winningPlayers.get(i) == userSpot) {
                                Toast.makeText(GamePage.this, "Congratulations! You won!", Toast.LENGTH_SHORT).show();
                                increaseUserMoney(Math.floorDiv(gVars.getTotalBet(), tree.size()));
                                break;
                            }
                            if(i == winningPlayers.size()-1) {
                                Toast.makeText(GamePage.this, "You lost!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(winningPlayers.get(winningPlayers.size()-1) == userSpot) {
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    resetGame();
                                    timer.cancel();
                                }}, 5000);
                        }
                        //DatabaseReference updateGlobal = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                        //updateGlobal.setValue(user);
                    }
                }
                else if(tCards.getState() == 6) {
                    if(user.getLastRoundPlayed() != 8) {
                        hideAllButtons();

                        if(user.getHasPlayerFolded()) {
                            Toast.makeText(GamePage.this, "Winner found! End of the round.", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            increaseUserMoney(gVars.getTotalBet());
                            Toast.makeText(GamePage.this, "Your opponent folded. You won!", Toast.LENGTH_SHORT).show();
                            DatabaseReference gameSpots = database.getReference("game-1/free-spots");
                            gameSpots.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if(Integer.parseInt(snapshot.getKey()) != userSpot) {
                                            if(snapshot.getValue(Boolean.class) == true) {
                                                hidePlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                            }
                                            else {
                                                showPlayerAvatar(Integer.parseInt(snapshot.getKey()));
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    resetGame();
                                    timer.cancel();
                                }
                            }, 4000);
                        }
                        user.setLastRoundPlayed(8);
                        DatabaseReference updateGlobal = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                        updateGlobal.setValue(user);
                    }
                }
                else if(tCards.getState() == 7) {
                    if(user.getLastRoundPlayed() == 8) return;
                    user.setMoney(gVars.getTotalBet()+user.getMoney());
                    user.setLastRoundPlayed(-1);
                    user.setHasPlayerFolded(false);
                    user.setCard1(-1);
                    user.setCard2(-1);

                    int resId = getResources().getIdentifier("backside_old", "drawable", "com.example.lepti.pokerapp");
                    userCard1View.setImageResource(resId);
                    userCard2View.setImageResource(resId);
                    tableCard1View.setImageResource(resId);
                    tableCard2View.setImageResource(resId);
                    tableCard3View.setImageResource(resId);
                    tableCard4View.setImageResource(resId);
                    tableCard5View.setImageResource(resId);

                    p2Card1.setImageResource(resId);
                    p2Card2.setImageResource(resId);
                    p3Card1.setImageResource(resId);
                    p3Card2.setImageResource(resId);
                    p4Card1.setImageResource(resId);
                    p4Card2.setImageResource(resId);

                    /* Setting up the UI */
                    resId = getResources().getIdentifier(user.getAvatar()+ "_notfolded", "drawable", "com.example.lepti.pokerapp");
                    userAvatar.setBackgroundResource(resId);
                    userNicknameText.setText(user.getNickname());
                    userMoneyText.setText("$"+Integer.toString(user.getMoney()));
                    foldButton.setVisibility(View.INVISIBLE);
                    checkButton.setVisibility(View.INVISIBLE);
                    readyBox.setVisibility(View.VISIBLE);
                    readyButton.setVisibility(View.VISIBLE);
                    raiseButton.setVisibility(View.INVISIBLE);
                    currentBetBox.setVisibility(View.INVISIBLE);
                    totalBetBox.setVisibility(View.INVISIBLE);

                    tableCardsLayout.setVisibility(View.INVISIBLE);
                    foldButton.setClickable(false);
                    checkButton.setClickable(false);
                    raiseButton.setClickable(false);
                    increaseBetButton.setClickable(false);
                    decreaseBetButton.setClickable(false);
                    increaseBetButton.setVisibility(View.INVISIBLE);
                    decreaseBetButton.setVisibility(View.INVISIBLE);
                    raiseText.setVisibility(View.INVISIBLE);
                    raiseLayout.setVisibility(View.INVISIBLE);
                    playerAvatarLayout[0].setVisibility(View.INVISIBLE);
                    playerAvatarLayout[1].setVisibility(View.INVISIBLE);
                    playerAvatarLayout[2].setVisibility(View.INVISIBLE);

                    tableCardsLayout.setVisibility(View.INVISIBLE);

                    p2Card1.setVisibility(View.INVISIBLE);
                    p2Card2.setVisibility(View.INVISIBLE);

                    p3Card1.setVisibility(View.INVISIBLE);
                    p3Card2.setVisibility(View.INVISIBLE);

                    p4Card1.setVisibility(View.INVISIBLE);
                    p4Card2.setVisibility(View.INVISIBLE);

                    userCard1View.setVisibility(View.INVISIBLE);
                    userCard2View.setVisibility(View.INVISIBLE);
                    hideAllButtons();

                    DatabaseReference ref = database.getReference("game-1/variables");
                    GameVariables gV = new GameVariables();
                    gV.setNumberPlayers(1);
                    ref.setValue(gV);

                    ref = database.getReference("game-1/table-cards");
                    ref.setValue(new TableCards());

                    ref = database.getReference("game-1/winner");
                    ref.removeValue();

                    ref = database.getReference("game-1/players-queue");
                    ref.removeValue();

                    ref = database.getReference("game-1/cards");
                    ref.removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        };
        tableCardVariablesDR.addValueEventListener(tableCardVariablesListener);

        /* Keep track of the players */

        gameSpotsDR = database.getReference("game-1/free-spots");

        gameSpotsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                freeSpots.clear();
                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        freeSpots.put(snapshot.getKey(), snapshot.getValue(Boolean.class));
                        if(Integer.parseInt(snapshot.getKey()) != userSpot) {
                            if(snapshot.getValue(Boolean.class)) {
                                hidePlayerAvatar(Integer.parseInt(snapshot.getKey()));
                            }
                            else {
                                showPlayerAvatar(Integer.parseInt(snapshot.getKey()));
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        gameSpotsDR.addValueEventListener(gameSpotsListener);


        cardsRefDR = database.getReference("game-1/cards");
        /* Keep track of the cards removed from the deck */

        cardsRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cardsUsed.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    cardsUsed.add(snapshot.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        cardsRefDR.addValueEventListener(cardsRefListener);


        winnersRefDR = database.getReference("game-1/winner");

        /* Keep track of the winning players */

        winnersRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                winningPlayers.clear();
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            winningPlayers.add(snapshot.getValue(Integer.class));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        winnersRefDR.addValueEventListener(winnersRefListener);


        pQueueRefDR = database.getReference("game-1/players-queue");
        /* Keep track of the players in queue */

        pQueueRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nextPlayerInLine.clear();
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            nextPlayerInLine.add(snapshot.getValue(Integer.class));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        pQueueRefDR.addValueEventListener(pQueueRefListener);

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gVars.setReadyPlayers(gVars.getReadyPlayers()+1);
                gVars.setPlayersCompeting(gVars.getPlayersCompeting() + (int)(Math.pow(2, userSpot)));
                gVars.setCurrentRound(0);
                readyButton.setVisibility(View.INVISIBLE);
                tableCardsLayout.setVisibility(View.VISIBLE);
                tableCard1View.setVisibility(View.INVISIBLE);
                tableCard2View.setVisibility(View.INVISIBLE);
                tableCard3View.setVisibility(View.INVISIBLE);
                if(gVars.getReadyPlayers() == gVars.getNumberPlayers() && gVars.getNumberPlayers() > 1) {
                    setupPlayerTurns();
                    gVars.setPlayerTurn(getNextInLine()+1);//);
                    gVars.setRoundBeginnerId(gVars.getPlayerTurn()-1);
                    gVars.setHasSomeonePlayed(false);
                    gVars.setOldPlayersCompeting(gVars.getPlayersCompeting());
                    DatabaseReference ref = database.getReference("game-1/players-queue");
                    ref.setValue(nextPlayerInLine);
                }
                else {
                    readyTextId.setText("Waiting ...");
                }
                DatabaseReference globalVariablesRef = database.getReference("game-1/variables");
                globalVariablesRef.setValue(gVars);
            }
        });
        int ressID = getResources().getIdentifier("backside_old", "drawable", "com.example.lepti.pokerapp");
        userCard1View.setImageResource(ressID);
        userCard2View.setImageResource(ressID);
        tableCard1View.setImageResource(ressID);
        tableCard2View.setImageResource(ressID);
        tableCard3View.setImageResource(ressID);
        tableCard4View.setImageResource(0);
        tableCard5View.setImageResource(0);

        foldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllButtons();
                gVars.setPlayersCompeting(gVars.getPlayersCompeting() - (int)(Math.pow(2, userSpot)));
                user.setHasPlayerFolded(true);
                int var = 0;
                for(int i =0; i < 3; i++) {
                    if(isKthBitSet(gVars.getPlayersCompeting(), i+1)) var++;
                }

                int resID = getResources().getIdentifier(user.getAvatar()+ "_folded", "drawable", "com.example.lepti.pokerapp");
                userAvatar.setBackgroundResource(resID);

                if(var == 1) {

                    user.setLastRoundPlayed(8);
                    DatabaseReference updateGlobal = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                    updateGlobal.setValue(user);


                    tCards.setState(6);

                    Toast.makeText(GamePage.this, "You lost!", Toast.LENGTH_SHORT).show();

                    updateGlobal = database.getReference("game-1/table-cards/");
                    updateGlobal.setValue(tCards);


                    // there was only one opponent
                }
                else {
                    DatabaseReference updateGlobal = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                    updateGlobal.setValue(user);
                    switchPlayer();
                }
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gVars.getRoundBet() == 0) { // It's a check
                    hideAllButtons();
                    switchPlayer();
                }
                else // It's a call
                {
                    int money = user.getMoney();
                    if((user.getCurrentBet()+money) < gVars.getRoundBet()) {
                        user.setMoney(0);
                        gVars.setTotalBet(gVars.getTotalBet()+gVars.getRoundBet()-(user.getCurrentBet()+money));
                        user.setCurrentBet(gVars.getRoundBet()-(user.getCurrentBet()+money));
                        Toast.makeText(GamePage.this, "You went all in.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(GamePage.this, "You've added $"+Integer.toString((gVars.getRoundBet()-user.getCurrentBet()))+" to match the pot.", Toast.LENGTH_SHORT).show();
                        user.setMoney(money-(gVars.getRoundBet()-user.getCurrentBet()));
                        gVars.setTotalBet(gVars.getTotalBet()+(gVars.getRoundBet()-user.getCurrentBet()));
                        user.setCurrentBet(gVars.getRoundBet());
                    }
                    totalBetText.setText("$"+Integer.toString(gVars.getTotalBet()));
                    currentBetText.setText("$"+Integer.toString(gVars.getRoundBet()));
                    currentBetText.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_default));
                    userMoneyText.setText("$"+Integer.toString(user.getMoney()));

                    DatabaseReference ref = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                    ref.setValue(user);

                    ref = database.getReference("game-1/variables/");
                    ref.setValue(gVars);

                    hideAllButtons();
                    switchPlayer();
                }

            }
        });

        raiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer raiseAmount = localRaiseAmount;
                if(raiseAmount < gVars.getRoundBet()) {
                    Toast.makeText(GamePage.this, "The minimum raise is $"+Integer.toString(gVars.getRoundBet()), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(raiseAmount > user.getMoney()) {
                    Toast.makeText(GamePage.this, "You don't have that much money!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(raiseAmount > gVars.getRoundBet()) {
                    Toast.makeText(GamePage.this, "You've added $"+Integer.toString((raiseAmount-user.getCurrentBet()))+" to the pot.", Toast.LENGTH_SHORT).show();
                    user.setMoney(user.getMoney() - (raiseAmount-user.getCurrentBet()) );
                    gVars.setTotalBet(gVars.getTotalBet() + (raiseAmount-user.getCurrentBet()));
                    user.setCurrentBet(raiseAmount);
                    gVars.setRoundBet(raiseAmount);
                    hideAllButtons();
                    addPlayerTurnsFromUserSpot(userSpot);
                    DatabaseReference ref = database.getReference("game-1/players-queue");
                    ref.setValue(nextPlayerInLine);
                }
                else
                {
                    Toast.makeText(GamePage.this, "You've added $"+Integer.toString((raiseAmount-user.getCurrentBet()))+" to match the pot.", Toast.LENGTH_SHORT).show();
                    user.setMoney(user.getMoney() - (raiseAmount-user.getCurrentBet()) );
                    gVars.setTotalBet(gVars.getTotalBet() + (raiseAmount-user.getCurrentBet()));
                    user.setCurrentBet(raiseAmount);
                    hideAllButtons();
                }
                totalBetText.setText("$"+Integer.toString(gVars.getTotalBet()));
                currentBetText.setText("$"+Integer.toString(gVars.getRoundBet()));
                currentBetText.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_default));
                userMoneyText.setText("$"+Integer.toString(user.getMoney()));

                DatabaseReference ref = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                ref.setValue(user);

                ref = database.getReference("game-1/variables/");
                ref.setValue(gVars);
                switchPlayer();

            }
        });
        alreadyCreated = true;
    }

    @Override
    public void onBackPressed() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        for (Map.Entry<String, Boolean> entry : freeSpots.entrySet()) {
            if(entry.getKey().equals(Integer.toString(userSpot))) {
                entry.setValue(true);
                break;
            }
        }

        gVars.setNumberPlayers(gVars.getNumberPlayers()-1);
        gVars.setReadyPlayers(gVars.getReadyPlayers()-1);

        if(gVars.getNumberPlayers() < 0) {
            gVars.setNumberPlayers(0);
        }
        if(gVars.getReadyPlayers() < 0) {
            gVars.setReadyPlayers(0);
        }

        DatabaseReference ref = database.getReference("game-1/free-spots/");
        ref.setValue(freeSpots);

        if(gVars.getNumberPlayers() == 0) { // last player in the room left

            ref = database.getReference("game-1/cards");
            ref.removeValue();

            ref = database.getReference("game-1/variables");
            ref.setValue(new GameVariables());

            ref = database.getReference("game-1/table-cards");
            ref.setValue(new TableCards());

            ref = database.getReference("game-1/winner");
            ref.removeValue();

            ref = database.getReference("game-1/players-queue");
            ref.removeValue();

            gameVariablesDR.removeEventListener(gameVariablesListener);
            playerVariablesDR.removeEventListener(playerVariablesListener);
            tableCardVariablesDR.removeEventListener(tableCardVariablesListener);
            gameSpotsDR.removeEventListener(gameSpotsListener);
            cardsRefDR.removeEventListener(cardsRefListener);
            winnersRefDR.removeEventListener(winnersRefListener);
            pQueueRefDR.removeEventListener(pQueueRefListener);

        }
        else if(gVars.getNumberPlayers() == 1) { // they were two so make the last player remaining go through ready button again
            DatabaseReference updateGlobal = database.getReference("game-1/table-cards/");
            user.setLastRoundPlayed(8);
            tCards.setState(7);
            gameVariablesDR.removeEventListener(gameVariablesListener);
            playerVariablesDR.removeEventListener(playerVariablesListener);
            tableCardVariablesDR.removeEventListener(tableCardVariablesListener);
            gameSpotsDR.removeEventListener(gameSpotsListener);
            cardsRefDR.removeEventListener(cardsRefListener);
            winnersRefDR.removeEventListener(winnersRefListener);
            pQueueRefDR.removeEventListener(pQueueRefListener);
            updateGlobal.setValue(tCards);
        }
        else {
            if(gVars.getPlayerTurn() == userSpot+1) { // he was playing

                gVars.setPlayersCompeting(gVars.getPlayersCompeting()-(int)(Math.pow(2, userSpot))); // remove the players competing
                gVars.setOldPlayersCompeting(gVars.getOldPlayersCompeting()-(int)(Math.pow(2, userSpot)));
                DatabaseReference updateGlobal = database.getReference("game-1/variables/");
                Integer spot = userSpot;
                updateGlobal.setValue(gVars);

                if(nextPlayerInLine.contains(spot)) {
                    nextPlayerInLine.remove(spot); // remove him from the queue
                    ref = database.getReference("game-1/players-queue");
                    ref.setValue(nextPlayerInLine);
                }
// if he was in a 1v1 situation
                gameVariablesDR.removeEventListener(gameVariablesListener);
                playerVariablesDR.removeEventListener(playerVariablesListener);
                tableCardVariablesDR.removeEventListener(tableCardVariablesListener);
                gameSpotsDR.removeEventListener(gameSpotsListener);
                cardsRefDR.removeEventListener(cardsRefListener);
                winnersRefDR.removeEventListener(winnersRefListener);
                pQueueRefDR.removeEventListener(pQueueRefListener);

                int var = 0;
                for(int i =0; i < 3; i++) {
                    if(isKthBitSet(gVars.getPlayersCompeting(), i+1)) var++;
                }

                // if the player was in a 1v1 situation
                if(var == 1) {

                    user.setLastRoundPlayed(8);
                    user.setHasPlayerFolded(true);
                    updateGlobal = database.getReference("game-1/table-cards/");
                    tCards.setState(6);
                    updateGlobal.setValue(tCards);
                    // there was only one opponent
                }
                else {
                    user.setHasPlayerFolded(true);
                    switchPlayer();
                }

                // it's not a 1v1 situation
            }
            else { // he wasn't the only one playing

                gVars.setPlayersCompeting(gVars.getPlayersCompeting()-(int)(Math.pow(2, userSpot))); // remove the players competing
                gVars.setOldPlayersCompeting(gVars.getOldPlayersCompeting()-(int)(Math.pow(2, userSpot)));
                DatabaseReference updateGlobal = database.getReference("game-1/variables/");
                updateGlobal.setValue(gVars);
                Integer spot = userSpot;
                if(nextPlayerInLine.contains(spot)) {
                    nextPlayerInLine.remove(spot); // remove him from the queue
                    ref = database.getReference("game-1/players-queue");
                    ref.setValue(nextPlayerInLine);
                }
// if he was in a 1v1 situation
                gameVariablesDR.removeEventListener(gameVariablesListener);
                playerVariablesDR.removeEventListener(playerVariablesListener);
                tableCardVariablesDR.removeEventListener(tableCardVariablesListener);
                gameSpotsDR.removeEventListener(gameSpotsListener);
                cardsRefDR.removeEventListener(cardsRefListener);
                winnersRefDR.removeEventListener(winnersRefListener);
                pQueueRefDR.removeEventListener(pQueueRefListener);
                int var = 0;
                for(int i =0; i < 3; i++) {
                    if(isKthBitSet(gVars.getPlayersCompeting(), i+1)) var++;
                }

                // if the player was in a 1v1 situation
                if(var == 1) {

                    user.setLastRoundPlayed(8);
                    user.setHasPlayerFolded(true);
                    updateGlobal = database.getReference("game-1/table-cards/");
                    tCards.setState(6);
                    updateGlobal.setValue(tCards);
                    // there was only one opponent
                }

            }
        }
        GamePage.super.onBackPressed();
        this.finish();
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

    private int getNextInLine() {
        if(nextPlayerInLine.isEmpty()) {
            return -1;
        }
        Integer next = nextPlayerInLine.get(0);
        nextPlayerInLine.remove(next);
        return next;
    }
    private void setupPlayerTurns() {
        nextPlayerInLine.clear();
        if(isPlayerCompeting(0)) nextPlayerInLine.add(0);
        if(isPlayerCompeting(1)) nextPlayerInLine.add(1);
        if(isPlayerCompeting(2)) nextPlayerInLine.add(2);
        if(isPlayerCompeting(3)) nextPlayerInLine.add(3);
    }

    private void addPlayerTurnsFromUserSpot(int spot) {
        for(int i = 1; i < 4; i++) {
            if (isPlayerCompeting(((spot + i) % 4)) && !nextPlayerInLine.contains(((spot + i) % 4))) nextPlayerInLine.add(((spot + i) % 4));
        }
    }

    private boolean isPlayerCompeting(int id) {
        if (isKthBitSet(gVars.getPlayersCompeting(), id+1)) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isKthBitSet(int n,
                                int k)
    {
        if ((n & (1 << (k - 1))) >= 1)
            return true;
        else
            return false;
    }

    private void fadeCardAway(final ImageView card) {
        Animation fade_out = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_out);
        card.startAnimation(fade_out);
        fade_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int resId = getResources().getIdentifier("backside_old", "drawable", "com.example.lepti.pokerapp");
                card.setImageResource(resId);
                card.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private void revealCardsPlayer() {

        userCard1View.setVisibility(View.VISIBLE);
        userCard2View.setVisibility(View.VISIBLE);

        Animation fade_in1 = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
        Animation fade_in2 = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);

        final AnimatorSet set = new AnimatorSet();
        Animator animator1 = AnimatorInflater.loadAnimator(GamePage.this,
                R.animator.flip_out);
        Animator animator2 = AnimatorInflater.loadAnimator(GamePage.this,
                R.animator.flip_in);

        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                userCard1View.setImageDrawable(getDrawable(returnCardName(user.getCard1())));
                revealCard(userCard2View, returnCardName(user.getCard2()));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        set.playSequentially(animator1, animator2);
        set.setTarget(userCard1View);

        userCard1View.startAnimation(fade_in1);
        userCard2View.startAnimation(fade_in2);
        fade_in1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                set.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private void revealCard(final ImageView imageid, final String cardName) {
        AnimatorSet set = new AnimatorSet();
        Animator animator1 = AnimatorInflater.loadAnimator(GamePage.this,
                R.animator.flip_out);
        Animator animator2 = AnimatorInflater.loadAnimator(GamePage.this,
                R.animator.flip_in);

        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageid.setImageDrawable(getDrawable(cardName));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.playSequentially(animator1, animator2);
        set.setTarget(imageid);
        set.start();
    }

    private Drawable getDrawable(String s) {
        String s2 = "drawable/" + s;
        int imageResource = getResources().getIdentifier(s2, null,
                getPackageName());
        return getResources().getDrawable(imageResource);
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
    private int rankHandName(String handName) {
       if(handName.equals("One Pair")) {
           return 1;
       }
       else if(handName.equals("Two Pairs")) {
           return 2;
       }
       else if(handName.equals("Three of a Kind")) {
           return 3;
       }
       else if(handName.equals("Straight")) {
           return 4;
       }
       else if(handName.equals("Flush")) {
           return 5;
       }
       else if(handName.equals("Full House")) {
           return 6;
       }
       else if(handName.equals("Four of a Kind")) {
           return 7;
       }
       else if(handName.equals("Straight Flush")) {
           return 8;
       }
       else if(handName.equals("Royal Flush")) {
           return 9;
       }
       else return 0;
    }

    private void displayCheckButton() {
        if(gVars.getRoundBet() > 0 && user.getMoney() > 0) {
            int resID = getResources().getIdentifier("call_button", "drawable", "com.example.lepti.pokerapp");
            checkButton.setImageResource(resID);
        }
        else
        {
            int resID = getResources().getIdentifier("check_button", "drawable", "com.example.lepti.pokerapp");
            checkButton.setImageResource(resID);
        }
        checkButton.setVisibility(View.VISIBLE);
        checkButton.setClickable(true);
    }

    private void displayRaiseButton() {
        if(user.getMoney() > 0) {
            raiseButton.setVisibility(View.VISIBLE);
            raiseButton.setClickable(true);
            raiseText.setVisibility(View.VISIBLE);
            increaseBetButton.setVisibility(View.VISIBLE);
            decreaseBetButton.setVisibility(View.VISIBLE);
            increaseBetButton.setClickable(true);
            decreaseBetButton.setClickable(true);
            raiseLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayFoldButton() {
        if(gVars.getRoundBet() > 0 && user.getMoney() > 0) {
            foldButton.setVisibility(View.VISIBLE);
            foldButton.setClickable(true);
        }
    }

    private void displayAllButtons() {
        displayCheckButton();
        displayRaiseButton();
        displayFoldButton();
        return;
    }

    private void hideAllButtons() {
        raiseButton.setVisibility(View.INVISIBLE);
        raiseText.setVisibility(View.INVISIBLE);
        foldButton.setVisibility(View.INVISIBLE);
        checkButton.setVisibility(View.INVISIBLE);
        increaseBetButton.setVisibility(View.INVISIBLE);
        decreaseBetButton.setVisibility(View.INVISIBLE);
        increaseBetButton.setClickable(false);
        decreaseBetButton.setClickable(false);
        raiseLayout.setVisibility(View.INVISIBLE);
        raiseButton.setClickable(false);
        foldButton.setClickable(false);
        raiseButton.setClickable(false);
        return;
    }
    private void updateHands() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference updateGlobal = database.getReference("game-1/table-cards/");
        int darank = rankHandName(tCards.getWinningHand());
        int playerHandRank = rankHandName(user.getBestHandName());

        if(playerHandRank > darank) {
            tCards.setWinningCard1(userBestHand[0]);
            tCards.setWinningCard2(userBestHand[1]);
            tCards.setWinningCard3(userBestHand[2]);
            tCards.setWinningCard4(userBestHand[3]);
            tCards.setWinningCard5(userBestHand[4]);
            tCards.setWinningHand(user.getBestHandName());

            updateGlobal.setValue(tCards);

            updateGlobal = database.getReference("game-1/winner/");
            updateGlobal.removeValue();

            List<Integer> winningPlayerEx = new ArrayList<>();
            winningPlayerEx.add(userSpot);
            updateGlobal.setValue(winningPlayerEx);
        }
        else if(playerHandRank == darank) {
            int trigger = 0;
            if((tCards.getWinningCard1() != 0 && tCards.getWinningCard1() < userBestHand[0]) || (tCards.getWinningCard1() == 0 && tCards.getWinningCard1() != userBestHand[0])) trigger = 1;
            else if((tCards.getWinningCard2() != 0 && tCards.getWinningCard2() < userBestHand[0]) || (tCards.getWinningCard2() == 0 && tCards.getWinningCard2() != userBestHand[0])) trigger = 1;
            else if((tCards.getWinningCard3() != 0 && tCards.getWinningCard3() < userBestHand[0]) || (tCards.getWinningCard3() == 0 && tCards.getWinningCard3() != userBestHand[0])) trigger = 1;
            else if((tCards.getWinningCard4() != 0 && tCards.getWinningCard4() < userBestHand[0]) || (tCards.getWinningCard4() == 0 && tCards.getWinningCard4() != userBestHand[0])) trigger = 1;
            else if((tCards.getWinningCard5() != 0 && tCards.getWinningCard5() < userBestHand[0]) || (tCards.getWinningCard5() == 0 && tCards.getWinningCard5() != userBestHand[0])) trigger = 1;

            if(trigger == 1) {
                tCards.setWinningCard1(userBestHand[0]);
                tCards.setWinningCard2(userBestHand[1]);
                tCards.setWinningCard3(userBestHand[2]);
                tCards.setWinningCard4(userBestHand[3]);
                tCards.setWinningCard5(userBestHand[4]);
                updateGlobal.setValue(tCards);

                updateGlobal = database.getReference("game-1/winner/");
                updateGlobal.removeValue();

                List<Integer> winningPlayerEx = new ArrayList<>();
                winningPlayerEx.add(userSpot);
                updateGlobal.setValue(winningPlayerEx);


            }
            else {
                updateGlobal = database.getReference("game-1/winner/");
                winningPlayers.add(userSpot);
                updateGlobal.setValue(winningPlayers);
            }
        }
        switchPlayer();
        return;
    }

    private void resetGame() {

        /* reset the winner field */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference updateGlobal = database.getReference("game-1/winner/");
        updateGlobal.removeValue();

        /* reset cards used */
        updateGlobal = database.getReference("game-1/cards/");
        updateGlobal.removeValue();


        /* reset table-cards */
        updateGlobal = database.getReference("game-1/table-cards/");
        updateGlobal.setValue(new TableCards());

        /* reset players-queue */
        updateGlobal = database.getReference("game-1/players-queue/");
        updateGlobal.removeValue();

        /* */
        gVars.setCurrentRound(-1);
        //gVars.setReadyPlayers(0);
        gVars.setHasSomeonePlayed(false);
        gVars.setTotalBet(0);
        gVars.setRoundBet(0);
        gVars.setPlayerTurn(0);
        gVars.setPlayersCompeting(gVars.getOldPlayersCompeting());

        //gVars.setPlayersCompeting(0);

        updateGlobal = database.getReference("game-1/variables/");
        updateGlobal.setValue(gVars);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                restartGame();
                timer.cancel();
            }}, 4000);
    }

    private void restartGame() {
        setupPlayerTurns();
        gVars.setCurrentRound(0);
        gVars.setPlayerTurn(getNextInLine()+1);//);
        gVars.setRoundBeginnerId(gVars.getPlayerTurn()-1);
        gVars.setHasSomeonePlayed(false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("game-1/players-queue");
        ref.setValue(nextPlayerInLine);

        ref = database.getReference("game-1/variables/");
        ref.setValue(gVars);
    }
    private void increaseUserMoney(int money) {
        user.setMoney(user.getMoney()+money);
        userMoneyText.setText("$"+Integer.toString(user.getMoney()));
        return;
    }
    private void triggerRoundStartEvent(int currentRound) {

        if(currentRound == 0 && (user.getCard1() == -1 || user.getCard2() == -1)) {

            readyTextId.setText("Click when ready");
            readyBox.setVisibility(View.INVISIBLE);
            raiseText.setText("0");
            totalBetText.setText("$0");
            currentBetText.setText("$0");
            currentBetText.setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_default));
            totalBetBox.setVisibility(View.VISIBLE);
            currentBetBox.setVisibility(View.VISIBLE);

            if(playerAvatarLayout[0].getVisibility() == View.VISIBLE) {
                Animation fade_in = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
                p2Card1.setVisibility(View.VISIBLE);
                p2Card1.startAnimation(fade_in);
                p2Card2.setVisibility(View.VISIBLE);
                p2Card2.startAnimation(fade_in);
            }

            if(playerAvatarLayout[1].getVisibility() == View.VISIBLE) {
                Animation fade_in = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
                p3Card1.setVisibility(View.VISIBLE);
                p3Card1.startAnimation(fade_in);
                p3Card2.setVisibility(View.VISIBLE);
                p3Card2.startAnimation(fade_in);
            }

            if(playerAvatarLayout[2].getVisibility() == View.VISIBLE) {
                Animation fade_in = AnimationUtils.loadAnimation(GamePage.this, R.anim.fade_in);
                p4Card1.setVisibility(View.VISIBLE);
                p4Card1.startAnimation(fade_in);
                p4Card2.setVisibility(View.VISIBLE);
                p4Card2.startAnimation(fade_in);
            }
            generateCardsForPlayer();
            displayAllButtons();
        }
        if(currentRound > 0 && currentRound < 4) { // Distribute table cards on rounds 1, 2, 3
            generateTableCards(gVars.getCurrentRound());
            displayAllButtons();
        }
        else if(currentRound == 4) { // Check hand on round 4

            /* Update the user's hand in their database */
            String hand = getUserHand();
            user.setBestHandName(hand);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference updateGlobal = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
            updateGlobal.setValue(user);

            tCards.setWinningCard1(userBestHand[0]);
            tCards.setWinningCard2(userBestHand[1]);
            tCards.setWinningCard3(userBestHand[2]);
            tCards.setWinningCard4(userBestHand[3]);
            tCards.setWinningCard5(userBestHand[4]);
            tCards.setState(4);
            tCards.setWinningHand(user.getBestHandName());

            updateGlobal = database.getReference("game-1/table-cards/");
            updateGlobal.setValue(tCards);

            winningPlayers.add(userSpot);
            updateGlobal = database.getReference("game-1/winner/");
            updateGlobal.setValue(winningPlayers);

            hideAllButtons();
        }
        else if(currentRound == 5) { // Reveal results
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            if(playerAvatarLayout[0].getVisibility() == View.VISIBLE) {
                DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(pId[0]));
                pReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                        pMoneyText[0].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                        if(layoutPlayerVariables.getHasPlayerFolded()) return;
                        int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                        pAvatar[0].setBackgroundResource(resID);
                        revealCard(p2Card1, returnCardName(layoutPlayerVariables.getCard1()));
                        revealCard(p2Card2, returnCardName(layoutPlayerVariables.getCard2()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                    }
                });
            }

            if(playerAvatarLayout[1].getVisibility() == View.VISIBLE) {
                DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(pId[1]));
                pReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                        pMoneyText[1].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                        if(layoutPlayerVariables.getHasPlayerFolded()) return;
                        int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                        pAvatar[1].setBackgroundResource(resID);
                        revealCard(p3Card1, returnCardName(layoutPlayerVariables.getCard1()));
                        revealCard(p3Card2, returnCardName(layoutPlayerVariables.getCard2()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                    }
                });
            }

            if(playerAvatarLayout[2].getVisibility() == View.VISIBLE) {
                DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(pId[2]));
                pReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                        pMoneyText[2].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                        if(layoutPlayerVariables.getHasPlayerFolded()) return;
                        int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                        pAvatar[2].setBackgroundResource(resID);
                        revealCard(p4Card1, returnCardName(layoutPlayerVariables.getCard1()));
                        revealCard(p4Card2, returnCardName(layoutPlayerVariables.getCard2()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                    }
                });
            }

            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    winnerReveal();
                    timer.cancel();
                }}, 3000);
            hideAllButtons();
        }
        else if(currentRound == 8 || currentRound == 6) {
            hideAllButtons();
        }

        /* Update the global variables */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference updateGlobal = database.getReference("game-1/variables");
        gVars.setHasSomeonePlayed(true);
        updateGlobal.setValue(gVars);

        if(currentRound == 4) {
            switchPlayer();
        }
    }

    private void winnerReveal() {
        tCards.setState(5);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference updateGlobal = database.getReference("game-1/table-cards/");
        updateGlobal.setValue(tCards);
    }

    private void switchPlayer() {
        /* Hide the buttons */
        hideAllButtons();


        /* Database variables */
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        int resID = getResources().getIdentifier(user.getAvatar()+ "_notfolded", "drawable", "com.example.lepti.pokerapp");
        if(user.getHasPlayerFolded()) resID = getResources().getIdentifier(user.getAvatar()+ "_folded", "drawable", "com.example.lepti.pokerapp");
        userAvatar.setBackgroundResource(resID);

        /* The round at the beginning of the round was the one this user currently has */
        if(gVars.getRoundBeginnerId() == userSpot) {
            gVars.setRoundStartBet(user.getCurrentBet());
        }

        int playerToCome = getNextInLine();
        if(playerToCome == -1) {
            // setup again
            setupPlayerTurns();
            playerToCome = getNextInLine();
            gVars.setPlayerTurn(playerToCome+1);
            gVars.setHasSomeonePlayed(false); // The next player will be first of the new round
            gVars.setCurrentRound(gVars.getCurrentRound()+1);
            gVars.setRoundBet(0);
        }
        else
        {
            gVars.setPlayerTurn(playerToCome+1);
        }

        DatabaseReference updateGlobal = database.getReference("game-1/players-queue");
        updateGlobal.setValue(nextPlayerInLine);
        updateGlobal = database.getReference("game-1/variables");
        updateGlobal.setValue(gVars);
    }

    private void showPlayerAvatar(final int playerId) {
        final int layoutId = (playerId+(4-userSpot))%4;
        Log.d("IDD:", "Spot "+ Integer.toString(userSpot)+ " playerId=" +Integer.toString(playerId) + " layoutId=" + Integer.toString(layoutId) );
        pId[layoutId-1] = playerId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(playerId));
        pReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                /* Change the layout with the user's information*/
                pMoneyText[layoutId-1].setText("$"+Integer.toString(layoutPlayerVariables.getMoney()));
                pNicknameText[layoutId-1].setText(layoutPlayerVariables.getNickname());
                int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_notfolded", "drawable", "com.example.lepti.pokerapp");
                if(gVars.getPlayerTurn() == playerId+1) {
                    resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_yourturn", "drawable", "com.example.lepti.pokerapp");
                }
                if(layoutPlayerVariables.getHasPlayerFolded()) {
                    resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar()+"_folded", "drawable", "com.example.lepti.pokerapp");
                }
                pAvatar[layoutId-1].setBackgroundResource(resID);

                /* Show the avatar */
                playerAvatarLayout[layoutId-1].setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private String returnSuitName(int card) {
        String suitName = "";
        int suit = Math.floorDiv(card, 13);
        switch(suit) {
            case 0: {
                suitName = "clubs";
                break;
            }
            case 1: {
                suitName = "diamonds";
                break;
            }
            case 2: {
                suitName = "spades";
                break;
            }
            case 3: {
                suitName = "hearts";
                break;
            }
        }
        return suitName;
    }

    private String returnCardRank(int card) {

        String cardRank;
        int rank = (card)%13;
        if(rank == 0) {
            cardRank = "ace";
        }
        else if(rank > 0 && rank < 10) {
            cardRank = "c" + Integer.toString((rank+1));
        }
        else if(rank == 10) {
            cardRank = "jack";
        }
        else if(rank == 11) {
            cardRank =  "queen";
        }
        else {
            cardRank = "king";
        }
        return cardRank;
    }

    private String returnCardName(int card) {
        String suitName = returnSuitName(card);
        String cardRank = returnCardRank(card);
        String cardName = cardRank + "_of_" + suitName;
        return cardName;
    }

    private int getRandomCardFromDeck(List<Integer> cardList) {
        Random rand = new Random();
        int randomCard = cardList.get(rand.nextInt(cardList.size()));
        cardsUsed.add(randomCard);
        return randomCard;
    }

    private List<Integer> getCurrentDeckOfCards() {
        List<Integer> cardList = new ArrayList<>();
        for(int i = 0; i < 52; i++) {
            cardList.add(i);
        }
        for(Integer list : cardsUsed) {
            cardList.remove(list);
        }
        return cardList;
    }

    private void generateCardsForPlayer() {
        user.setCard1(getRandomCardFromDeck(getCurrentDeckOfCards()));
        user.setCard2(getRandomCardFromDeck(getCurrentDeckOfCards()));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference cardsRef = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
        cardsRef.setValue(user);

        cardsRef = database.getReference("game-1/cards");
        cardsRef.setValue(cardsUsed);

        //int resID = getResources().getIdentifier(returnCardName(user.getCard1()), "drawable", "com.example.lepti.pokerapp");
        //userCard1View.setImageResource(resID);
        revealCardsPlayer();
        //resID = getResources().getIdentifier(returnCardName(user.getCard2()), "drawable", "com.example.lepti.pokerapp");
        //userCard2View.setImageResource(resID);
    }

    private void generateTableCards(int phase) {
        if(phase == 1) {
            tCards.setTableCard1(getRandomCardFromDeck(getCurrentDeckOfCards()));
            tCards.setTableCard2(getRandomCardFromDeck(getCurrentDeckOfCards()));
            tCards.setTableCard3(getRandomCardFromDeck(getCurrentDeckOfCards()));
            tCards.setState(1);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference cardsRef = database.getReference("game-1/table-cards");
            cardsRef.setValue(tCards);

            cardsRef = database.getReference("game-1/cards");
            cardsRef.setValue(cardsUsed);
        }
        else if(phase == 2) {
            tCards.setTableCard4(getRandomCardFromDeck(getCurrentDeckOfCards()));
            tCards.setState(2);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference cardsRef = database.getReference("game-1/table-cards");
            cardsRef.setValue(tCards);

            cardsRef = database.getReference("game-1/cards");
            cardsRef.setValue(cardsUsed);
        }
        else if(phase == 3){
            tCards.setTableCard5(getRandomCardFromDeck(getCurrentDeckOfCards()));
            tCards.setState(3);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference cardsRef = database.getReference("game-1/table-cards");
            cardsRef.setValue(tCards);

            cardsRef = database.getReference("game-1/cards");
            cardsRef.setValue(cardsUsed);
        }
    }
    private void hidePlayerAvatar(int playerId) {
        int layoutId = (playerId+(4-userSpot))%4;
        playerAvatarLayout[layoutId-1].setVisibility(View.INVISIBLE);
    }

    public String getUserHand() {

        userDeckOfCards[0] = user.getCard1();
        userDeckOfCards[1] = user.getCard2();
        userDeckOfCards[2] = tCards.getTableCard1();
        userDeckOfCards[3] = tCards.getTableCard2();
        userDeckOfCards[4] = tCards.getTableCard3();
        userDeckOfCards[5] = tCards.getTableCard4();
        userDeckOfCards[6] = tCards.getTableCard5();

        /* We generate a TreeSet that contains all the player's cards */
        Set<Integer> allCards = new TreeSet<>();
        allCards.add(userDeckOfCards[0]%13);
        allCards.add(userDeckOfCards[1]%13);
        allCards.add(userDeckOfCards[2]%13);
        allCards.add(userDeckOfCards[3]%13);
        allCards.add(userDeckOfCards[4]%13);
        allCards.add(userDeckOfCards[5]%13);
        allCards.add(userDeckOfCards[6]%13);
        int firstRemoved = -1;

        /* We create a reverse set */
        Set<Integer> reverseCards = ((TreeSet<Integer>) allCards).descendingSet();
        Iterator<Integer> iteratorReverse;
        iteratorReverse = reverseCards.iterator();

        /* We iterate through the set */
        int index = 0;
        if(reverseCards.contains(0)) {
            userBestHand[index] = 0;
            index++;
        }
        while (iteratorReverse.hasNext() && index < 5) {
            int card = iteratorReverse.next();
            userBestHand[index] = card;
            index++;
        }

        ArrayList<Integer> pairs = new ArrayList<>();
        String results = "No Pair";
        for(int i = 0; i < 6; i++) {
            for(int y = i+1; y < 7; y++ ) {
                if((userDeckOfCards[i]%13) == (userDeckOfCards[y]%13)) {
                    pairs.add((userDeckOfCards[i]%13));
                    break;
                }
            }
        }
        int highestStraightFlush = -1;
        int highestStraight = -1;
        for(int i = 0; i < 7; i++) {
            int middleCardRank = (userDeckOfCards[i])%13;
            if( middleCardRank > 1 && middleCardRank < 12) {
                boolean[] straightFound = new boolean[4];
                straightFound[0] = false;
                straightFound[1] = false;
                straightFound[2] = false;
                straightFound[3] = false;
                boolean isFlush = true;
                int flushSuit = Math.floorDiv(userDeckOfCards[i], 13);
                for(int u = 0; u < 7; u++) {
                    if(u == i) continue;
                    if( ( (middleCardRank+1) %13 ) == ( (userDeckOfCards[u]) %13 ) && straightFound[2] == false) {
                        straightFound[2] = true;
                        if(flushSuit != Math.floorDiv(userDeckOfCards[u], 13) && isFlush) {
                            boolean doesSameSuitExist = false;
                            for(int y = u+1; y < 7; y++) {
                                if(y == i) continue;
                                if(( (userDeckOfCards[u]) %13 ) == ( (userDeckOfCards[y]) %13 ) && flushSuit == Math.floorDiv(userDeckOfCards[y], 13)) {
                                    doesSameSuitExist = true;
                                    break;
                                }
                            }
                            isFlush = doesSameSuitExist;
                        }
                    }
                    if( ( (middleCardRank+2) %13 ) == ( (userDeckOfCards[u]) %13 )  && straightFound[3] == false) {
                        straightFound[3] = true;
                        if(flushSuit != Math.floorDiv(userDeckOfCards[u], 13) && isFlush) {
                            boolean doesSameSuitExist = false;
                            for(int y = u+1; y < 7; y++) {
                                if(y == i) continue;
                                if(( (userDeckOfCards[u]) %13 ) == ( (userDeckOfCards[y]) %13 ) && flushSuit == Math.floorDiv(userDeckOfCards[y], 13)) {
                                    doesSameSuitExist = true;
                                    break;
                                }
                            }
                            isFlush = doesSameSuitExist;
                        }
                    }
                    if( ( (middleCardRank-1) %13 ) == ( (userDeckOfCards[u]) %13 )  && straightFound[1] == false) {
                        straightFound[1] = true;
                        if(flushSuit != Math.floorDiv(userDeckOfCards[u], 13) && isFlush) {
                            boolean doesSameSuitExist = false;
                            for(int y = u+1; y < 7; y++) {
                                if(y == i) continue;
                                if(( (userDeckOfCards[u]) %13 ) == ( (userDeckOfCards[y]) %13 ) && flushSuit == Math.floorDiv(userDeckOfCards[y], 13)) {
                                    doesSameSuitExist = true;
                                    break;
                                }
                            }
                            isFlush = doesSameSuitExist;
                        }
                    }
                    if( ( (middleCardRank-2) %13 ) == ( (userDeckOfCards[u]) %13 )  && straightFound[0] == false) {
                        straightFound[0] = true;
                        if(flushSuit != Math.floorDiv(userDeckOfCards[u], 13) && isFlush) {
                            boolean doesSameSuitExist = false;
                            for(int y = u+1; y < 7; y++) {
                                if(y == i) continue;
                                if(( (userDeckOfCards[u]) %13 ) == ( (userDeckOfCards[y]) %13 ) && flushSuit == Math.floorDiv(userDeckOfCards[y], 13)) {
                                    doesSameSuitExist = true;
                                    break;
                                }
                            }
                            isFlush = doesSameSuitExist;
                        }
                    }
                }
                if(straightFound[0] && straightFound[1] && straightFound[2] && straightFound[3]) {
                    int newHighest = (middleCardRank+2) %13;
                    if(highestStraight < newHighest || newHighest == 0) highestStraight = newHighest ;
                    if((highestStraightFlush < newHighest || newHighest == 0 )&& isFlush) highestStraightFlush = newHighest;
                }
            }
        }
        Set<Integer> flushSet = new TreeSet<>();
        for(int u = 0; u < 3; u++) {
            for(int i = 0; i < 7; i++) {
                int cardRank = (userDeckOfCards[i])%13;
                if(Math.floorDiv(userDeckOfCards[i], 13) == u) {
                    flushSet.add(cardRank);
                }
            }
            if(flushSet.size() < 5) {
                flushSet.clear();
            }
            else {
                break;
            }
        }

        Set<Integer> unique = new HashSet<>(pairs);
        Map<Integer, Integer> unsortedMap = new HashMap<>();
        for (Integer key : unique) {
            Integer numberOfSimilarCards = Collections.frequency(pairs, key)+1;
            unsortedMap.put(key, numberOfSimilarCards);
            System.out.println("PAIRS: Rank " + Integer.toString(key) + " has " + Integer.toString(numberOfSimilarCards));
        }

        List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        int[] allPairsCheck = new int[13];

        for(int i = 0; i < 7; i++) {
            int rank = 0;
            if((userDeckOfCards[i]%13) == 0) rank = 12;
            else {
                rank = ((userDeckOfCards[i]-1)%13);
            }
            allPairsCheck[rank%13] += 1;
        }

        int max3 = -1;
        int max4 = -1;
        int max22 = -1;
        int max2 = -1;

        for(int i = 0; i < 13; i++) {
            if(allPairsCheck[i] == 4) {
                max4 = i;
            }
            else if(allPairsCheck[i] == 3 && max3 < i) {
                max3 = i;
            }
            else if(allPairsCheck[i] == 2 && max2 < i) {
                max2 = i;
            }
        }

        for(int i = 0; i<13; i++) {
            if(allPairsCheck[i] == 2 && max22 < i && i != max2) {
                max22 = i;
            }
        }

        if(max4 != -1) {
            results = "Four of a Kind";
            Integer number = (max4+1)%13;
            allCards.remove(number);

            Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
            Iterator<Integer> iterator;
            iterator = reverseAllCards.iterator();

            while (iterator.hasNext()) {
                int card = iterator.next();
                userBestHand[0] = number;
                userBestHand[1] = number;
                userBestHand[2] = number;
                userBestHand[3] = number;
                if(reverseAllCards.contains(0)) userBestHand[4] = 0;
                else userBestHand[4] = card;
                break;
            }
        }
        else if(max3 != -1 && max2 != -1) {
            results = "Full House";
            Integer number = (max3+1)%13;
            allCards.remove(number);
            userBestHand[0] = number;
            userBestHand[1] = number;
            userBestHand[2] = number;
            number = (max2+1)%13;
            allCards.remove(number);
            userBestHand[3] = number;
            userBestHand[4] = number;
        }
        else if(max3 != -1) {

            results = "Three of a Kind";
            Integer number = (max3+1)%13;
            allCards.remove(number);
            Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
            Iterator<Integer> iterator;
            iterator = reverseAllCards.iterator();


            userBestHand[0] = number;
            userBestHand[1] = number;
            userBestHand[2] = number;


            int i = 3;
            if(reverseAllCards.contains(0)) {
                userBestHand[3] = 0;
                i++;
            }
            while (iterator.hasNext() && i < 5) {
                int card = iterator.next();
                userBestHand[i] = card;
                i++;
            }
        }
        else if(max2 != -1 && max22 != -1) {
            results = "Two Pairs";
            Integer number = (max2+1)%13;
            allCards.remove(number);
            userBestHand[0] = number;
            userBestHand[1] = number;

            number = (max22+1)%13;
            allCards.remove(number);
            userBestHand[2] = number;
            userBestHand[3] = number;


            Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
            Iterator<Integer> iterator;
            iterator = reverseAllCards.iterator();

            if(reverseAllCards.contains(0)) userBestHand[4] = 0;
            else {

                while (iterator.hasNext()) {
                    int card = iterator.next();
                    userBestHand[4] = card;
                    break;
                }
            }
        }
        else if(max2 != -1) {
            results = "One Pair";

            Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
            Iterator<Integer> iterator;
            iterator = reverseAllCards.iterator();
            Integer number = (max2+1)%13;

            userBestHand[0] = number;
            userBestHand[1] = number;

            int i = 2;

            if(reverseAllCards.contains(0)) {
                userBestHand[2] = 0;
                i++;
            }
            while (iterator.hasNext() && i < 5) {
                int card = iterator.next();
                userBestHand[i] = card;
                i++;
            }
        }
        else {
            results = "No Pair";
        }

        if(highestStraightFlush == 0) {
            userBestHand[0] = 0;
            userBestHand[1] = 12;
            userBestHand[2] = 11;
            userBestHand[3] = 10;
            userBestHand[4] = 9;
            results = "Royal Flush";
        }
        else if(highestStraightFlush != -1) {
            userBestHand[0] = (highestStraightFlush)%13;
            userBestHand[1] = (highestStraightFlush-1)%13;
            userBestHand[2] = (highestStraightFlush-2)%13;
            userBestHand[3] = (highestStraightFlush-3)%13;
            userBestHand[4] = (highestStraightFlush-4)%13;
            results = "Straight Flush";
        }
        else if(!results.equals("Four of a Kind") && !results.equals("Full House") && !flushSet.isEmpty()) {
            int i = 0;
            if(flushSet.contains(0)) {
                userBestHand[0] = 0;
                i++;
            }

            /* We create a reverse set */
            Set<Integer> flushSetReverse = ((TreeSet<Integer>) flushSet).descendingSet();
            Iterator iterator;
            iterator = flushSetReverse.iterator();

            /* We iterate through the set */
            while (iterator.hasNext()) {
                userBestHand[i] = (int) iterator.next();
                i++;
                if(i == 5) break;
            }

            results = "Flush";
        }
        else if(!results.equals("Four of a Kind") && !results.equals("Full House") && highestStraight != -1) {
            if(highestStraight == 0) {
                userBestHand[0] = 0;
                userBestHand[1] = 12;
                userBestHand[2] = 11;
                userBestHand[3] = 10;
                userBestHand[4] = 9;
            }
            else {
                userBestHand[0] = highestStraight;
                userBestHand[1] = (highestStraight-1)%13;
                userBestHand[2] = (highestStraight-2)%13;
                userBestHand[3] = (highestStraight-3)%13;
                userBestHand[4] = (highestStraight-4)%13;
            }
            results = "Straight";
        }

        if(results.equals("No Pair")) {

        }
        return results;
    }
}
