package com.tdp.coolp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.text.style.AlignmentSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.TypedValue;
import android.widget.Toast;
import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.graphics.Point;
import android.app.AlertDialog.Builder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.media.MediaPlayer;
import android.content.res.Configuration;


public class MainActivity extends Activity {
    ImageView iv;
    ImageView iv2;
    Board board;
    Competitor player;
    Competitor comp;
    Deck deck;
    private static Context context;
    
    MediaPlayer callSound;
    MediaPlayer foldSound;
    MediaPlayer dealSound;
    
    private ImageView imageCard1;
    private static final String KEY_CARD_VISIBLE = "Visible1";
    private static final String KEY_CARD_HEIGHT = "height";
    
    HandEvaluator handEvaluator = new HandEvaluator();
    ComputerAI computerAI = new ComputerAI();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();
        
        String fbName = "";
        String fbID = "";
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fbName = extras.getString("fbName");
            fbID = extras.getString("fbID");
        }
        Log.d("fuck", "oncreate");
        deck = new Deck();
        board = new Board(); //start roundNum at 0 not 1
        Log.d("fuck", "oncreate2");
        player = new Competitor(1000, "Player", R.id.playerMessage, 0);
        
        player.fbName = fbName;
        player.fbID = fbID;
        
        comp = new Competitor(1000, "Computer", R.id.compMessage, 1);
        
        callSound = MediaPlayer.create(context, R.raw.poker_chip_call);
        foldSound = MediaPlayer.create(context, R.raw.fold_cards);
        dealSound = MediaPlayer.create(context, R.raw.deal_cards);
        comp.chips = 45;
        
        startHand();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        //        Completely handle screen orientation
        int roundNum = board.roundNum;
        
        String cText = (String) ((TextView) findViewById(R.id.compMessage)).getText();
        String pText = (String) ((TextView) findViewById(R.id.playerMessage)).getText();
        
        int playerButtonVisibility  = findViewById(R.id.playerButton).getVisibility();
        int compButtonVisibility  = findViewById(R.id.compButton).getVisibility();
        
        int checkVisibility = findViewById(R.id.checkFold).getVisibility();
        int callVisibility = findViewById(R.id.call).getVisibility();
        int raiseVisibility = findViewById(R.id.raise).getVisibility();
        
        String checkText = (String) ((Button) findViewById(R.id.checkFold)).getText();
        String callText = (String) ((Button) findViewById(R.id.call)).getText();
        String raiseText = (String) ((Button) findViewById(R.id.raise)).getText();
        
        setContentView(R.layout.activity_main);
        board.hideBoardCards();
        
        ((TextView) findViewById(R.id.compChips)).setText("Chips: " + comp.chips);
        ((TextView) findViewById(R.id.compBet)).setText("Bet: " + comp.bet);
        
        ((TextView) findViewById(R.id.playerChips)).setText("Chips: " + player.chips);
        ((TextView) findViewById(R.id.playerBet)).setText("Bet: " + player.bet);
        
        ((TextView) findViewById(R.id.hand_num)).setText("Hand: " + board.handNum);
        ((TextView) findViewById(R.id.pot)).setText("Pot: " + board.pot);
        
        ((TextView) findViewById(R.id.compMessage)).setText(cText);
        ((TextView) findViewById(R.id.playerMessage)).setText(pText);
        
        findViewById(R.id.playerButton).setVisibility(playerButtonVisibility);
        findViewById(R.id.compButton).setVisibility(compButtonVisibility);
        
        player.cardObject[0].showDrawnCard();
        player.cardObject[1].showDrawnCard();
        
        if (board.hand_over) {
            comp.cardObject[0].showDrawnCard();
            comp.cardObject[1].showDrawnCard();
        }
        
        if (roundNum >= 1){
            board.showFlop(false, false);
        }
        if (roundNum >= 2){
            board.showTurn(false, false);
        }
        if (roundNum >= 3){
            board.showRiver(false, false);
        }
        
        ((Button) findViewById(R.id.checkFold)).setText(checkText);
        ((Button) findViewById(R.id.call)).setText(callText);
        ((Button) findViewById(R.id.raise)).setText(raiseText);
        
        findViewById(R.id.checkFold).setVisibility(checkVisibility);
        findViewById(R.id.call).setVisibility(callVisibility);
        findViewById(R.id.raise).setVisibility(raiseVisibility);
    }
    
    
    //    @Override
    //    protected void onSaveInstanceState (Bundle outState) {
    //        super.onSaveInstanceState(outState);
    //        Toast.makeText(this, "Hello there", Toast.LENGTH_LONG).show();
    //        outState.putInt(KEY_CARD_VISIBLE, imageCard1.getVisibility());
    //        outState.putInt(KEY_CARD_HEIGHT, imageCard1.getHeight());
    //        Log.d("fuck you fucker", "save???????");
    //    }
    
    public static Context getContext() {
        return MainActivity.context;
    }
    
    public class Deck {
        List<Integer> allCards = new ArrayList<Integer>();
        public Deck(){
            
            for(int i = 0; i < 52; i++){
                this.allCards.add(i);
            }
            Collections.shuffle(this.allCards);
        }
        
        public void print(){
            for(int i = 0; i < 52; i++){
                System.out.println(this.allCards.get(i));
            }
        }
        
        public int[] returnArray(){
            int[] myArray = new int[52];
            for(int i = 0; i < 52; i++){
                myArray[i] = this.allCards.get(i);
            }
            return myArray;
        }
        
        public void reShuffle(){
            Collections.shuffle(this.allCards);
        }
    }
    
    public class Card {
        int cardNum;
        int rankNum;
        int id;
        String rankString;
        String suit;
        String unicodeRep;
        
        public Card(int cardNum, int cardID){
            this.cardNum = cardNum;
            this.rankNum = this.getRankNum();
            this.rankString = this.getRankString();
            this.suit = this.getSuit();
            this.unicodeRep = this.getUnicodeRep();
            this.id = cardID;
        }
        
        public Card(){
            this.cardNum = 0;
            this.rankNum = 0;
            this.rankString = "";
            this.suit = "";
            this.unicodeRep = "";
            this.id=0;
        }
        
        private int getRankNum(){
            return (int) Math.floor(this.cardNum % 13);
        }
        
        private String getRankString(){
            Map<Integer, String> rankString = new HashMap<Integer, String>();
            
            rankString.put(0, "A");
            rankString.put(10, "J");
            rankString.put(11, "Q");
            rankString.put(12, "K");
            
            if (this.rankNum <= 0 || this.rankNum >= 10) {
                return rankString.get(this.rankNum);
            }
            if(this.rankNum == 9) {
                return "10";
            }
            return "" + (this.rankNum + 1);
            
        }
        
        private String getSuit(){
            Map<Integer, String> suit = new HashMap<Integer, String>();
            int suitNum;
            
            suit.put(0, "c");
            suit.put(1, "s");
            suit.put(2, "h");
            suit.put(3, "d");
            
            suitNum = (int) Math.floor(this.cardNum/13);
            return suit.get(suitNum);
        }
        
        private String getUnicodeRep(){
            Map<String, String> suitUnicode = new HashMap<String, String>();
            
            suitUnicode.put("c", "\u2663");
            suitUnicode.put("s", "\u2660");
            suitUnicode.put("h", "\u2665");
            suitUnicode.put("d", "\u2666");
            
            if (this.rankString == "10"){
                return " " + this.rankString + "\n" + suitUnicode.get(this.suit);
            } else {
                return " " + this.rankString + " \n" + suitUnicode.get(this.suit);
            }
        }
        
        private void showDrawnCard(){
            TextView textView = (TextView) findViewById(this.id);
            textView.setText(this.unicodeRep);
            
            Spannable span = new SpannableString(textView.getText());
            span.setSpan(new RelativeSizeSpan(1.5f), 4, span.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (this.suit == "d" || this.suit == "h") {
                //                span.setSpan(new TextAppearanceSpan(getContext(), R.style.drawnCardRed), 3, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setTextColor(Color.RED);
            }else{
                //                span.setSpan(new TextAppearanceSpan(getContext(), R.style.drawnCardBlack), 3, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setTextColor(Color.BLACK);
            }
            span.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 4, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            textView.setText(span, TextView.BufferType.SPANNABLE);
        }
        
        public void displayCard(boolean animate, boolean waitTillFinish){
            // use to have int cardNum, as paramr
            //		iv = (ImageView) findViewById(id);
            //		Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.drawable.allcards);
            //
            //		int h = bmp.getHeight() / 4;
            //		int w = bmp.getWidth() / 13;
            //
            //		Bitmap resizedbitmap1=Bitmap.createBitmap(bmp,(int) Math.floor(cardNum % 13) * w,(int) Math.floor(cardNum/13) * h,w, h);
            //		iv.setImageBitmap(resizedbitmap1);
            TextView tv = (TextView) findViewById(this.id);
            if(animate){
                
                Animation anim;
                tv.setVisibility(View.VISIBLE);
                anim = AnimationUtils.makeInAnimation(MainActivity.this, true);
                if(waitTillFinish) {
                    anim.setAnimationListener(new AnimationListener() {
                        
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //wait until animation is finished to continue turn
                            if (board.turn == 1) compTurn(); else playerTurn();
                        }
                    });
                }
                tv.startAnimation(anim);
            } else{
                tv.setVisibility(View.VISIBLE);
            }
            this.showDrawnCard();
        }
    }
    
    public class Competitor {
        int[] cards;
        Card[] cardObject = new Card[2];
        int chips;
        int bet;
        int startChips; //chips that comp started the hand with
        int totalBet; //total bet this hand
        int hasGone;
        int[] numRaises;
        int[] percentile;
        int preflopScore;
        double[] gameProbs = new double[2];
        double score;
        int bluff;
        boolean drawingHand;
        boolean allIn;
        String winMessage;
        String fbName = "";
        String fbID = "";
        String name;
        int messageID;
        boolean overs;
        int id;
        
        public Competitor(int chips, String name, int messageID, int id){
            
            this.chips = chips;
            //			this.bet = 0;
            this.startChips = 1000;
            //			this.totalBet = 0;
            //			this.hasGone = 0;
            //			this.numRaises = new int[4];
            //			this.percentile = new int[4];
            //			this.score = 0;
            //			this.bluff = 0;
            //			this.winMessage = "";
            //			this.preflopScore = 0;
            //			this.gameProbs = 0.0;
            //			this.drawingHand = false;
            //            this.allIn = false;
            this.name = name;
            this.messageID = messageID;
            //            this.overs = false;
            this.id = id;
        }
        
        public void getCurrentHandPercentile(){
            // This calculates the percentile rank for the hand given the current board
            // The percentile is the percentage of two card hands worse than starting hand
            
            int[] currentBoard = Arrays.copyOfRange(board.cards, 0, board.roundNum + 2);
            int[] cardsEvaluated = concat(currentBoard, this.cards ); //current hand as of the current board reading
            HandEvaluator currentHand = new HandEvaluator();
            currentHand.handValue(cardsEvaluated);
            HandEvaluator tempHand = new HandEvaluator();
            int[] tempCards;
            int winners = 0;
            int totalTrials = 0;
            
            //change to how many hands beat it
            
            for(int i = 0; i < 52; i++) {
                if(Arrays.asList(cardsEvaluated).indexOf(i) != -1) {continue;}
                for(int j = i + 1; j < 52; j++){
                    if(Arrays.asList(cardsEvaluated).indexOf(j) != -1) {continue;}
                    tempCards = concat(currentBoard, new int[] {i, j});
                    tempHand.handValue(tempCards);
                    if(currentHand.score > tempHand.score) { winners++; }
                    else if (currentHand.score == tempHand.score) { winners++; }
                    totalTrials++;
                }
            }
            
            //Log.w("percentile", "percentile is " + winners /totalTrials + " float" + winners /(float) totalTrials);
            this.percentile[board.roundNum] = (int) (Math.round(winners / (float) totalTrials  * 100));
        }
        
        public void hasOvers() {
            
            int[] currentBoardCards = HandEvaluator.changeAce(HandEvaluator.getRank(Arrays.copyOfRange(board.cards,0, board.roundNum + 2)));
            int[] compCards = HandEvaluator.changeAce(HandEvaluator.getRank(this.cards));
            Arrays.sort(currentBoardCards);
            Arrays.sort(compCards);
            int maxBoard = currentBoardCards[currentBoardCards.length - 1];
            
            if(compCards[0] > maxBoard && compCards[1] > maxBoard && compCards[0] >= 9) {
                this.overs = true;
            }
            else {
                this.overs = false;
            }
        }
        
        public void newHand(){
            
            this.setCards();
            this.bet = 0;
            this.totalBet = 0;
            this.hasGone = 0;
            this.numRaises = new int[4];
            this.percentile = new int[4];
            this.score = 0;
            this.bluff = (int) Math.round(Math.random());
            this.winMessage = "";
            this.preflopScore = computerAI.computePreflopScore(this.cards);
            this.gameProbs = computerAI.computePreflopProbs(this.preflopScore, board);
            this.drawingHand = false;
            this.allIn = false;
            this.overs = false;
            this.startChips = this.chips;
        }
        
        private void setCards(){
            if (this.name == "Player"){
                this.cards = Arrays.copyOfRange(deck.returnArray(), 0, 2);
                this.cardObject[0] = new Card(this.cards[0], R.id.player1);
                this.cardObject[1] = new Card(this.cards[1], R.id.player2);
            } else {
                this.cards = Arrays.copyOfRange(deck.returnArray(), 7, 9);
                this.cardObject[0] = new Card(this.cards[0], R.id.comp1);
                this.cardObject[1] = new Card(this.cards[1], R.id.comp2);
            }
        }
        
    }
    
    public class Board {
        
        int roundNum;
        int betRound;
        int[] cards;
        Card[] cardObject = new Card[5];
        int smallBet;
        int bigBet;
        int betSize; //current round's bet
        int turn; //players turn will be 0 and comps turn will be 1
        int dealer;
        int pot;
        int handNum;
        int[] lastRaise;
        int[] roundBets;
        int stacked;
        boolean hand_over;
        int[] cardIDs;
        
        
        public Board(){
            
            this.roundNum = 0;  // the round number will start at 0
            this.cardIDs = new int[]{R.id.board1, R.id.board2, R.id.board3, R.id.board4, R.id.board5};
            this.smallBet = 10;
            this.bigBet = 20;
            this.turn = 0;
            this.betRound = 1;
            this.pot = 0;
            this.dealer = 1;
            this.betSize = this.smallBet;
            this.handNum = 0;
            this.lastRaise = new int[] {-1, -1, -1, -1};
            this.roundBets = new int[4];
            this.stacked = 0;
            this.hand_over = false;
        }
        
        public void deal(){
            
            dealSound.start();
            switch (this.roundNum){
                case 1: showFlop(true, true); break;
                case 2: showTurn(true, true); break;
                case 3: showRiver(true, true); break;
            }
        }
        
        private void showFlop(boolean animate, boolean waitTillFinish){
            this.cardObject[0].displayCard(animate, false);
            this.cardObject[1].displayCard(animate, false);
            this.cardObject[2].displayCard(animate, waitTillFinish);
        }
        
        private void showTurn(boolean animate, boolean waitTillFinish){
            this.cardObject[3].displayCard(animate, waitTillFinish);
        }
        
        private void showRiver(boolean animate, boolean waitTillFinish){
            this.cardObject[4].displayCard(animate, waitTillFinish);
        }
        
        private void newHand(){
            this.cards = Arrays.copyOfRange(deck.returnArray(), 2, 7);
            this.initCardObject();
            this.dealer = 1 - this.dealer;
            this.roundNum = 0;
            this.betRound = 1;
            this.handNum += 1;
            this.pot = 0;
            Log.d("fucker", "board new hand");
            this.betSize = this.smallBet;
            this.stacked = 0;
            this.turn = this.dealer;
            Log.d("fucker", "board new hand2");
            this.lastRaise = new int[] {-1, -1, -1, -1};
            this.hand_over = false;
            
            Button callButton = (Button) findViewById(R.id.call);
            callButton.setText("Call");
            Log.d("fucker", "board new hand3");
            this.hideBoardCards();
            moveButton();
        }
        
        private void initCardObject(){
            for(int i = 0; i < 5;  i++){
                this.cardObject[i] = new Card(this.cards[i], this.cardIDs[i]);
            }
        }
        
        public void hideBoardCards(){
            
            for(int i = 0; i < 5; i++){
                findViewById(this.cardIDs[i]).setVisibility(View.INVISIBLE);
            }
        }
    }
    
    
    public void hideCompCard(){
        
        ImageView c1 = (ImageView) findViewById(R.id.comp1);
        ImageView c2 = (ImageView) findViewById(R.id.comp2);
        c1.setImageResource(R.drawable.card_back);
        c2.setImageResource(R.drawable.card_back);
    }
    
    public void startHand(){
        Log.d("fucker", "start hand");
        deck.reShuffle();
        player.newHand();
        Log.d("fucker", "player new hand");
        comp.newHand();
        Log.d("fucker", "comp new hand");
        board.newHand();
        Log.d("fucker", "all new hand");
        
        player.cardObject[0].displayCard(false, false);
        player.cardObject[1].displayCard(false, false);
        
        dealSound.start();
        
        clearMessages();
        updatePot(); //update this before new blinds are bet
        updateHandNum();
        updateBlinds();
        updatePlayerChips(-player.bet);
        updateCompChips(-comp.bet);
        updatePlayerBet();
        updateCompBet();
        
        //if player or comp has only a small blind left on the button
        if((player.chips + player.bet) == board.smallBet / 2) {toggleButtons(1); allIn(player, comp, ""); return; }
        if((comp.chips + comp.bet) == board.smallBet / 2) {toggleButtons(1); allIn(comp, player, ""); return; }
        
        if(board.dealer == 0) { playerTurn(); }
        else { compTurn(); }
    }
    
    public void clearMessages(){
        TextView pm = (TextView) findViewById(R.id.playerMessage);
        TextView cm = (TextView) findViewById(R.id.compMessage);
        pm.setText("");
        cm.setText("");
    }
    
    public void playerTurn(){
        if(player.allIn){
            //            Toast.makeText(getApplicationContext(), "all in from here", Toast.LENGTH_LONG).show();
            allIn(player, comp, "");
            return;
        }
        if ((comp.allIn && comp.bet <= player.bet) || (comp.chips == 0 && comp.bet <= player.bet)){
            
            allIn(comp, player,"");
            return;
        }
        toggleButtons(0);
        changeButtons();
    }
    
    public void compTurn(){
        toggleButtons(1);
        if(comp.allIn){
            allIn(comp, player, "");
            return;
        }
        if (player.allIn && comp.bet == player.bet){
            allIn(player, comp,"");
            return;
        }
        
        //any other allin the right decision is going to be to call
        if (player.allIn){
            //            allIn(player, comp,"calls ");
            compCall();
            return;
        }
        comp.hasOvers();
        double randNum = Math.random();
        comp.gameProbs = computerAI.probAdjustment(comp, player, board);
        if(player.bet > comp.bet && comp.chips <= board.betSize){
            compCall();
        }
        else if(comp.bet == player.bet) {
            if(comp.chips == 0) { allIn(comp, player, ""); }
            else if(randNum < comp.gameProbs[0]){ compCheckFold(); }
            else  { compRaise(); }
        } else if(board.betRound == 4) {
            compCall();
        } else{
            if(randNum < comp.gameProbs[0]){ compCheckFold(); }
            else if(randNum < comp.gameProbs[1])  { compCall(); }
            else { compRaise(); }
        }
    }
    
    public void fold() {
        foldSound.start();
        TextView pm = (TextView) findViewById(R.id.playerMessage);
        pm.setText("Computer wins pot");
        updateCompChips(comp.bet + player.bet + board.pot);
        insertIntoDB();
        showNextHand();
    }
    
    public void checkFold(View v){
        toggleButtons(1);
        if(player.bet < comp.bet) { fold(); return; }
        TextView pm = (TextView) findViewById(R.id.playerMessage);
        //		pm.setText("Player Checks");
        player.hasGone = 1;
        determineTurn();
    }
    
    public void call(View v){
        toggleButtons(1);
        Button callButton = (Button) findViewById(R.id.call);
        if (callButton.getText().toString() == "Next Hand"){
            callButton.setVisibility(View.INVISIBLE);
            startHand();
        }
        else {
            callSound.start();
            if( (comp.bet - player.bet) >= player.chips){ //allIN
                allIn(player, comp, "calls ");
                return;
            }
            else {
                TextView pm = (TextView) findViewById(R.id.playerMessage);
                updatePlayerChips(-(comp.bet - player.bet));
                player.bet = comp.bet;
                updatePlayerBet();
                //		pm.setText("Player Calls to " + comp.bet);
                player.hasGone = 1;
                determineTurn();
            }
        }
    }
    
    public void raise(View v){
        callSound.start();
        toggleButtons(1);
        TextView pm = (TextView) findViewById(R.id.playerMessage);
        if( player.chips - (comp.bet - player.bet + board.betSize) <= 0){ //allIN
            allIn(player, comp, "raises to ");
            return;
        }
        updatePlayerChips(-1 * (comp.bet + Math.min(comp.chips, board.betSize) - player.bet));
        player.bet = comp.bet + Math.min(comp.chips, board.betSize);
        player.numRaises[board.roundNum] += 1;
        board.lastRaise[board.roundNum] = 0;
        updatePlayerBet();
        //		if(comp.bet == 0){ pm.setText("Player Bets " + player.bet); }
        //		else { pm.setText("Player Raises to " + player.bet); }
        player.hasGone = 1;
        comp.hasGone = 0;
        board.betRound += 1;
        determineTurn();
    }
    
    public void compFold() {
        foldSound.start();
        TextView pm = (TextView) findViewById(R.id.playerMessage);
        pm.setText("Player wins pot");
        String message = "Computer Folds";
        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
        final TextView tv = (TextView) findViewById(R.id.compMessage);
        tv.setText(message);
        a.setAnimationListener(new AnimationListener(){
            
            @Override
            public void onAnimationStart(Animation animation){}
            
            @Override
            public void onAnimationRepeat(Animation animation){}
            
            @Override
            public void onAnimationEnd(Animation animation){
                updatePlayerChips(comp.bet + player.bet + board.pot);
                comp.hasGone = 1;
                showNextHand();
            }
        });
        tv.startAnimation(a);
        insertIntoDB();
    }
    
    public void compCheckFold(){
        String message = "Computer Checks";
        if(comp.bet < player.bet) { compFold(); return; }
        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
        final TextView tv = (TextView) findViewById(R.id.compMessage);
        tv.setText(message);
        a.setAnimationListener(new AnimationListener(){
            
            @Override
            public void onAnimationStart(Animation animation){}
            
            @Override
            public void onAnimationRepeat(Animation animation){}
            
            @Override
            public void onAnimationEnd(Animation animation){
                comp.hasGone = 1;
                determineTurn();
            }
        });
        tv.startAnimation(a);
    }
    
    public void compCall(){
        //        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.poker_chip_call);
        callSound.start();
        if( (player.bet - comp.bet) >= comp.chips){ //allIN
            allIn(comp, player, "calls ");
            return;
        }
        updateCompChips(-1 * (player.bet - comp.bet));
        comp.bet = player.bet;
        updateCompBet();
        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
        final TextView tv = (TextView) findViewById(R.id.compMessage);
        tv.setText("Computer Calls to " + player.bet);
        a.setAnimationListener(new AnimationListener(){
            
            @Override
            public void onAnimationStart(Animation animation){}
            
            @Override
            public void onAnimationRepeat(Animation animation){}
            
            @Override
            public void onAnimationEnd(Animation animation){
                comp.hasGone = 1;
                determineTurn();
            }
        });
        tv.startAnimation(a);
    }
    
    public void compRaise(){
        callSound.start();
        String message;
        if( comp.chips - (player.bet - comp.bet + board.betSize) <= 0){ //allIN
            allIn(comp, player, "raises to ");
            return;
        }
        updateCompChips(-1 * (player.bet + Math.min(player.chips, board.betSize) - comp.bet));
        comp.bet = player.bet + Math.min(player.chips, board.betSize);
        comp.numRaises[board.roundNum] += 1;
        board.lastRaise[board.roundNum] = 1;
        
        updateCompBet();
        //        toggleButtons(1);
        if(player.bet == 0){
            message = "Computer Bets " + comp.bet;
        }
        else {
            message = "Computer Raises to " + comp.bet;
        }
        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
        final TextView tv = (TextView) findViewById(R.id.compMessage);
        tv.setText(message);
        a.setAnimationListener(new AnimationListener(){
            
            @Override
            public void onAnimationStart(Animation animation){}
            
            @Override
            public void onAnimationRepeat(Animation animation){}
            
            @Override
            public void onAnimationEnd(Animation animation){
                comp.hasGone = 1;
                player.hasGone = 0;
                board.betRound += 1;
                determineTurn();
            }
        });
        tv.startAnimation(a);
    }
    
    public void allIn(final Competitor allInner, Competitor bigStack, String message){
        allInner.allIn = true;
        allInner.bet = allInner.bet + allInner.chips;
        if (allInner.chips == 0){
            message = "";
        }
        
        if(message == "calls ") {
            bigStack.bet = allInner.bet;
        } else if (message == "raises to "){
            board.betRound += 1;
            bigStack.hasGone = 0;
            if (bigStack.bet == 0) message = "bets ";
            allInner.numRaises[board.roundNum] += 1;
            board.lastRaise[board.roundNum] = allInner.id;
        }
        
        if(allInner.bet == 0 || message == "") {
            toggleButtons(1);
            message = allInner.name + " is all in";
            bigStack.hasGone = 1;
            bigStack.bet = allInner.bet;
        } else {
            message = allInner.name + " " + message + allInner.bet + " and is all in";
        }
        allInner.hasGone = 1;
        
        if (allInner.name == "Computer") updateCompChips(-1 * comp.chips); else updatePlayerChips(-1 * player.chips);
        
        
        
        final TextView tv = (TextView) findViewById(allInner.messageID);
        tv.setText(message);
        if (allInner.name == "Computer") {
            Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
            a.setAnimationListener(new AnimationListener() {
                
                @Override
                public void onAnimationStart(Animation animation) {
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    determineTurn();
                }
            });
            tv.startAnimation(a);
        } else {
            determineTurn();
        }
        
    }
    
    public void updatePlayerBet(){
        TextView pBet = (TextView) findViewById(R.id.playerBet);
        pBet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        pBet.setText("Bet: " + player.bet);
    }
    
    public void updatePlayerChips(int amount){
        TextView pChips = (TextView) findViewById(R.id.playerChips);
        player.chips += amount;
        pChips.setText("Chips: " + player.chips);
    }
    
    public void updateCompBet(){
        TextView cBet = (TextView) findViewById(R.id.compBet);
        cBet.setText("Bet: " + comp.bet);
    }
    
    public void updateCompChips(int amount){
        TextView cChips = (TextView) findViewById(R.id.compChips);
        comp.chips += amount;
        cChips.setText("Chips: " + comp.chips);
    }
    
    public void updateBlinds(){
        if(board.dealer == 0){
            player.bet = board.smallBet / 2;
            comp.bet = Math.min(comp.chips, board.smallBet);
        } else {
            comp.bet = board.smallBet / 2;
            player.bet = Math.min(player.chips, board.smallBet);
        }
    }
    
    public void updatePot(){
        board.pot  += comp.bet + player.bet;
        TextView tpot = (TextView) findViewById(R.id.pot);
        tpot.setText("Pot: " + board.pot);
    }
    
    public void moveButton(){
        ImageView playerButton  = (ImageView) findViewById(R.id.playerButton);
        ImageView compButton  = (ImageView) findViewById(R.id.compButton);
        
        if(board.dealer == 0){ //player turn
            compButton.setVisibility(View.INVISIBLE);
            playerButton.setVisibility(View.VISIBLE);
        }
        else {
            compButton.setVisibility(View.VISIBLE);
            playerButton.setVisibility(View.INVISIBLE);
        }
    }
    
    public void toggleButtons(int currentTurn){
        View checkButton  = (View) findViewById(R.id.checkFold);
        View callButton  = (View) findViewById(R.id.call);
        View raiseButton  = (View) findViewById(R.id.raise);
        
        if(currentTurn == 1){ //comps turn
            checkButton.setVisibility(View.INVISIBLE);
            callButton.setVisibility(View.INVISIBLE);
            raiseButton.setVisibility(View.INVISIBLE);
        } else {
            checkButton.setVisibility(View.VISIBLE);
            callButton.setVisibility(View.VISIBLE);
            raiseButton.setVisibility(View.VISIBLE);
        }
    }
    
    
    
    public void changeButtons(){
        Button checkButton  = (Button) findViewById(R.id.checkFold);
        Button callButton  = (Button) findViewById(R.id.call);
        Button raiseButton  = (Button) findViewById(R.id.raise);
        if(player.bet == 0 && comp.bet == 0){
            checkButton.setText("Check");
            callButton.setVisibility(View.INVISIBLE);
            raiseButton.setText("Bet " + Math.min(board.betSize, player.chips));
        }
        else if(player.bet == comp.bet){
            checkButton.setText("Check");
            callButton.setVisibility(View.INVISIBLE);
            raiseButton.setText("Raise to " + Math.min((comp.bet + board.betSize), player.bet + player.chips));
        } else {
            checkButton.setText("Fold");
            callButton.setText("Call to " + Math.min(comp.bet, player.bet + player.chips));
            raiseButton.setText("Raise to " + Math.min((comp.bet + board.betSize), player.bet + player.chips));
            if(board.betRound == 4 || comp.allIn == true || (player.bet + player.chips <= comp.bet)) { raiseButton.setVisibility(View.INVISIBLE); }
            
        }
        if(comp.chips == 0){
            raiseButton.setVisibility(View.INVISIBLE);
        }
    }
    
    public void getFutureValue() {
        int[] boardCards = Arrays.copyOfRange(board.cards, 0,  board.roundNum + 2);
        int[] compCards = comp.cards;
        int[] allCards = concat(boardCards, compCards);
        boolean isFlush = false;
        boolean isStraight = false;
        int[] suits= HandEvaluator.getSuit(allCards);
        int[] cardRanks = HandEvaluator.getRank(compCards);
        int scard = 0;
        int numStraights = 0;
        boolean check1;
        //boolean check2;
        int tempSuitCount;
        int[] possStraightCards;
        //don't look for straight draws on 4 flush boards
        
        //check if there are 4 or more of same suit
        for(int i = 0; i < 4; i++){
            tempSuitCount = 0;
            for(int j = 0; j < suits.length; j++){
                if(suits[j] == i){
                    tempSuitCount++;
                }
            }
            if (tempSuitCount == 4){
                isFlush = true;
            }
        }
        
        //only check here if not flush or not 3 flush on board
        int[] boardSuits = HandEvaluator.getSuit(boardCards);
        Log.d("num suits", "num suits " + boardSuits.length);
        if (! ((boardSuits[0] == boardSuits[1]) && (boardSuits[1] == boardSuits[2]))){
            for(int i = 0; i < 52; i++) {
                check1 = false;
                //check2 = false;
                if(Arrays.asList(allCards).indexOf(i) == -1){  //check if card is part of the board.
                    possStraightCards = concat(allCards, new int[] {i});
                }
                else {
                    continue;
                }
                
                possStraightCards = HandEvaluator.getUniqueCards(HandEvaluator.getRank(possStraightCards));
                if (possStraightCards.length < 4) {continue;}
                Arrays.sort(possStraightCards);
                scard = HandEvaluator.rowStraight(possStraightCards);
                
                if(scard == 0) { //not a straight
                    possStraightCards = HandEvaluator.changeAce(possStraightCards);
                    Arrays.sort(possStraightCards);
                    scard = HandEvaluator.rowStraight(possStraightCards);
                    if(scard == 0){
                        continue;
                    }
                    else { //straight
                        scard += 4;
                    }
                }
                else { //straight
                    scard += 4;
                }
                
                //if(Math.abs(scard - HandEvaluator.changeAceValue(cardRanks[0]))  <= 4 && Math.abs(scard - HandEvaluator.changeAceValue(cardRanks[1]))  <= 4)
                //	{  check1 = true; }
                
                if( Math.abs(scard - HandEvaluator.changeAceValue(cardRanks[0]))  <= 1 ||
                   Math.abs(scard - HandEvaluator.changeAceValue(cardRanks[1]))  <= 1 ||
                   (Math.abs(scard - HandEvaluator.changeAceValue(cardRanks[0]))  <= 4 && Math.abs(scard - HandEvaluator.changeAceValue(cardRanks[1]))  <= 4))
                { check1 = true; }
                
                if(check1) { numStraights++;}
            }
        }
        
        if(numStraights >= 8) { isStraight = true; }
        //	if(isStraight) { alert("straight draw"); }
        if(isFlush || isStraight) { comp.drawingHand = true; }
    }
    
    public void determineTurn(){
        
        // Determines who's turn it is and moves on to next round if both players have acted
        
        if(comp.hasGone == 1 && player.hasGone == 1){
            //move on to next round
            board.roundBets[board.roundNum] = Math.min(player.bet, comp.bet);
            board.roundNum += 1;
            if(board.roundNum == 1) { initiateBluff(); }
            if(board.roundNum == 2) { board.betSize = board.betSize * 2; }
            //			Toast.makeText(getApplicationContext(), "bluff " + comp.bluff, Toast.LENGTH_SHORT).show();
            
            //reset the round
            updatePot();
            if(board.roundNum > 3) { evaluateHand(); return; }	//end of hand
            comp.bet = 0;
            player.bet = 0;
            board.betRound = 0;
            board.turn = 1 - board.dealer;
            comp.hasGone = 0;
            player.hasGone = 0;
            comp.getCurrentHandPercentile();
            player.getCurrentHandPercentile();
            comp.gameProbs = computerAI.initializeCompProbs(comp, player, board);
            comp.bluff = computerAI.bluff;
            updateCompBet();
            updatePlayerBet();
            clearMessages();
            if(board.roundNum < 3)  getFutureValue();
            board.deal();
        }
        else { //round still continues
            board.turn = 1 - board.turn;
            if (board.turn == 1) compTurn(); else playerTurn();
        }
        
    }
    
    public void evaluateHand(){
        String title, message;
        TextView pm = (TextView) findViewById(R.id.playerMessage);
        toggleButtons(1);
        
        comp.cardObject[0].displayCard(false, false);
        comp.cardObject[1].displayCard(false, false);
        
        player.bet = 0;
        comp.bet = 0;
        updatePlayerBet();
        updateCompBet();
        
        handEvaluator.handValue(concat(player.cards, board.cards));
        player.score = handEvaluator.score;
        player.winMessage = handEvaluator.winMessage;
        
        handEvaluator.handValue(concat(comp.cards, board.cards));
        comp.score = handEvaluator.score;
        comp.winMessage = handEvaluator.winMessage;
        
        showNextHand();
        if(player.score > comp.score){
            pm.setText("Player wins with " + player.winMessage);
            updatePlayerChips(board.pot);
        }
        else if(comp.score > player.score){
            pm.setText("Comp wins with " + comp.winMessage);
            updateCompChips(board.pot);
        }
        else{
            pm.setText("Players tie with " + comp.winMessage);
            updateCompChips(board.pot / 2);
            updatePlayerChips(board.pot / 2);
        }
        
        if(player.chips == 0){
            board.stacked = -1;
            title = "The computer has defeated you!!";
            message = "Press OK to rebuy to 1000.";
            insertIntoDB();
            player.chips = 1000;
            showWinnerPopUp(title, message);
        } else if (comp.chips == 0) {
            board.stacked = 1;
            title = "Congratulations, You Win!!";
            message = "The computer will rebuy to 1000.";
            insertIntoDB();
            comp.chips = 1000;
            showWinnerPopUp(title, message);
        }
        else{
            insertIntoDB();
        }
    }
    
    public void showWinnerPopUp(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
        .setCancelable(false)
        .setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do things
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    
    public void showNextHand(){
        board.hand_over = true;
        Button callButton = (Button) findViewById(R.id.call);
        callButton.setVisibility(View.VISIBLE);
        callButton.setText("Next Hand");
    }
    
    public void updateHandNum(){
        TextView hn = (TextView) findViewById(R.id.hand_num);
        hn.setText("Hand: " + board.handNum);
    }
    
    //concatenate two arrays
    public int[] concat(int[] A, int[] B) {
        int aLen = A.length;
        int bLen = B.length;
        int[] C= new int[aLen+bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }
    
    
    
    public void initiateBluff(){
        if (Math.random() > .5){
            comp.bluff = 1;
        }
    }
    
    public void insertIntoDB(){
        
        String showdown = "0";
        if (comp.bet == player.bet){
            showdown = "1";
            board.roundNum = 3; //the roundNum increases to 4 at showdown so needs to be decreased here
        }
        board.roundBets[board.roundNum] = Math.min(player.bet, comp.bet);
        DB_Task db_task = new DB_Task();
        String email = getAccountName();
        
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        
        InternalDatabaseHH dbHelper = new InternalDatabaseHH(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //        dbHelper.onUpgrade(db, 2, 3);
        //        db.execSQL(InternalDatabaseHH.SQL_DELETE_ENTRIES);
        //        db.execSQL(InternalDatabaseHH.TABLE_CREATE);
        
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Guid", randomUUIDString);
        values.put("Score", Integer.toString(player.startChips - player.chips));
        values.put("C1", cardToString(comp.cards[0]));
        values.put("C2", cardToString(comp.cards[1]));
        values.put("B1",cardToString(board.cards[0]));
        values.put("B2",cardToString(board.cards[1]));
        values.put("B3",cardToString(board.cards[2]));
        values.put("B4",cardToString(board.cards[3]));
        values.put("B5",cardToString(board.cards[4]));
        values.put("P1", cardToString(player.cards[0]));
        values.put("P2",cardToString(player.cards[1]));
        values.put("Button",Integer.toString(board.dealer));
        values.put("Percentile",Integer.toString(comp.percentile[board.roundNum]));
        values.put("ShowDown",showdown);
        values.put("BigBet",Integer.toString(board.bigBet));
        values.put("CompRaise_PF",Integer.toString(comp.numRaises[0]));
        values.put("PlayerRaise_PF",Integer.toString(player.numRaises[0]));
        values.put("Last_Raise_PF",Integer.toString(board.lastRaise[0]));
        values.put("Bet_PF",Integer.toString(board.roundBets[0]));
        values.put("CompRaise_Flop",Integer.toString(comp.numRaises[1]));
        values.put("PlayerRaise_Flop",Integer.toString(player.numRaises[1]));
        values.put("Last_Raise_Flop",Integer.toString(board.lastRaise[1]));
        values.put("Bet_Flop",Integer.toString(board.roundBets[1]));
        values.put("CompRaise_Turn",Integer.toString(comp.numRaises[2]));
        values.put("PlayerRaise_Turn",Integer.toString(player.numRaises[2]));
        values.put("Last_Raise_Turn",Integer.toString(board.lastRaise[2]));
        values.put("Bet_Turn",Integer.toString(board.roundBets[2]));
        values.put("CompRaise_River",Integer.toString(comp.numRaises[3]));
        values.put("PlayerRaise_River",Integer.toString(player.numRaises[3]));
        values.put("Last_Raise_River",Integer.toString(board.lastRaise[3]));
        values.put("Bet_River",Integer.toString(board.roundBets[3]));
        values.put("GameHandNum",Integer.toString(board.handNum));
        values.put("Comp_PF_Value",Integer.toString(comp.preflopScore));
        values.put("Player_PF_Value",Integer.toString(player.preflopScore));
        values.put("Comp_Flop_Perc",Integer.toString(comp.percentile[1]));
        values.put("Player_Flop_Perc",Integer.toString(player.percentile[1]));
        values.put("Comp_Turn_Perc",Integer.toString(comp.percentile[2]));
        values.put("Player_Turn_Perc",Integer.toString(player.percentile[2]));
        values.put("Comp_River_Perc",Integer.toString(comp.percentile[3]));
        values.put("Player_River_Perc",Integer.toString(player.percentile[3]));
        values.put("Round_Completed",Integer.toString(board.roundNum + 1));
        values.put("Stacked", board.stacked);
        values.put("FB_Name",player.fbName);
        values.put("FB_ID",player.fbID);
        values.put("Start_Chips",Integer.toString(player.startChips));
        values.put("Email", email);
        
        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                             InternalDatabaseHH.TABLE_NAME,
                             "null",
                             values);
        
        db_task.execute(randomUUIDString, Integer.toString(player.startChips - player.chips), cardToString(comp.cards[0]), cardToString(comp.cards[1]),
                        cardToString(board.cards[0]), cardToString(board.cards[1]), cardToString(board.cards[2]),
                        cardToString(board.cards[3]),cardToString(board.cards[4]), cardToString(player.cards[0]),
                        cardToString(player.cards[1]), Integer.toString(board.dealer), Integer.toString(comp.percentile[board.roundNum]),
                        showdown, Integer.toString(board.bigBet), Integer.toString(comp.numRaises[0]), Integer.toString(player.numRaises[0]),
                        Integer.toString(board.lastRaise[0]), Integer.toString(board.roundBets[0]),
                        Integer.toString(comp.numRaises[1]), Integer.toString(player.numRaises[1]),
                        Integer.toString(board.lastRaise[1]), Integer.toString(board.roundBets[1]),
                        Integer.toString(comp.numRaises[2]), Integer.toString(player.numRaises[2]),
                        Integer.toString(board.lastRaise[2]), Integer.toString(board.roundBets[2]),
                        Integer.toString(comp.numRaises[3]), Integer.toString(player.numRaises[3]),
                        Integer.toString(board.lastRaise[3]), Integer.toString(board.roundBets[3]),
                        Integer.toString(board.handNum), Integer.toString(comp.preflopScore), Integer.toString(player.preflopScore),
                        Integer.toString(comp.percentile[1]), Integer.toString(player.percentile[1]),
                        Integer.toString(comp.percentile[2]), Integer.toString(player.percentile[2]),
                        Integer.toString(comp.percentile[3]), Integer.toString(player.percentile[3]),
                        Integer.toString(board.roundNum + 1), Integer.toString(board.stacked), player.fbName, player.fbID,
                        Integer.toString(player.startChips), email);
        
        Cursor cursor =db.rawQuery("Select max(HandNum) from hh_gto", null);
        if (cursor != null)
            cursor.moveToFirst();
        //        Toast.makeText(getApplicationContext(), "Max score is " + cursor.getString(0), Toast.LENGTH_SHORT).show();
        
    }
    
    public String getAccountName(){
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String email = "";
        
        Log.d("length of accounts", "" + list.length);
        
        for(Account account: list)
        {
            if (account.name.contains("@")) {
                email = account.name;
                break;
            }
        }
        return email;
    }
    
    public String cardToString(int cdNum) {
        int rankNum = cdNum % 13;
        int suitNum = (int) (cdNum / 13);
        String rankString = "";
        String suitString = "";
        
        switch(rankNum) {
            case 9:
                rankString = "T";
                break;
            case 10:
                rankString = "J";
                break;
            case 11:
                rankString = "Q";
                break;
            case 12:
                rankString = "K";
                break;
            case 0:
                rankString = "A";
                break;
            default:
                rankString = "" + (rankNum + 1);
        }
        switch(suitNum){
            case 0:
                suitString = "c";
                break;
            case 1:
                suitString = "s";
                break;
            case 2:
                suitString = "h";
                break;
            case 3:
                suitString = "d";
                break;
        }
        
        return(rankString + suitString);
    }
    
}
