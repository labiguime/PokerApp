package com.example.lepti.pokerapp;


import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static android.widget.Toast.LENGTH_SHORT;

import classes.GameVariables;
import classes.PlayerVariables;

public class GamePage extends AppCompatActivity {

    int userSpot;
    ImageView userAvatar;
    Button userNicknameText;
    Button readyButton;

    ImageView[] pAvatar = new ImageView[3];
    Button[] pNicknameText = new Button[3];
    Button[] pMoneyText = new Button[3];

    ImageView userCard1View;
    ImageView userCard2View;
    ImageView tableCard1View;
    ImageView tableCard2View;
    ImageView tableCard3View;
    ImageView tableCard4View;
    ImageView tableCard5View;
    RelativeLayout[] playerAvatarLayout = new RelativeLayout[4];
    Button checkButton;


    int playerTurn = 0;
    int currId = 2;
    int[][] playerCards = new int[4][7];
    Button foldButton;
    int phase = 1;
    int totalBet = 0;
    int individualBet = 0;
    int totalPlayers = 1;
    ArrayList<Integer> cardsInUse  = new ArrayList<Integer>();

    int readyPlayers = 0;
    int numberPlayers = 0;

    PlayerVariables user;
    GameVariables gVars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

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

        userAvatar = findViewById(R.id.userAvatar);
        userNicknameText = findViewById(R.id.userNicknameText);
        checkButton = findViewById(R.id.checkButton);
        userCard1View = findViewById(R.id.userCard1);
        userCard2View = findViewById(R.id.userCard2);
        tableCard1View = findViewById(R.id.tableCard1);
        tableCard2View = findViewById(R.id.tableCard2);
        tableCard3View = findViewById(R.id.tableCard3);
        tableCard4View = findViewById(R.id.tableCard4);
        tableCard5View = findViewById(R.id.tableCard5);
        playerAvatarLayout[0] = findViewById(R.id.layout_player_0);
        playerAvatarLayout[1] = findViewById(R.id.layout_player_1);
        playerAvatarLayout[2] = findViewById(R.id.layout_player_2);
        playerAvatarLayout[3] = findViewById(R.id.layout_player_3);
        foldButton = findViewById(R.id.foldButton);
        readyButton = findViewById(R.id.readyButton);

        /* Recovering the extras */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = new PlayerVariables(extras.getString("nickname"), extras.getString("avatar"));
            userSpot = extras.getInt("playerSpot");
            DatabaseReference playerVariables = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
            playerVariables.setValue(user);
        }

        /* Setting up the UI */
        int resID = getResources().getIdentifier(user.getAvatar(), "drawable", "com.example.lepti.pokerapp");
        userAvatar.setImageResource(resID);
        userNicknameText.setText(user.getNickname());
        foldButton.setVisibility(View.GONE);
        checkButton.setVisibility(View.GONE);
        readyButton.setVisibility(View.VISIBLE);
        playerAvatarLayout[0].setVisibility(View.VISIBLE);
        playerAvatarLayout[1].setVisibility(View.GONE);
        playerAvatarLayout[2].setVisibility(View.GONE);
        playerAvatarLayout[3].setVisibility(View.GONE);


        /* Keep global variables up-to-date */
        DatabaseReference gameVariables = database.getReference("game-1/variables");
        ValueEventListener gameVariablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gVars = dataSnapshot.getValue(GameVariables.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        };
        gameVariables.addValueEventListener(gameVariablesListener);

        /* Keep track of the players */
        DatabaseReference gameSpots = database.getReference("game-1/free-spots");
        gameSpots.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(!snapshot.getValue(Boolean.class))
                        hidePlayerAvatar(Integer.parseInt(snapshot.getKey()));
                    else
                        showPlayerAvatar(Integer.parseInt(snapshot.getKey()));
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
                readyButton.setVisibility(View.GONE);
                if(gVars.getReadyPlayers() == gVars.getNumberPlayers() && gVars.getNumberPlayers() > 1) {
                    // we can start the game
                    // remove value event listener for players
                }
            }
        });

        PokerPhase(0);

        foldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PokerPhase(phase);
                phase++;
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(playerTurn == 0) {
                    if(individualBet != 0) {
                        Toast.makeText(GamePage.this, "You cannot check because there is a bet on the table.", LENGTH_SHORT).show();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(GamePage.this);
                    builder.setMessage("Do you really want to check?").setTitle("Action");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(GamePage.this, "You checked!", LENGTH_SHORT).show();
                            // call next turn
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }*/

            }
        });
    }
    private void nextTurn() {
        if(playerTurn == totalPlayers-1) {
            // Call next phase
        }
        else {
            playerTurn++;
            // send message to the player in question
        }
        return;
    }
    private String generateCard(int forced1, int forced2)
    {
        String suit;
        String rank;
        Integer number = 0;
        do {
            int min = 0;
            int max = 3;
            int random = new Random().nextInt((max - min) + 1) + min;
            if(forced1 != -1) random=forced1;

            if(random == 0) {
                suit = "clubs";
            }
            else if(random == 1) {
                suit = "diamonds";
            }
            else if(random == 2) {
                suit = "spades";
            }
            else {
                suit = "hearts";
            }
            number = random*13;

            min = 0;
            max = 12;
            random = new Random().nextInt((max - min) + 1) + min;
            if(forced2 != -1) random=forced2;
            if(random == 0) {
                rank = "ace_of_";
            }
            else if(random == 1) {
                rank = "c2_of_";
            }
            else if(random == 2) {
                rank = "c3_of_";
            }
            else if(random == 3) {
                rank = "c4_of_";
            }
            else if(random == 4) {
                rank = "c5_of_";
            }
            else if(random == 5) {
                rank = "c6_of_";
            }
            else if(random == 6) {
                rank = "c7_of_";
            }
            else if(random == 7) {
                rank = "c8_of_";
            }
            else if(random == 8) {
                rank = "c9_of_";
            }
            else if(random == 9) {
                rank = "c10_of_";
            }
            else if(random == 10) {
                rank = "jack_of_";
            }
            else if(random == 11) {
                rank = "queen_of_";
            }
            else {
                rank = "king_of_";
            }
            number = number+random;
        }
        while (cardsInUse.contains(number));
        cardsInUse.add(number);
        playerCards[0][currId] = number;
        return (rank+suit);
    }

    private void showPlayerAvatar(int playerId) {
        final int layoutId = (playerId+userSpot)%4;
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
                playerAvatarLayout[layoutId].setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void hidePlayerAvatar(int playerId) {
        int layoutId = (playerId+userSpot)%4;
        playerAvatarLayout[layoutId].setVisibility(View.GONE);
    }
    private String generatePlayerCards(int playerid, int cardid, int forced1, int forced2)
    {
        String suit;
        String rank;
        Integer number = 0;
        do {
            int min = 0;
            int max = 3;
            int random = new Random().nextInt((max - min) + 1) + min;
            if(forced1 != -1) random=forced1;

            if(random == 0) {
                suit = "clubs";
            }
            else if(random == 1) {
                suit = "diamonds";
            }
            else if(random == 2) {
                suit = "spades";
            }
            else {
                suit = "hearts";
            }
            number = random*13;

            min = 0;
            max = 12;
            random = new Random().nextInt((max - min) + 1) + min;
            if(forced2 != -1) random=forced2;
            if(random == 0) {
                rank = "ace_of_";
            }
            else if(random == 1) {
                rank = "c2_of_";
            }
            else if(random == 2) {
                rank = "c3_of_";
            }
            else if(random == 3) {
                rank = "c4_of_";
            }
            else if(random == 4) {
                rank = "c5_of_";
            }
            else if(random == 5) {
                rank = "c6_of_";
            }
            else if(random == 6) {
                rank = "c7_of_";
            }
            else if(random == 7) {
                rank = "c8_of_";
            }
            else if(random == 8) {
                rank = "c9_of_";
            }
            else if(random == 9) {
                rank = "c10_of_";
            }
            else if(random == 10) {
                rank = "jack_of_";
            }
            else if(random == 11) {
                rank = "queen_of_";
            }
            else {
                rank = "king_of_";
            }
            number = number+random;
        }
        while (cardsInUse.contains(number));
        cardsInUse.add(number);
        playerCards[playerid][cardid] = number;
        return (rank+suit);
    }
    private int PokerPhase(int phase) {
        switch (phase) {
            case 0:
            {
                int resID = getResources().getIdentifier("backside_old", "drawable", "com.example.lepti.pokerapp");
                userCard1View.setImageResource(resID);
                userCard2View.setImageResource(resID);
                tableCard1View.setImageResource(resID);
                tableCard2View.setImageResource(resID);
                tableCard3View.setImageResource(resID);
                tableCard4View.setImageResource(0);
                tableCard5View.setImageResource(0);
                break;
            }
            case 1:
            {
                String Card1 = generateCard(-1, -1);
                currId = 3;
                String Card2 = generateCard(-1, -1);
                currId = 4;
                String Card3 = generateCard(-1, -1);
                int resID = getResources().getIdentifier(Card1, "drawable", "com.example.lepti.pokerapp");
                tableCard1View.setImageResource(resID);
                resID = getResources().getIdentifier(Card2, "drawable", "com.example.lepti.pokerapp");
                tableCard2View.setImageResource(resID);
                resID = getResources().getIdentifier(Card3, "drawable", "com.example.lepti.pokerapp");
                tableCard3View.setImageResource(resID);
                break;
            }
            case 2:
            {

                String Card1 = generatePlayerCards(0, 0, -1, -1);
                String Card2 = generatePlayerCards(0, 1, -1, -1);
                int resID = getResources().getIdentifier(Card1, "drawable", "com.example.lepti.pokerapp");
                userCard1View.setImageResource(resID);
                resID = getResources().getIdentifier(Card2, "drawable", "com.example.lepti.pokerapp");
                userCard2View.setImageResource(resID);
                break;
            }
            case 3:
            {
                currId = 5;
                String Card1 = generateCard(-1, -1);
                int resID = getResources().getIdentifier(Card1, "drawable", "com.example.lepti.pokerapp");
                tableCard4View.setImageResource(resID);
                break;
            }
            case 4:
            {
                currId = 6;
                String Card1 = generateCard(-1, -1);
                int resID = getResources().getIdentifier(Card1, "drawable", "com.example.lepti.pokerapp");
                tableCard5View.setImageResource(resID);
                break;
            }
            case 5:
            {
                ArrayList<Integer> pairs = new ArrayList<Integer>();
                String results = "No Pair";
                for(int i = 0; i < 6; i++) {
                    for(int y = i+1; y < 7; y++ ) {
                        if((playerCards[0][i]%13) == (playerCards[0][y]%13)) {
                            pairs.add((playerCards[0][i]%13));
                            break;
                        }
                    }
                }
                int highestStraightFlush = -1;
                int highestStraight = -1;
                for(int i = 0; i < 7; i++) {
                    int middleCardRank = (playerCards[0][i])%13;
                    if( middleCardRank > 1 && middleCardRank < 12) {
                        boolean[] straightFound = new boolean[4];
                        straightFound[0] = false;
                        straightFound[1] = false;
                        straightFound[2] = false;
                        straightFound[3] = false;
                        boolean isFlush = true;
                        int flushSuit = Math.floorDiv(playerCards[0][i], 13);
                        for(int u = 0; u < 7; u++) {
                            if(u == i) continue;
                            if( ( (middleCardRank+1) %13 ) == ( (playerCards[0][u]) %13 ) && straightFound[2] == false) {
                                straightFound[2] = true;
                                if(flushSuit != Math.floorDiv(playerCards[0][u], 13) && isFlush) {
                                    boolean doesSameSuitExist = false;
                                    for(int y = u+1; y < 7; y++) {
                                        if(y == i) continue;
                                        if(( (playerCards[0][u]) %13 ) == ( (playerCards[0][y]) %13 ) && flushSuit == Math.floorDiv(playerCards[0][y], 13)) {
                                            doesSameSuitExist = true;
                                            break;
                                        }
                                    }
                                    isFlush = doesSameSuitExist;
                                }
                            }
                            if( ( (middleCardRank+2) %13 ) == ( (playerCards[0][u]) %13 )  && straightFound[3] == false) {
                                straightFound[3] = true;
                                if(flushSuit != Math.floorDiv(playerCards[0][u], 13) && isFlush) {
                                    boolean doesSameSuitExist = false;
                                    for(int y = u+1; y < 7; y++) {
                                        if(y == i) continue;
                                        if(( (playerCards[0][u]) %13 ) == ( (playerCards[0][y]) %13 ) && flushSuit == Math.floorDiv(playerCards[0][y], 13)) {
                                            doesSameSuitExist = true;
                                            break;
                                        }
                                    }
                                    isFlush = doesSameSuitExist;
                                }
                            }
                            if( ( (middleCardRank-1) %13 ) == ( (playerCards[0][u]) %13 )  && straightFound[1] == false) {
                                straightFound[1] = true;
                                if(flushSuit != Math.floorDiv(playerCards[0][u], 13) && isFlush) {
                                    boolean doesSameSuitExist = false;
                                    for(int y = u+1; y < 7; y++) {
                                        if(y == i) continue;
                                        if(( (playerCards[0][u]) %13 ) == ( (playerCards[0][y]) %13 ) && flushSuit == Math.floorDiv(playerCards[0][y], 13)) {
                                            doesSameSuitExist = true;
                                            break;
                                        }
                                    }
                                    isFlush = doesSameSuitExist;
                                }
                            }
                            if( ( (middleCardRank-2) %13 ) == ( (playerCards[0][u]) %13 )  && straightFound[0] == false) {
                                straightFound[0] = true;
                                if(flushSuit != Math.floorDiv(playerCards[0][u], 13) && isFlush) {
                                    boolean doesSameSuitExist = false;
                                    for(int y = u+1; y < 7; y++) {
                                        if(y == i) continue;
                                        if(( (playerCards[0][u]) %13 ) == ( (playerCards[0][y]) %13 ) && flushSuit == Math.floorDiv(playerCards[0][y], 13)) {
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
                Set<Integer> flushSet = new HashSet<Integer>();
                for(int u = 0; u < 3; u++) {
                    for(int i = 0; i < 7; i++) {
                        int cardRank = (playerCards[0][i])%13;
                        if(Math.floorDiv(playerCards[0][i], 13) == u) {
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


                Set<Integer> unique = new HashSet<Integer>(pairs);
                Map<Integer, Integer> unsortedMap = new HashMap<Integer, Integer>();
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
                        break;
                    }
                    if(entry.getValue() == 3) results = "Three of a Kind";
                    if(( entryId == 2 || entryId == 3) && results.equals("Three of a Kind") && entry.getValue() == 2) {
                        results = "Full House";
                        break;
                    }
                    if(entry.getValue() == 2 && results.equals("One Pair")) {
                        results = "Two Pairs";
                        System.out.println("PAIRS: GET CALLED");
                        break;
                    }
                    else if(entry.getValue() == 2) {
                        results = "One Pair";
                    }
                }
                System.out.println("PAIRS: Called " + Integer.toString(entryId));
                if(highestStraightFlush == 0) {
                    results = "Royal Flush";
                }
                else if(highestStraightFlush != -1) {
                    results = "Straight Flush";
                }
                else if(!results.equals("Four of a Kind") && !results.equals("Full House") && !flushSet.isEmpty()) {
                    results = "Flush";
                }
                else if(!results.equals("Four of a Kind") && !results.equals("Full House") && highestStraight != -1) {
                    results = "Straight";
                }
                Toast.makeText(getApplicationContext(),results,LENGTH_SHORT).show();
            }
        }
        return 1;
    }
}
