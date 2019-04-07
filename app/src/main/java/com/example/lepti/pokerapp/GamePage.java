package com.example.lepti.pokerapp;


import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import static android.widget.Toast.LENGTH_SHORT;

import classes.GameVariables;
import classes.PlayerVariables;
import classes.TableCards;

public class GamePage extends AppCompatActivity {

    int userSpot;
    ImageView userAvatar;
    Button userNicknameText;
    Button userMoneyText;
    Button readyButton;

    ImageView[] pAvatar = new ImageView[3];
    Button[] pNicknameText = new Button[3];
    Button[] pMoneyText = new Button[3];
    Button foldButton;
    ImageView userCard1View;
    ImageView userCard2View;
    ImageView tableCard1View;
    ImageView tableCard2View;
    ImageView tableCard3View;
    ImageView tableCard4View;
    ImageView tableCard5View;
    EditText raiseEditText;
    RelativeLayout[] playerAvatarLayout = new RelativeLayout[3];
    Button checkButton;
    Button raiseButton;


    int playerTurn = 0;
    int currId = 2;
    int[] userDeckOfCards = new int[7];
    int[] userBestHand = new int[5];

    int phase = 1;
    int totalPlayers = 1;

    ArrayList<Integer> cardsInUse  = new ArrayList<Integer>();

    List<Integer> cardsUsed  = new ArrayList<>();
    List<Integer> winningPlayers = new ArrayList<>();
    PlayerVariables user;
    GameVariables gVars;
    TableCards tCards = new TableCards();
    List<Integer> nextPlayerInLine = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        /* Assigning a variable to each view */
        pAvatar[0] = findViewById(R.id.p2Avatar);
        pAvatar[1] = findViewById(R.id.p3Avatar);
        pAvatar[2] = findViewById(R.id.p4Avatar);

        pNicknameText[0] = findViewById(R.id.p2NicknameText);
        pNicknameText[1] = findViewById(R.id.p3NicknameText);
        pNicknameText[2] = findViewById(R.id.p4NicknameText);

        pMoneyText[0] = findViewById(R.id.p2MoneyText);
        pMoneyText[1] = findViewById(R.id.p3MoneyText);
        pMoneyText[2] = findViewById(R.id.p4MoneyText);

        raiseEditText = findViewById(R.id.raiseEditText);
        userAvatar = findViewById(R.id.userAvatar);
        userNicknameText = findViewById(R.id.userNicknameText);
        userMoneyText = findViewById(R.id.userMoneyText);
        checkButton = findViewById(R.id.checkButton);
        userCard1View = findViewById(R.id.userCard1);
        userCard2View = findViewById(R.id.userCard2);
        tableCard1View = findViewById(R.id.tableCard1);
        tableCard2View = findViewById(R.id.tableCard2);
        tableCard3View = findViewById(R.id.tableCard3);
        tableCard4View = findViewById(R.id.tableCard4);
        tableCard5View = findViewById(R.id.tableCard5);
        playerAvatarLayout[0] = findViewById(R.id.layout_player_1);
        playerAvatarLayout[1] = findViewById(R.id.layout_player_2);
        playerAvatarLayout[2] = findViewById(R.id.layout_player_3);
        foldButton = findViewById(R.id.foldButton);
        readyButton = findViewById(R.id.readyButton);
        raiseButton = findViewById(R.id.raiseButton);
        /* Recovering the extras */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = new PlayerVariables(extras.getString("nickname"), extras.getString("avatar"));
            userSpot = extras.getInt("playerSpot");
        }

        /* Setting up the UI */
        int resID = getResources().getIdentifier(user.getAvatar(), "drawable", "com.example.lepti.pokerapp");
        userAvatar.setImageResource(resID);
        userNicknameText.setText(user.getNickname());
        userMoneyText.setText(Integer.toString(user.getMoney()));
        foldButton.setVisibility(View.GONE);
        checkButton.setVisibility(View.GONE);
        readyButton.setVisibility(View.VISIBLE);
        raiseButton.setVisibility(View.GONE);
        playerAvatarLayout[0].setVisibility(View.GONE);
        playerAvatarLayout[1].setVisibility(View.GONE);
        playerAvatarLayout[2].setVisibility(View.GONE);

        hideAllButtons();
        /* Keep global variables up-to-date */
        DatabaseReference gameVariables = database.getReference("game-1/variables");
        ValueEventListener gameVariablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gVars = dataSnapshot.getValue(GameVariables.class);
                if(gVars.getCurrentRound() == -1) {
                    readyButton.setVisibility(View.VISIBLE);
                    hideAllButtons();
                    user.setCard1(-1);
                    user.setCard2(-1);

                    int ressID = getResources().getIdentifier("backside_old", "drawable", "com.example.lepti.pokerapp");
                    userCard1View.setImageResource(ressID);
                    userCard2View.setImageResource(ressID);
                    tableCard1View.setImageResource(ressID);
                    tableCard2View.setImageResource(ressID);
                    tableCard3View.setImageResource(ressID);
                    tableCard4View.setImageResource(0);
                    tableCard5View.setImageResource(0);
                    DatabaseReference db = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
                    db.setValue(user);
                }
                else if(gVars.getPlayerTurn() == userSpot+1) {
                    if((user.getLastRoundPlayed() == gVars.getCurrentRound() && gVars.getCurrentRound() != 0)) return;

                    user.setLastRoundPlayed(gVars.getCurrentRound());
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference update = db.getReference("game-1/player-variables/" + Integer.toString(userSpot));
                    update.setValue(user);


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
                            //switchPlayer();

                        }
                        else if(gVars.getCurrentRound() < 4) {
                            displayAllButtons();
                            if(gVars.getCurrentRound() == 0 && (user.getCard1() == -1 || user.getCard2() == -1)) {
                                generateCardsForPlayer();
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        };
        gameVariables.addValueEventListener(gameVariablesListener);

        /* Keep track of the cards on the table */
        DatabaseReference tableCardVariables = database.getReference("game-1/table-cards");
        tableCardVariables.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tCards = dataSnapshot.getValue(TableCards.class);
                if(tCards.getState() == 1) {
                    // state 1 update 3 cards
                    int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard1()), "drawable", "com.example.lepti.pokerapp");
                    tableCard1View.setImageResource(resID);

                    resID = getResources().getIdentifier(returnCardName(tCards.getTableCard2()), "drawable", "com.example.lepti.pokerapp");
                    tableCard2View.setImageResource(resID);

                    resID = getResources().getIdentifier(returnCardName(tCards.getTableCard3()), "drawable", "com.example.lepti.pokerapp");
                    tableCard3View.setImageResource(resID);
                }
                else if(tCards.getState() == 2) {
                    // state 2 update 1 card
                    int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard4()), "drawable", "com.example.lepti.pokerapp");
                    tableCard4View.setImageResource(resID);
                }
                else if(tCards.getState() == 3) {
                    // state 3 update 1 card
                    int resID = getResources().getIdentifier(returnCardName(tCards.getTableCard5()), "drawable", "com.example.lepti.pokerapp");
                    tableCard5View.setImageResource(resID);
                }
                else if(tCards.getState() == 5) { // time to get your winnings
                    if(user.getLastRoundPlayed() != 8) {
                        checkButton.setVisibility(View.GONE);
                        foldButton.setVisibility(View.GONE);
                        raiseButton.setVisibility(View.GONE);
                        user.setLastRoundPlayed(8);
                        boolean hasUserWon = false;
                        for(int i = 0; i < winningPlayers.size(); i++) {
                            if(winningPlayers.get(i) == userSpot) {
                                Toast.makeText(GamePage.this, "Congratulations! You won!", Toast.LENGTH_SHORT).show();
                                hasUserWon = true;
                                increaseUserMoney((int) Math.floorDiv(gVars.getTotalBet(), winningPlayers.size()));
                            }
                        }
                        if(winningPlayers.get(winningPlayers.size()-1) == userSpot) {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    resetGame();
                                }
                            }, 4000);
                        }
                        if(!hasUserWon) {
                            Toast.makeText(GamePage.this, "You lost!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

        /* Keep track of the players */
        DatabaseReference gameSpots = database.getReference("game-1/free-spots");
        gameSpots.addValueEventListener(new ValueEventListener() {
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

        DatabaseReference cardsRef = database.getReference("game-1/cards");

        /* Keep track of the cards removed from the deck */
        cardsRef.addValueEventListener(new ValueEventListener() {
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
        });

        DatabaseReference winnersRef = database.getReference("game-1/winner");

        /* Keep track of the winning players */
        winnersRef.addValueEventListener(new ValueEventListener() {
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
        });

        DatabaseReference pQueueRef = database.getReference("game-1/players-queue");
        /* Keep track of the players in queue */
        pQueueRef.addValueEventListener(new ValueEventListener() {
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
        });

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gVars.setReadyPlayers(gVars.getReadyPlayers()+1);
                gVars.setPlayersCompeting(gVars.getPlayersCompeting() + (int)(Math.pow(2, userSpot)));
                gVars.setCurrentRound(0);
                readyButton.setVisibility(View.GONE);
                if(gVars.getReadyPlayers() == gVars.getNumberPlayers() && gVars.getNumberPlayers() > 1) {
                    //gVars.setPlayerTurn((int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)) + 1); // The current player
                    //gVars.setCurrentlyCompeting( gVars.getPlayersCompeting() - (int)(Math.pow(2, (int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)))));
                    setupPlayerTurns();
                    gVars.setPlayerTurn(getNextInLine()+1);//);
                    gVars.setRoundBeginnerId(gVars.getPlayerTurn()-1);
                    gVars.setHasSomeonePlayed(false);
                    DatabaseReference ref = database.getReference("game-1/players-queue");
                    ref.setValue(nextPlayerInLine);
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
                //PokerPhase(phase);
                //phase++;
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gVars.getRoundBet() == 0) { // It's a check
                    checkButton.setVisibility(View.GONE);
                    foldButton.setVisibility(View.GONE);
                    raiseButton.setVisibility(View.GONE);
                    //Toast.makeText(GamePage.this, "You checked!", LENGTH_SHORT).show();
                    switchPlayer();
                }
                else // It's a call
                {
                    int money = user.getMoney();
                    if(money < gVars.getRoundBet()) {
                        user.setCurrentBet(user.getCurrentBet()+money);
                        user.setMoney(0);
                        gVars.setTotalBet(gVars.getTotalBet()+money);
                    }
                    else {
                        user.setCurrentBet(user.getCurrentBet()+gVars.getRoundBet());
                        user.setMoney(money-gVars.getRoundBet());
                        gVars.setTotalBet(gVars.getTotalBet()+gVars.getRoundBet());
                    }
                    checkButton.setVisibility(View.GONE);
                    foldButton.setVisibility(View.GONE);
                    raiseButton.setVisibility(View.GONE);
                    switchPlayer();
                    // update money text & database
                }

            }
        });

        raiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNumeric(raiseButton.getText().toString())) {
                    Toast.makeText(GamePage.this, "It's not a valid number !", Toast.LENGTH_SHORT).show();
                    return;
                }
                int raiseAmount = Integer.parseInt(raiseButton.getText().toString());
                if(raiseAmount < gVars.getRoundBet()) {
                    Toast.makeText(GamePage.this, "The minimum raise is $"+Integer.toString(gVars.getRoundBet()), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(raiseAmount < user.getMoney()) {
                    Toast.makeText(GamePage.this, "You don't have that much money!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(raiseAmount > gVars.getRoundBet()) {
                    user.setMoney(user.getMoney() - raiseAmount);
                    user.setCurrentBet(raiseAmount);
                    gVars.setRoundBet(raiseAmount);
                    gVars.setTotalBet(gVars.getTotalBet() + raiseAmount);
                    checkButton.setVisibility(View.GONE);
                    foldButton.setVisibility(View.GONE);
                    raiseButton.setVisibility(View.GONE);
                    addPlayerTurnsFromUserSpot(userSpot);
                }
                else
                {
                    user.setCurrentBet(user.getCurrentBet()+gVars.getRoundBet());
                    user.setMoney(user.getMoney()-gVars.getRoundBet());
                    gVars.setTotalBet(gVars.getTotalBet()+gVars.getRoundBet());
                }
                // update money text & database
                switchPlayer();

            }
        });
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
            if(isPlayerCompeting(((spot+i)%4)) && !nextPlayerInLine.contains(((spot+i)%4))) nextPlayerInLine.add(((spot+i)%4));
        }
    }

    private boolean isPlayerCompeting(int id) {
        int n = gVars.getPlayersCompeting();
        for(int i =0; i < (3-id); i++) {
            n = n >> 1;
        }

        if ((n & 1) == 0) {
            Log.d("COMPET:", "id " + Integer.toString(id) + " not competing");
            return true;
        }
        else {
            Log.d("COMPET:", "id " + Integer.toString(id) + " competing");
            return false;
        }
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
            checkButton.setText("Call");
        }
        else
        {
            checkButton.setText("Check");
        }
        checkButton.setVisibility(View.VISIBLE);
    }

    private void displayRaiseButton() {
        if(user.getMoney() > 0) {
            raiseButton.setVisibility(View.VISIBLE);
            raiseEditText.setVisibility(View.VISIBLE);
        }
    }

    private void displayFoldButton() {
        if(gVars.getRoundBet() > 0 && user.getMoney() > 0) {
            foldButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayAllButtons() {
        displayCheckButton();
        displayRaiseButton();
        displayFoldButton();
        return;
    }

    private void hideAllButtons() {
        raiseButton.setVisibility(View.GONE);
        raiseEditText.setVisibility(View.GONE);
        foldButton.setVisibility(View.GONE);
        checkButton.setVisibility(View.GONE);
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

        /* reset table-cards */
        updateGlobal = database.getReference("game-1/table-cards/");
        updateGlobal.setValue(new TableCards());

        /* reset cards used */
        updateGlobal = database.getReference("game-1/cards/");
        updateGlobal.removeValue();

        gVars.setCurrentRound(-1);
        gVars.setReadyPlayers(0);
        gVars.setHasSomeonePlayed(false);
        gVars.setPlayersCompeting(0);
        gVars.setPlayerTurn(0);

        updateGlobal = database.getReference("game-1/variables/");
        updateGlobal.setValue(gVars);
    }

    private void increaseUserMoney(int money) {
        user.setMoney(user.getMoney()+money);
        return;
    }
    private void triggerRoundStartEvent(int currentRound) {

        if(currentRound == 0 && (user.getCard1() == -1 || user.getCard2() == -1)) {
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
            tCards.setState(5);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference updateGlobal = database.getReference("game-1/table-cards/");
            updateGlobal.setValue(tCards);

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

    private void switchPlayer() {
        /* Hide the buttons */
        checkButton.setVisibility(View.GONE);
        foldButton.setVisibility(View.GONE);
        raiseButton.setVisibility(View.GONE);

        /* Database variables */
        FirebaseDatabase database = FirebaseDatabase.getInstance();

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

    private void showPlayerAvatar(int playerId) {
        final int layoutId = (playerId+(4-userSpot))%4;
        Log.d("IDD:", "Spot "+ Integer.toString(userSpot)+ " playerId=" +Integer.toString(playerId) + " layoutId=" + Integer.toString(layoutId) );
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference pReference = database.getReference("game-1/player-variables/"+Integer.toString(playerId));
        pReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PlayerVariables layoutPlayerVariables = dataSnapshot.getValue(PlayerVariables.class);

                /* Change the layout with the user's information*/
                pMoneyText[layoutId-1].setText(Integer.toString(layoutPlayerVariables.getMoney()));
                pNicknameText[layoutId-1].setText(layoutPlayerVariables.getNickname());
                int resID = getResources().getIdentifier(layoutPlayerVariables.getAvatar(), "drawable", "com.example.lepti.pokerapp");
                pAvatar[layoutId-1].setImageResource(resID);

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

        int resID = getResources().getIdentifier(returnCardName(user.getCard1()), "drawable", "com.example.lepti.pokerapp");
        userCard1View.setImageResource(resID);
        resID = getResources().getIdentifier(returnCardName(user.getCard2()), "drawable", "com.example.lepti.pokerapp");
        userCard2View.setImageResource(resID);
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
        playerAvatarLayout[layoutId-1].setVisibility(View.GONE);
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
        int firstRemoved = 0;

        /* We create a reverse set */
        Set<Integer> reverseCards = ((TreeSet<Integer>) allCards).descendingSet();
        Iterator<Integer> iteratorReverse;
        iteratorReverse = reverseCards.iterator();

        /* We iterate through the set */
        int index = 0;
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
            unsortedMap.put(key, numberOfSimilarCards); // insert in the map
            System.out.println("PAIRS: Rank " + Integer.toString(key) + " has " + Integer.toString(numberOfSimilarCards));
        }

        List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        int entryId = 0;

        for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
            entryId++;
            if(entry.getValue() == 4) {
                results = "Four of a Kind";

                allCards.remove(entry.getKey());

                /* We create a reverse set */
                Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
                Iterator<Integer> iterator;
                iterator = reverseAllCards.iterator();

                /* We iterate through the set */
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    userBestHand[0] = entry.getKey();
                    userBestHand[1] = entry.getKey();
                    userBestHand[2] = entry.getKey();
                    userBestHand[3] = entry.getKey();
                    userBestHand[4] = card;
                    break;
                }
                break;
            }
            if(entry.getValue() == 3) {
                results = "Three of a Kind";
                firstRemoved = entry.getKey();
                allCards.remove(firstRemoved);
            }
            if(( entryId == 2 ) && results.equals("Three of a Kind") && entry.getValue() == 2) {
                results = "Full House";
                allCards.remove(entry.getKey());
                userBestHand[0] = firstRemoved;
                userBestHand[1] = firstRemoved;
                userBestHand[2] = firstRemoved;
                userBestHand[3] = entry.getKey();
                userBestHand[4] = entry.getKey();
                break;
            }
            if(entry.getValue() == 2 && results.equals("One Pair")) {
                results = "Two Pairs";
                allCards.remove(entry.getKey());
                userBestHand[0] = firstRemoved;
                userBestHand[1] = firstRemoved;
                userBestHand[2] = entry.getKey();
                userBestHand[3] = entry.getKey();

                /* We create a reverse set */
                Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
                Iterator<Integer> iterator;
                iterator = reverseAllCards.iterator();

                /* We iterate through the set */
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    userBestHand[4] = card;
                    break;
                }

                break;
            }
            else if(entry.getValue() == 2) {
                results = "One Pair";
                firstRemoved = entry.getKey();
                allCards.remove(firstRemoved);
            }
        }

        if(results.equals("Three of a Kind")) {
            /* We create a reverse set */
            Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
            Iterator<Integer> iterator;
            iterator = reverseAllCards.iterator();

            /* We assign the first three cards */
            userBestHand[0] = firstRemoved;
            userBestHand[1] = firstRemoved;
            userBestHand[2] = firstRemoved;

            /* We iterate through the set */
            int i = 3;
            while (iterator.hasNext() && i < 5) {
                int card = iterator.next();
                userBestHand[i] = card;
                i++;
            }
        }
        else if(results.equals("One Pair")) {
            /* We create a reverse set */
            Set<Integer> reverseAllCards = ((TreeSet<Integer>) allCards).descendingSet();
            Iterator<Integer> iterator;
            iterator = reverseAllCards.iterator();

            /* We assign the first three cards */
            userBestHand[0] = firstRemoved;
            userBestHand[1] = firstRemoved;

            /* We iterate through the set */
            int i = 2;
            while (iterator.hasNext() && i < 5) {
                int card = iterator.next();
                userBestHand[i] = card;
                i++;
            }
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
            userBestHand[0] = highestStraightFlush;
            userBestHand[1] = highestStraightFlush-1;
            userBestHand[2] = highestStraightFlush-2;
            userBestHand[3] = highestStraightFlush-3;
            userBestHand[4] = highestStraightFlush-4;
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
                userBestHand[1] = highestStraight-1;
                userBestHand[2] = highestStraight-2;
                userBestHand[3] = highestStraight-3;
                userBestHand[4] = highestStraight-4;
            }
            results = "Straight";
        }
        return results;
    }
}
