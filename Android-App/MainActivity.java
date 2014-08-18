package com.tdp.coolp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	ImageView iv; 
	ImageView iv2;
	Board board;
	Player player;
	Comp comp;
	Deck deck;
	HandEvaluator handEvaluator = new HandEvaluator();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);				
	
		deck = new Deck();
		deck.print();
		
		board = new Board(1, Arrays.copyOfRange(deck.returnArray(), 2, 7));
		
		player = new Player(Arrays.copyOfRange(deck.returnArray(), 0, 2), 1000);
		player.displayCards();
		
		comp = new Comp(Arrays.copyOfRange(deck.returnArray(), 7, 9), 1000);
		//comp.displayCards();
		
		//board = new Board(1, Arrays.copyOfRange(deck.returnArray(), 2, 7));
		
		//startHand();
		updateBlinds();
		updatePlayerChips(-player.bet);
		updateCompChips(-comp.bet);	
		updatePlayerBet();
		updateCompBet();
		changeButtons();
		updateHandNum();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	
	public class Player {
		int[] cards = new int[2];
		int chips;
		int bet;
		int startChips;
		int totalBet; //total bet this hand
		int hasGone;
		double score;
		String winMessage;
		
		public Player(int[] cards, int chips){
			this.chips = chips;
			this.cards[0] = cards[0];
			this.cards[1] = cards[1];
			this.bet=0;
			this.startChips = 1000;
			this.totalBet = 0;
			this.hasGone = 0;
			this.score = 0;
			this.winMessage = "";
		}
		
		
		public void displayCards(){
			displayCard(R.id.player1, cards[0], true);
			displayCard(R.id.player2, cards[1], true);			
		}
	}
	
	public class Comp {
		int[] cards = new int[2];
		int chips;
		int bet;
		int startChips; //chips that comp started the hand with
		int totalBet; //total bet this hand
		int hasGone;
		int preflopScore;
		double[] gameProbs = new double[2];
		double score;
		String winMessage;
		
		public Comp(int[] cards, int chips){
			this.chips = chips;
			this.cards[0] = cards[0];
			this.cards[1] = cards[1];
			this.bet = 0;
			this.startChips = 1000;
			this.totalBet = 0;
			this.hasGone = 0;
			this.score = 0;
			this.winMessage = "";
			this.preflopScore = computePreflopScore(this.cards);
			this.gameProbs = computePreflopProbs(this.preflopScore);
			Toast.makeText(getApplicationContext(), "Preflop score is " + this.preflopScore, Toast.LENGTH_SHORT).show();
		}
		
		
		public void displayCards(){
			displayCard(R.id.comp1, this.cards[0], false);
			displayCard(R.id.comp2, this.cards[1], false);			
		}
		
	}
	
	public class Board {
		
		int roundNum;
		int betRound;
		int[] cards;
		int smallBet;
		int bigBet;
		int betSize; //current round's bet
		int turn; //players turn will be 0 and comps turn will be 1
		int dealer;
		int pot;	
		int handNum;
		
		
		public Board(int roundNum, int[] cards){
			this.roundNum = roundNum;
			this.cards = cards;
			this.smallBet = 10;
			this.bigBet = 20;
			this.turn = 0;	
			this.betRound = 1;
			this.pot = 0;
			this.dealer = 0;
			this.betSize = this.smallBet;
			this.handNum = 1;
			
		}
		
		public void increaseRound(){
			this.roundNum += 1;
		}
		
		public void deal(){
			if(this.roundNum == 2){
				showFlop();
			}
			else if(this.roundNum == 3){
				showTurn();
			}
			else {
				showRiver();
			}
		}
		
		private void showFlop(){
			displayCard(R.id.board1, this.cards[0], true);			
			displayCard(R.id.board2, this.cards[1], true);
			displayCard(R.id.board3, this.cards[2], true);
			
		}
		
		private void showTurn(){
			displayCard(R.id.board4, this.cards[3], true);
		}

		private void showRiver(){
			displayCard(R.id.board5, this.cards[4], true);
		}
		
		private void newHand(){
			this.dealer = 1 - this.dealer;
			this.roundNum = 1;
			this.betRound = 1;
			this.handNum += 1;
			this.pot = 0;
			this.betSize = this.smallBet;
			
			ImageView b1 = (ImageView) findViewById(R.id.board1);
			ImageView b2 = (ImageView) findViewById(R.id.board2);
			ImageView b3 = (ImageView) findViewById(R.id.board3);
			ImageView b4 = (ImageView) findViewById(R.id.board4);
			ImageView b5 = (ImageView) findViewById(R.id.board5);
			
			
			
			b1.setVisibility(View.INVISIBLE);
			b2.setVisibility(View.INVISIBLE);
			b3.setVisibility(View.INVISIBLE);
			b4.setVisibility(View.INVISIBLE);
			b5.setVisibility(View.INVISIBLE);	
			
			moveButton();
			
		}
	}
	
	public void displayCard(int id, int cardNum, boolean animate){
		iv = (ImageView) findViewById(id);
		Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.drawable.allcards);
		
		int h = bmp.getHeight() / 4;
		int w = bmp.getWidth() / 13;
		
		Bitmap resizedbitmap1=Bitmap.createBitmap(bmp,(int) Math.floor(cardNum % 13) * w,(int) Math.floor(cardNum/13) * h,w, h);
		iv.setImageBitmap(resizedbitmap1);
		
		ViewGroup.LayoutParams iv_params_b1 = iv.getLayoutParams();
		iv_params_b1.height = (int) (resizedbitmap1.getHeight() * .6);
		iv_params_b1.width = (int) (resizedbitmap1.getWidth() * .6);
		iv.setLayoutParams(iv_params_b1);
		
		if(animate){
			Animation anim;
			iv.setVisibility(View.VISIBLE);
			anim = AnimationUtils.makeInAnimation(MainActivity.this, true);
			iv.startAnimation(anim);
		} else{
			iv.setVisibility(View.VISIBLE);
		}
	}
	public void hideCompCard(){
		
		ImageView c1 = (ImageView) findViewById(R.id.comp1);
		ImageView c2 = (ImageView) findViewById(R.id.comp2);
	
		c1.setBackgroundResource(R.drawable.card_back);
		c2.setBackgroundResource(R.drawable.card_back);
	}
	
	public void startHand(){
		deck.reShuffle();
		player.cards = Arrays.copyOfRange(deck.returnArray(), 0, 2);
		comp.cards = Arrays.copyOfRange(deck.returnArray(), 7, 9);
		board.cards= Arrays.copyOfRange(deck.returnArray(), 2, 7);
		board.newHand();
		player.hasGone = 0;
		comp.hasGone = 0;
		player.bet = 0;
		comp.bet = 0;
		board.turn = board.dealer;
		comp.preflopScore = computePreflopScore(comp.cards);
		comp.gameProbs = computePreflopProbs(comp.preflopScore);
		hideCompCard();
		player.displayCards();
		clearMessages();
		updatePot(); //update this before new blinds are bet
		updateBlinds();
		updatePlayerChips(-player.bet);
		updateCompChips(-comp.bet);	
		updatePlayerBet();
		updateCompBet();
		updateHandNum();
		Toast.makeText(getApplicationContext(), "Preflop score is " + comp.preflopScore, Toast.LENGTH_SHORT).show();
		if(board.dealer == 0) { playerTurn(); }
		else { compTurn(); }
	}
	
	public void nextHand(View v){
		v.setVisibility(View.INVISIBLE);
		startHand();		
	}
	
	public void clearMessages(){
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		TextView cm = (TextView) findViewById(R.id.compMessage);
		pm.setText("");
		cm.setText("");		
	}
	
	public void fold() {
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		pm.setText("Computer wins pot");
		updateCompChips(comp.bet + player.bet + board.pot);
		toggleButtons(1);
		showNextHand();		
	}
	
	public void checkFold(View v){		
		if(player.bet < comp.bet) { fold(); return; }
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		pm.setText("Player Checks");
		player.hasGone = 1;
		determineTurn();
	}
	
	public void call(View v){
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		updatePlayerChips(-(comp.bet - player.bet));
		player.bet = comp.bet;
		updatePlayerBet();
		pm.setText("Player Calls to " + comp.bet);		
		player.hasGone = 1;
		determineTurn();
	}
	
	public void raise(View v){
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		updatePlayerChips(-1 * (comp.bet + board.betSize - player.bet));
		player.bet = comp.bet + board.betSize;	
		updatePlayerBet();
		if(comp.bet == 0){ pm.setText("Player Bets " + player.bet); } 
		else { pm.setText("Player Raises to " + player.bet); } 
		player.hasGone = 1;
		comp.hasGone = 0;
		board.betRound += 1;
		determineTurn();
	}
	
	public void compFold() {	
		//Toast.makeText(getApplicationContext(), "comp folds", Toast.LENGTH_SHORT).show();
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		TextView cm = (TextView) findViewById(R.id.compMessage);
		pm.setText("Player wins pot");	
		cm.setText("Computer Folds");
		updatePlayerChips(comp.bet + player.bet + board.pot);
		comp.hasGone = 1;
		showNextHand();		
	}
	
	public void compCheckFold(){
		if(comp.bet < player.bet) { compFold(); return; }
		//Toast.makeText(getApplicationContext(), "comp checks", Toast.LENGTH_SHORT).show();
		TextView cm = (TextView) findViewById(R.id.compMessage);
		cm.setText("Comp Checks");				
		comp.hasGone = 1;
		determineTurn();
	}
	
	public void compCall(){
		//Toast.makeText(getApplicationContext(), "comp calls", Toast.LENGTH_SHORT).show();
		TextView cm = (TextView) findViewById(R.id.compMessage);
		updateCompChips(-1 * (player.bet - comp.bet));
		comp.bet = player.bet;
		updateCompBet();
		cm.setText("Comps Calls to " + player.bet);		
		comp.hasGone = 1;
		determineTurn();
	}
	
	public void compRaise(){
		//Toast.makeText(getApplicationContext(), "comp raises", Toast.LENGTH_SHORT).show();
		TextView cm = (TextView) findViewById(R.id.compMessage);
		updateCompChips(-1 * (player.bet + board.betSize - comp.bet));
		comp.bet = player.bet + board.betSize;
		updateCompBet();
		if(player.bet == 0){ cm.setText("Comp Bets " + comp.bet); } 
		else { cm.setText("Comp Raises to " + comp.bet); } 
		comp.hasGone = 1;
		player.hasGone = 0;
		board.betRound += 1;
		determineTurn();
	}	
	
	public void updatePlayerBet(){	
		TextView pBet = (TextView) findViewById(R.id.playerBet);		
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
			comp.bet = board.smallBet;
		} else {
			comp.bet = board.smallBet / 2;
			player.bet = board.smallBet;
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
		
		Log.d("In toggle", "whats the problem");
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
	
	public void playerTurn(){
		toggleButtons(0);
		changeButtons();
		//Toast.makeText(getApplicationContext(), "player turn", Toast.LENGTH_SHORT).show();		
	}
	
	public void compTurn(){
		toggleButtons(1);
		double randNum = Math.random();
		if(comp.bet == player.bet) {
			if(randNum < comp.gameProbs[0]){ compCheckFold(); }
			else  { compRaise(); }
		} else if(board.betRound == 4) {
			compCall();
		} else{
			if(randNum < comp.gameProbs[0]){ compCheckFold(); }
			else if(randNum < comp.gameProbs[1])  { compCall(); }
			else { compRaise(); }
		}
		//Toast.makeText(getApplicationContext(), "comp turn", Toast.LENGTH_SHORT).show();
	}
	
	public void changeButtons(){
		Button checkButton  = (Button) findViewById(R.id.checkFold);
		Button callButton  = (Button) findViewById(R.id.call);
		Button raiseButton  = (Button) findViewById(R.id.raise);
		
		if(player.bet == comp.bet){
			checkButton.setText("Check");
			callButton.setVisibility(View.INVISIBLE);			
			raiseButton.setText("Raise to " + (comp.bet + board.betSize));
		} else {
			checkButton.setText("Fold");
			callButton.setText("Call to " + comp.bet);			
			raiseButton.setText("Raise to " + (comp.bet + board.betSize));
			if(board.betRound == 4) { raiseButton.setVisibility(View.INVISIBLE); }
		}		
	}
	
	public void determineTurn(){
		if(comp.hasGone == 1 && player.hasGone == 1){
			
			board.roundNum += 1;
			if(board.roundNum == 3) { board.betSize = board.betSize * 2; }
			//Toast.makeText(getApplicationContext(), "round number now " + board.roundNum, Toast.LENGTH_SHORT).show();
			
			//reset the round
			updatePot();
			if(board.roundNum > 4) { evaluateHand(); return; }	//end of hand
			comp.bet = 0;
			player.bet = 0;
			board.betRound = 0;
			board.turn = 1 - board.dealer;
			comp.hasGone = 0;
			player.hasGone = 0;
			updateCompBet();
			updatePlayerBet();			
			board.deal();
		}
		else { //round still continues
			board.turn = 1 - board.turn;			
		}	
		if(board.turn == 1){ compTurn(); }
		else { playerTurn(); }
	}
	
	public void evaluateHand(){
		
		View vw = (View) findViewById(R.id.nextHand);
		TextView pm = (TextView) findViewById(R.id.playerMessage);
		
		toggleButtons(1);

		comp.displayCards();
		
		//c1.setVisibility(View.v);
		handEvaluator.handValue(concat(player.cards, board.cards));
		player.score = handEvaluator.score;
		player.winMessage = handEvaluator.winMessage;
		
		handEvaluator.handValue(concat(comp.cards, board.cards));
		comp.score = handEvaluator.score;
		comp.winMessage = handEvaluator.winMessage;
		
		vw.setVisibility(View.VISIBLE);
		
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
	}
	
	public void showNextHand(){
		View vw = (View) findViewById(R.id.nextHand);
		vw.setVisibility(View.VISIBLE);
	}
	
	public void updateHandNum(){
		TextView hn = (TextView) findViewById(R.id.hand_num);
		hn.setText("Hand: " + board.handNum);
	}
	
	public int[] concat(int[] A, int[] B) {
		   int aLen = A.length;
		   int bLen = B.length;
		   int[] C= new int[aLen+bLen];
		   System.arraycopy(A, 0, C, 0, aLen);
		   System.arraycopy(B, 0, C, aLen, bLen);
		   return C;
	}
	
	public int computePreflopScore(int[] cards){
		int[] rcards = handEvaluator.changeAce(handEvaluator.getRank(cards));
		int[] scards = handEvaluator.getSuit(rcards);
		
		int tempScore = 0;
		int[] valueCards = {20,21,23,25,28,32,36,41,47,55,65,80,100};
		
		tempScore = valueCards[rcards[0] - 1] + valueCards[rcards[1] - 1];
		if(rcards[0] == rcards[1]) { tempScore += 60; } //pair
		if(scards[0] == scards[1]) { tempScore += 12; } //suited
		if(Math.abs(rcards[0] - rcards[1]) == 1) { tempScore += 7; } //straight
		if(Math.abs(rcards[0] - rcards[1]) == 2) { tempScore += 4; } //straight
		if(Math.abs(rcards[0] - rcards[1]) > 5) { tempScore -= 7; } //too far apart
					
		return(tempScore);
	}
	
	public double[] computePreflopProbs(int score) { //computes preflop probabilities for computer
			if(board.betRound == 1) {
				if(board.dealer == 1) { //comp has button
					if(score < 63) {return(new double[] {.8, .8}); }
					else if(score < 72) { return(new double[] {0, 0}); }
					else if(score < 90) { return(new double[] {0, 0}); }
					else if(score < 100) { return(new double[] {0, 0}); }
					else if(score < 110) { return(new double[] {0, 0}); }
					else if(score < 130) { return(new double[] {0, 0}); }
					else if(score < 150) { return(new double[] {0, 0}); }
					else { return(new double[] {0, 0}); }
				}
				else {				
					if(score < 67) { return(new double[] {1, 1}); }
					else if(score < 75) { return(new double[] {.6, 1}); }
					else if(score < 85) { return(new double[] {0, 1}); }
					else if(score < 90) { return(new double[] {0, 1}); }
					else if(score < 100) { return(new double[] {0, 1}); }
					else if(score < 110) { return(new double[] {0, 0}); }
					else if(score < 130) { return(new double[] {0, 0}); }
					else if(score < 150) { return(new double[] {0, 0}); }
					else { return(new double[] {0, 0}); }
				}										
			}
			return (new double[] {0, 0});
	}
	
}
