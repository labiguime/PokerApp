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
import classes.TableCards;

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
    RelativeLayout[] playerAvatarLayout = new RelativeLayout[3];
    Button checkButton;
    Button raiseButton;


    int playerTurn = 0;
    int currId = 2;
    int[] userDeckOfCards = new int[7];
    Button foldButton;
    int phase = 1;
    int totalPlayers = 1;
    ArrayList<Integer> cardsInUse  = new ArrayList<Integer>();

    List<Integer> cardsUsed  = new ArrayList<>();

    PlayerVariables user;
    GameVariables gVars;
    TableCards tCards = new TableCards();

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
        foldButton.setVisibility(View.GONE);
        checkButton.setVisibility(View.GONE);
        readyButton.setVisibility(View.VISIBLE);
        raiseButton.setVisibility(View.GONE);
        playerAvatarLayout[0].setVisibility(View.GONE);
        playerAvatarLayout[1].setVisibility(View.GONE);
        playerAvatarLayout[2].setVisibility(View.GONE);

        /* Keep global variables up-to-date */
        DatabaseReference gameVariables = database.getReference("game-1/variables");
        ValueEventListener gameVariablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gVars = dataSnapshot.getValue(GameVariables.class);
                if(gVars.getPlayerTurn() == userSpot+1) {
                    if(gVars.getCurrentRound() == 4) {
                        checkButton.setVisibility(View.GONE);
                        foldButton.setVisibility(View.GONE);
                        raiseButton.setVisibility(View.GONE);

                        String hand = getUserHand();
                        user.setBestHandName(hand);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference updateGlobal = database.getReference("game-1/player-variables/" + Integer.toString(userSpot));
                        updateGlobal.setValue(user);


                        //switchPlayer();
                    }
                    else {
                        if (user.getCard1() == -1) {
                            generateCardsForPlayer();
                            /* No animation yet so we're gonna make it sleep */
                        }
                        checkButton.setVisibility(View.VISIBLE);
                        foldButton.setVisibility(View.VISIBLE);
                        raiseButton.setVisibility(View.VISIBLE);
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

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gVars.setReadyPlayers(gVars.getReadyPlayers()+1);
                gVars.setPlayersCompeting(gVars.getPlayersCompeting() + (int)(Math.pow(2, userSpot)));
                readyButton.setVisibility(View.GONE);
                if(gVars.getReadyPlayers() == gVars.getNumberPlayers() && gVars.getNumberPlayers() > 1) {
                    gVars.setPlayerTurn((int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)) + 1); // The current player
                    gVars.setCurrentlyCompeting( gVars.getPlayersCompeting() - (int)(Math.pow(2, (int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)))));
                }
                DatabaseReference globalVariablesRef = database.getReference("game-1/variables");
                globalVariablesRef.setValue(gVars);
            }
        });

        PokerPhase(0);

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
                checkButton.setVisibility(View.GONE);
                foldButton.setVisibility(View.GONE);
                raiseButton.setVisibility(View.GONE);
                Toast.makeText(GamePage.this, "You checked!", LENGTH_SHORT).show();
                switchPlayer();
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
    private void beginNewRound() {
        if(gVars.getCurrentRound() < 3) {
            generateTableCards(gVars.getCurrentRound()+1);
        }
        else if(gVars.getCurrentRound() == 3) {
            String hand = getUserHand();
            user.setBestHandName(hand);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference updateGlobal = database.getReference("game-1/player-variables/"+Integer.toString(userSpot));
            updateGlobal.setValue(user);


            updateGlobal = database.getReference("game-1/variables");
            gVars.setCurrentRound(gVars.getCurrentRound()+1);
            gVars.setPlayerTurn((int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)) + 1); // The current player
            gVars.setCurrentlyCompeting( gVars.getPlayersCompeting() - (int)(Math.pow(2, (int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)))));


            updateGlobal.setValue(gVars);

            checkButton.setVisibility(View.GONE);
            foldButton.setVisibility(View.GONE);
            raiseButton.setVisibility(View.GONE);

            return;
        }
        gVars.setCurrentRound(gVars.getCurrentRound()+1);
        gVars.setPlayerTurn((int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)) + 1); // The current player
        gVars.setCurrentlyCompeting( gVars.getPlayersCompeting() - (int)(Math.pow(2, (int)((Math.log( gVars.getPlayersCompeting() & -gVars.getPlayersCompeting() ))/Math.log(2)))));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference updateGlobal = database.getReference("game-1/variables");
        updateGlobal.setValue(gVars);
    }
    private void switchPlayer() {
        checkButton.setVisibility(View.GONE);
        foldButton.setVisibility(View.GONE);
        raiseButton.setVisibility(View.GONE);
        if(gVars.getCurrentlyCompeting() == 0) {
            beginNewRound();
        }
        else {

            gVars.setPlayerTurn((int)((Math.log( gVars.getCurrentlyCompeting() & -gVars.getCurrentlyCompeting() ))/Math.log(2)) + 1); // The current player
            gVars.setCurrentlyCompeting( gVars.getCurrentlyCompeting() - (int) (Math.pow(2, gVars.getPlayerTurn()-1)));
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference updateGlobal = database.getReference("game-1/variables");
            updateGlobal.setValue(gVars);
        }
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
        userDeckOfCards[currId] = number;
        return (rank+suit);
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
        else if(rank > 0 && rank < 9) {
            cardRank = "c" + Integer.toString((rank+1));
        }
        else if(rank == 10) {
            cardRank = "jack";
        }
        else if(rank == 10) {
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
        userDeckOfCards[cardid] = number;
        return (rank+suit);
    }
    public String getUserHand() {
        userDeckOfCards[0] = user.getCard1();
        userDeckOfCards[1] = user.getCard2();
        userDeckOfCards[2] = tCards.getTableCard1();
        userDeckOfCards[3] = tCards.getTableCard2();
        userDeckOfCards[4] = tCards.getTableCard3();
        userDeckOfCards[5] = tCards.getTableCard4();
        userDeckOfCards[6] = tCards.getTableCard5();
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
        Set<Integer> flushSet = new HashSet<Integer>();
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
        return results;
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
                Set<Integer> flushSet = new HashSet<Integer>();
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
