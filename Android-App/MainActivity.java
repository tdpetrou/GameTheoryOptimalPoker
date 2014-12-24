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
	//Player player;
	Competitor player;
	Competitor comp;
	Deck deck;
	HandEvaluator handEvaluator = new HandEvaluator();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);				
	
		deck = new Deck();
		deck.print();
		
		board = new Board(0, Arrays.copyOfRange(deck.returnArray(), 2, 7)); //start roundNum at 0 not 1
		
		player = new Competitor(Arrays.copyOfRange(deck.returnArray(), 0, 2), 1000);
		displayCard(R.id.player1, player.cards[0], true);
		displayCard(R.id.player2, player.cards[1], true);	
		
		comp = new Competitor(Arrays.copyOfRange(deck.returnArray(), 7, 9), 1000);
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
	
	public class Competitor {
		int[] cards = new int[2];
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
		String winMessage;
		
		public Competitor(int[] cards, int chips){
			this.chips = chips;
			this.cards[0] = cards[0];
			this.cards[1] = cards[1];
			this.bet = 0;
			this.startChips = 1000;
			this.totalBet = 0;
			this.hasGone = 0;
			this.numRaises = new int[4];
			this.percentile = new int[4];
			this.score = 0;
			this.bluff = (int) Math.round(Math.random());
			this.winMessage = "";
			this.preflopScore = computePreflopScore(this.cards);
			this.gameProbs = computePreflopProbs(this.preflopScore);
			this.drawingHand = false;
			Toast.makeText(getApplicationContext(), "Preflop score is " + this.preflopScore, Toast.LENGTH_SHORT).show();
		}
		
		public void getCurrentHandPercentile(){
			// This calculates the percentile rank for the hand given the current board
			// The percentile is the percentage of two card hands worse than starting hand
			
			int[] currentBoard = Arrays.copyOfRange(board.cards, 0, board.roundNum + 2);
			int[] cardsEvaluated = concat(currentBoard, this.cards); //current hand as of the current board reading
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
			
			//Log.w("percentile", "precentile is " + winners /totalTrials + " float" + winners /(float) totalTrials);
			this.percentile[board.roundNum] = (int) (Math.round(winners / (float) totalTrials  * 100));
			Toast.makeText(getApplicationContext(), "Percentile is " + comp.percentile[board.roundNum], Toast.LENGTH_SHORT).show();
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
		int[] lastRaise;
		
		
		public Board(int roundNum, int[] cards){
			this.roundNum = roundNum;  // the round number will start at 0
			this.cards = cards;
			this.smallBet = 10;
			this.bigBet = 20;
			this.turn = 0;	
			this.betRound = 1;
			this.pot = 0;
			this.dealer = 0;
			this.betSize = this.smallBet;
			this.handNum = 1;
			this.lastRaise = new int[] {-1, -1, -1, -1};
		}
		
		public void increaseRound(){
			this.roundNum += 1;
		}
		
		public void deal(){
			if(this.roundNum == 1){
				showFlop();
			}
			else if(this.roundNum == 2){
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
			this.roundNum = 0;
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
		
//		ViewGroup.LayoutParams iv_params_b1 = iv.getLayoutParams();
//		iv_params_b1.height = (int) (resizedbitmap1.getHeight() * .6);
//		iv_params_b1.width = (int) (resizedbitmap1.getWidth() * .6);
//		iv.setLayoutParams(iv_params_b1);
		
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
        c1.setImageResource(R.drawable.card_back);
        c2.setImageResource(R.drawable.card_back);
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
		player.numRaises = new int[4];
		comp.numRaises = new int[4];
		board.turn = board.dealer;
		board.lastRaise = new int[] {-1, -1, -1, -1};
		comp.preflopScore = computePreflopScore(comp.cards);
		comp.gameProbs = computePreflopProbs(comp.preflopScore);
		comp.bluff = (int) Math.round(Math.random());
		comp.drawingHand = false;
		hideCompCard();
		//
		displayCard(R.id.player1, player.cards[0], false);
		displayCard(R.id.player2, player.cards[1], false);
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
		player.numRaises[board.roundNum] += 1;
		board.lastRaise[board.roundNum] = 0;
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
		comp.numRaises[board.roundNum] += 1;
		board.lastRaise[board.roundNum] = 1;
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
		probAdjustment();
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
	
	public boolean hasOvers() {
		
		int[] currentBoardCards = HandEvaluator.changeAce(HandEvaluator.getRank(Arrays.copyOfRange(board.cards,0, board.roundNum + 2)));
		int[] compCards = HandEvaluator.changeAce(HandEvaluator.getRank(comp.cards));
		Arrays.sort(currentBoardCards);
		Arrays.sort(compCards);
		int maxBoard = currentBoardCards[currentBoardCards.length - 1];
		
		if(compCards[0] > maxBoard && compCards[1] > maxBoard && compCards[0] >= 9) {
			return true;
		} 
		else {
			return false;
		}
	}

	public void getFutureValue() {
		int[] boardCards = Arrays.copyOfRange(board.cards, 0,  board.betRound + 2);
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
					
					if(check1) { numStraights++;} // alert("scard is " + scard  + "  " + check1 + "   " + check2); }
				}
		}
			
			if(numStraights >= 8) { isStraight = true; }
		//	if(isStraight) { alert("straight draw"); }	
			if(isFlush || isStraight) { comp.drawingHand = true; }
	}
	
	public void determineTurn(){
		
		// Determines who's turn it is and move's on to next round if both players have acted 
		
		if(comp.hasGone == 1 && player.hasGone == 1){
			//move on to next round
			board.roundNum += 1;
			if(board.roundNum == 2) { board.betSize = board.betSize * 2; }
			//Toast.makeText(getApplicationContext(), "round number now " + board.roundNum, Toast.LENGTH_SHORT).show();
			
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
			initializeCompProbs();
			updateCompBet();
			updatePlayerBet();	
			//if(board.roundNum < 3)  getFutureValue(); 
			board.deal();
		}
		else { //round still continues
			board.turn = 1 - board.turn;			
		}	
		if(board.turn == 1){ compTurn(); }
		else { playerTurn(); }
	}
	
	public void evaluateHand(){

		TextView pm = (TextView) findViewById(R.id.playerMessage);
		toggleButtons(1);

		displayCard(R.id.comp1, comp.cards[0], false);
		displayCard(R.id.comp2, comp.cards[1], false);

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
	}
	
	public void showNextHand(){
		View vw = (View) findViewById(R.id.nextHand);
		vw.setVisibility(View.VISIBLE);
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
	
	public int computePreflopScore(int[] cards){
		int[] rcards = HandEvaluator.changeAce(HandEvaluator.getRank(cards));
		int[] scards = HandEvaluator.getSuit(rcards);
		
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
	
	

	
	
	public void initializeCompProbs(){
		//this function will initialize a starting probability action for the comp that will be modified later
		
		
		//if(board.dealer == 1) { numPlayerRaises[0] = Math.max(numPlayerRaises[0] - 1, 0); }	
		// might need to bring back line above
		
		int percentile = comp.percentile[board.roundNum];
		int numPlayerRaisesPrevRound = player.numRaises[board.roundNum - 1];
		
	
		
		if(percentile <= 30) { comp.gameProbs = new double[] {1, 1}; } // fold all bad hands
		
			else if(percentile < 40) { 
				if (board.dealer == 1) {
					comp.gameProbs = new double[] {1, 1};
					if (numPlayerRaisesPrevRound > 0 ) {comp.gameProbs[1] = 1;};
				}
				else {
					comp.gameProbs = new double[] {1, 1};
					if (numPlayerRaisesPrevRound > 0 ) {comp.gameProbs[1] = 1;};
				}
			}
			
			else if(percentile < 45) { //if first to enter pot bet but don't raise
				comp.gameProbs = new double[] {0, .7}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {1, 1};} break; // was comp.gameProbs = new double[] {.7, 1};
						case 1: comp.gameProbs = new double[] {1, 1}; break;
						case 2: comp.gameProbs = new double[] {1, 1}; /*alert("folding 40 - 50 percentile");*/ break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {1, 1};}  break; // was comp.gameProbs = new double[] {0, 1};
						case 1: comp.gameProbs = new double[] {1, 1}; break;
						case 2: comp.gameProbs = new double[] {1, 1}; /* alert("folding 40 - 50 percentile"); */ break;	
					}
				}
			}
			
			
			else if(percentile < 50) { //if first to enter pot bet but don't raise
				comp.gameProbs = new double[] {0, .7}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {1, 1};} break; // was comp.gameProbs = [.2, 1];
						case 1: comp.gameProbs = new double[] {1, 1}; break;
						case 2: comp.gameProbs = new double[] {1, 1}; /*alert("folding 40 - 50 percentile");*/ break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {.8, 1};}  break; // was comp.gameProbs = new double[] {0, 1};
						case 1: comp.gameProbs = new double[] {1, 1}; break;
						case 2: comp.gameProbs = new double[] {1, 1}; /* alert("folding 40 - 50 percentile"); */ break;	
					}
				}
			}
			
			else if(percentile < 55) { 
				comp.gameProbs = new double[] {0, .7}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {.5, 1};}  break; // was comp.gameProbs = new double[] {0, .5};
						case 1: comp.gameProbs = new double[] {.5, 1}; break; // was new double[] {0, 1}
						case 2: comp.gameProbs = new double[] {1, 1}; break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {.8, 1};}  break; // was comp.gameProbs = new double[] {0, .5};
						case 1: comp.gameProbs = new double[] {.5, 1}; break; // was new double[] {0, 1}
						case 2: comp.gameProbs = new double[] {1, 1}; break;	
					}
				}
			}
				
			else if(percentile < 60) { 
				comp.gameProbs = new double[] {0, .7}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {.4, 1};}  break; // was comp.gameProbs = new double[] {0, .5};
						case 1: comp.gameProbs = new double[] {.5, 1}; break; // was new double[] {0, 1}
						case 2: comp.gameProbs = new double[] {1, 1}; break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: if(board.betRound == 0) {comp.gameProbs = new double[] {0, .5};} else { comp.gameProbs = new double[] {.7, 1};}  break; // was comp.gameProbs = new double[] {0, .5};
						case 1: comp.gameProbs = new double[] {.5, 1}; break; // was new double[] {0, 1}
						case 2: comp.gameProbs = new double[] {1, 1}; break;	
					}
				}
			}
			
			else if(percentile < 70) { 
				comp.gameProbs = new double[] {0, .6}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, .2}; break;
						case 1: comp.gameProbs = new double[] {0, 1}; break;
						case 2: comp.gameProbs = new double[] {0, 1}; break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, .2}; break;
						case 1: comp.gameProbs = new double[] {0, 1}; break;
						case 2: comp.gameProbs = new double[] {0, 1};	break;
				
					}
				}
			}
			
			else if(percentile < 77) { 
				comp.gameProbs = new double[] {0, .6}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, .2}; break;
						case 1: comp.gameProbs = new double[] {0, 1}; break;
						case 2: comp.gameProbs = new double[] {0, 1}; break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, .2}; break;
						case 1: comp.gameProbs = new double[] {0, 1}; break;
						case 2: comp.gameProbs = new double[] {0, 1};	break;
					}
				}
			}
				
			else if(percentile < 82) { 
				comp.gameProbs = new double[] {0, .5}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, .15}; break;
						case 1: comp.gameProbs = new double[] {0, .7}; break;
						case 2: comp.gameProbs = new double[] {0, 1};	break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, .15}; break;
						case 1: comp.gameProbs = new double[] {0, .7}; break;
						case 2: comp.gameProbs = new double[] {0, 1};	break;	
					}
				}
			}
			
			
			else if(percentile < 87) { 
				comp.gameProbs = new double[] {0, .3}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, 0}; break;
						case 1: comp.gameProbs = new double[] {0, .5}; break;
						case 2: comp.gameProbs = new double[] {0, .8}; break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
                        case 0: comp.gameProbs = new double[] {0, 0}; break;
                        case 1: comp.gameProbs = new double[] {0, .5}; break;
                        case 2: comp.gameProbs = new double[] {0, .8}; break;
					}
				}
			}
			
			else if(percentile < 94) { 
				comp.gameProbs = new double[] {0, .3}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, 0}; break;
						case 1: comp.gameProbs = new double[] {0, .4}; break;
						case 2: comp.gameProbs = new double[] {0, .6}; break;						
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, 0}; break;
						case 1: comp.gameProbs = new double[] {0, .4}; break;
						case 2: comp.gameProbs = new double[] {0, .6}; break;						
					}						
				}
				
			}
			
			else { 
				comp.gameProbs = new double[] {0, .2}; 
				if(board.dealer == 1) {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, 0}; break;
						case 1: comp.gameProbs = new double[] {0, .2}; break;
						case 2: comp.gameProbs = new double[] {0, .3}; break;							
					}
				}
				else {
					switch(numPlayerRaisesPrevRound){
						case 0: comp.gameProbs = new double[] {0, 0}; break;
						case 1: comp.gameProbs = new double[] {0, .2}; break;
						case 2: comp.gameProbs = new double[] {0, .3}; break;	
					}
				}
			}
			
	}
	

	public void probAdjustment() {
		//update probabilities
		
		boolean overs = hasOvers();
		
		//if (board.roundNum > 1) {generalCompProbs();}
		//if (board.roundNum >= 2 && board.roundNum <= 3) { getFutCompHandValue(); } ;
		if (board.pot >= 100 && comp.percentile[board.roundNum] > 46) { comp.gameProbs[0] = 0; }
		if (board.pot >= 140 && comp.percentile[board.roundNum] > 38) { comp.gameProbs[0] = 0; }						//never fold large board.pots
		if (board.pot >= 160 && comp.percentile[board.roundNum] > 30) { comp.gameProbs[0] = 0; }
		if (board.pot >= 200) { comp.gameProbs[0] = 0; }	
		
		if (board.roundNum == 1) {
			if (board.betRound > 2) {comp.gameProbs[0] = 0; }		//call to see flop if already raised pre
			if (board.betRound >= 2 && comp.preflopScore < 120) { comp.gameProbs[1] = 1; }		//should i clamp down pf
			if (comp.bet >= 10 && board.dealer == 1) {comp.gameProbs[0] = 0; } //call if limped in				
		}
		
		if (board.roundNum == 2){
		
			if (comp.percentile[board.roundNum] >= 60) { comp.bluff = 0; }
			
			if (board.betRound == 0) {
			
				if (comp.numRaises[0] == 2) { comp.gameProbs = new double [] {0, 0}; }	//always bet flop after 4 betting pf
				if (board.lastRaise[0] == 1) {  comp.gameProbs = new double [] {0, 0}; } //always bet flop after 3 betting
				
				if (board.dealer == 1) {
					if (board.lastRaise[0] == 0) {comp.gameProbs = new double[] {0, Math.min(.5, comp.gameProbs[1])};} //increase chance of raising flop when on board.dealer	when player has lead pf and checks flop						
					if (comp.bluff == 1 && Math.random() > .8) { comp.gameProbs = new double [] {0, 0}; } //	
				}
				
				if (board.dealer == 0) {
					if (comp.percentile[board.roundNum] < 85 && Math.random() < .9 && board.lastRaise[0] == 0) { comp.gameProbs = new double[] {1, 1};} //check when first to act when player has lead
					if (comp.percentile[board.roundNum] >= 85 && Math.random() < .7 && board.lastRaise[0] == 0) { comp.gameProbs = new double[] {1, 1};} //keep checking to induce bets				
					if (comp.bluff == 1 && Math.random() > .8) { comp.gameProbs = new double [] {0, 0}; } //donk comp.bluff == 1	
				}												
			}
			
			if (board.betRound == 1){
				if (board.dealer == 1) {							
					if (comp.percentile[board.roundNum] > 50 && Math.random() > .3) { comp.gameProbs = new double [] {0, 0}; }	//raise more with decent hands
					if (comp.bluff == 1) { comp.gameProbs = new double [] {0, 0}; }
					if (comp.bluff == 1 && Math.random() < .2) { comp.gameProbs = new double[] {0, 1}; } //call to comp.bluff == 1 the turn
				}
				if (board.dealer == 0) {
					if (comp.percentile[board.roundNum] > 70 && Math.random() > .4) { comp.gameProbs = new double [] {0, 0}; } //raising more with decent hands
					if (comp.bluff == 1 && Math.random() > .5) { comp.gameProbs = new double [] {0, 0}; } //comp.bluff == 1 less often oop			
				}				
				if (overs && comp.percentile[board.roundNum] > 35 && Math.random() > .4) { comp.gameProbs[0] = 0; } //sometimes call with overs
			}
			
			if (board.betRound == 2){
				if (board.dealer == 1) {						
					if (comp.percentile[board.roundNum] < 80) { comp.gameProbs[1] = 1; }	//slow down, do not 3 bet
					if (comp.bluff == 1 && Math.random() > .4) { comp.gameProbs = new double [] {0, 0}; }								
				}
				if (board.dealer == 0) {
					if (comp.percentile[board.roundNum] < 85) { comp.gameProbs[1] = 1; }	//slow down, do not 3 bet							
					if (comp.bluff == 1 && Math.random() > .5) { comp.gameProbs = new double [] {0, 0}; }												
				}			
				if (overs && comp.percentile[board.roundNum] > 35 && Math.random() > .3) { comp.gameProbs[0] = 0; } //sometimes call with overs
			
			}
			
			
			if (board.betRound == 3){
				if (board.dealer == 1) {						
					if (comp.percentile[board.roundNum] < 80) { comp.gameProbs[1] = 1; }	//slow down, do not 3 bet
					if (comp.bluff == 1 && Math.random() > .4) { comp.gameProbs = new double [] {0, 0}; } //4-bet comp.bluff == 1							
				}
				if (board.dealer == 0) {
					if (comp.percentile[board.roundNum] < 85) { comp.gameProbs[1] = 1; }	//slow down, do not 3 bet							
					if (comp.bluff == 1 && Math.random() > .5) { comp.gameProbs = new double [] {0, 0}; } //4-bet comp.bluff == 1											
				}			
				if (overs && comp.percentile[board.roundNum] > 35) { comp.gameProbs[0] = 0; } //call with overs					
			}
			
			if (board.betRound == 4){						
				if (comp.bluff == 1) { comp.gameProbs = new double[] {1, 1}; } //finally fold comp.bluff == 1	
				if (comp.percentile[board.roundNum] < 60) { comp.gameProbs = new double[] {1, 1}; }
				if (overs) { comp.gameProbs[0] = 0; } //call with overs						
			}								
		}
		
		
		if (board.roundNum == 3){
			if (comp.percentile[board.roundNum] >= 60) { comp.bluff = 0; }
			//if (board.lastRaise[1] == 1 && player.numRaises[2] == 2) { comp.bluff == 1 = 0; } //turn comp.bluff == 1 off if called 4 betting
			
			if(board.betRound == 0) {
				if (board.dealer == 1) { //comp has board.dealer
					if (comp.numRaises[1] == 2 && comp.percentile[board.roundNum] > 96.5) { comp.gameProbs = new double [] {0, 0}; } //bet with monster
					if (comp.numRaises[1] <= 1 && board.pot <= 100) { comp.gameProbs = new double[] {0, Math.min(.5, comp.gameProbs[1])};  } //increase chance of betting with air
					//if (board.lastRaise[1] == 1 && player.numRaises[1] <= 1 && comp.percentile[board.roundNum] > 75 && Math.random() > .4) { comp.gameProbs = new double [] {0, 0}; }
					if (board.lastRaise[1] == 1) { comp.gameProbs = new double [] {0, 0}; } //always bet after having lead on flop on board.dealer
					if (comp.bluff == 0 && comp.percentile[board.roundNum] < 60 &&  Math.random() < .4) { comp.gameProbs = new double[] {0, 1}; } // check behind occasionally with mediocre hands
					if (comp.bluff == 1 && board.lastRaise[1] == 1 && Math.random() < .8) { comp.gameProbs = new double [] {0, 0}; } // keep comp.bluff == 1ing almost always
				}
				if (board.dealer == 0) { //player has board.dealer
					if (comp.percentile[board.roundNum] < 85 && Math.random() < .9 && board.lastRaise[1] == 0 && comp.numRaises[1] < 2) { comp.gameProbs = new double[] {1, 1};} //check when first to act when player has lead
					if (comp.percentile[board.roundNum] >= 85 && Math.random() < .7 && board.lastRaise[1] == 0 && comp.numRaises[1] < 2) { comp.gameProbs = new double[] {1, 1};} //keep checking to induce bets						
					if (board.lastRaise[1] == 0 && comp.percentile[board.roundNum] < 77) { comp.gameProbs = new double[] {1, 1}; } //check when player is in lead
					if (comp.percentile[1] - comp.percentile[0] > 15 && board.lastRaise[1] == 0 && player.numRaises[1] > 0) { comp.gameProbs = new double[] {1, 1}; } //check-raise if backed into hand and player has lead
					
					if (board.lastRaise[1] == 1 && player.numRaises[1] <= 2 && comp.percentile[board.roundNum] > 50) { comp.gameProbs = new double [] {0, 0}; } //always bet with lead oop with decent hand
					//if (board.lastRaise[1] == 1 && player.numRaises[1] == 2 && comp.percentile[board.roundNum] > 80) { comp.gameProbs = new double [] {0, 0}; } //always bet with lead oop with decent hand
					
					if (comp.bluff == 1 && board.lastRaise[1] == 1) { comp.gameProbs = new double [] {0, 0}; }
				}					
			}
			
			if(board.betRound == 1) {
				if (comp.percentile[board.roundNum] < 82) { comp.gameProbs[1] = 1; }	//slow down, do not raise
				if (comp.percentile[board.roundNum] < 86 && player.numRaises[1] == 1) { comp.gameProbs[1] = 1; } //never raise turn against strong player hand		
				if (comp.percentile[board.roundNum] < 89 && player.numRaises[1] == 2) { comp.gameProbs[1] = 1; } //never raise turn against strong player hand									
			}
			
			if(board.betRound == 2) {
				if (comp.percentile[board.roundNum] < 92) { comp.gameProbs[1] = 1; }	//slow down, do not 3 bet
				if(comp.percentile[board.roundNum] < 94 && player.numRaises[1] == 2) { comp.gameProbs[1] = 1; } //slow down, don't three bet									
			}			
			
			if (board.betRound == 3) {
				if (comp.percentile[board.roundNum] < 96.5) { comp.gameProbs[1] = 1; }	//slow down, do not 4 bet				
				if(comp.percentile[board.roundNum] < 97.5 && player.numRaises[1] == 2) { comp.gameProbs[1] = 1; } //slow down, don't 4 bet
			}
			
			if (board.betRound == 4){
				comp.gameProbs = new double[] {0, 1};
			}					
		}				
		
		
		if (comp.drawingHand && board.roundNum < 4) { comp.gameProbs[0] = 0; }
		
		
		if (board.roundNum == 4) { 
			if(comp.percentile[board.roundNum] > 94) { comp.gameProbs = new double[] {0, 0}; } //raise all the way
			
			if(board.betRound == 0) {
				if(comp.bluff == 1 && board.lastRaise[2] == 1) { comp.gameProbs = new double [] {0, 0}; }
				if(board.dealer == 0) {
					if (comp.percentile[board.roundNum] < 85 && Math.random() < 1 && board.lastRaise[2] == 0 && comp.numRaises[2] < 2) { comp.gameProbs = new double[] {1, 1};} //check when first to act when player has lead
					if (comp.percentile[board.roundNum] >= 85 && Math.random() < .7 && board.lastRaise[2] == 0 && comp.numRaises[2] < 2) { comp.gameProbs = new double[] {1, 1};} //keep checking to induce bets
					if (comp.percentile[2] - comp.percentile[1] > 15 && board.lastRaise[2] == 0 && player.numRaises[2] > 0) { comp.gameProbs = new double[] {1, 1}; } //check-raise if backed into hand and player has lead							
				}
				if (board.dealer == 1) {						
					if (comp.percentile[board.roundNum] > 50 && player.numRaises[2] == 0) { comp.gameProbs = new double[] {0, 0};} //make sure to bet if checked to on river
					if (comp.percentile[board.roundNum] > 66 && player.numRaises[2] == 1) { comp.gameProbs = new double[] {0, 0};} //make sure to bet if checked to on river
					if (comp.percentile[board.roundNum] > 80 &&  player.numRaises[2] == 2) { comp.gameProbs = new double[] {0, 0};} //make sure to bet if checked to on river					
					if (board.lastRaise[2] == 1 && comp.percentile[board.roundNum] > 55) { comp.gameProbs = new double[] {0, 0}; }
					if (board.pot <= 80 && board.lastRaise[2] == -1 && Math.random() < .4 && comp.percentile[board.roundNum] < 25) { comp.gameProbs = new double [] {0, 0}; }
				}
			}
								
			if (board.betRound == 1) {
				if(comp.percentile[board.roundNum] < 85) { comp.gameProbs[1] = 1; } //never raise river with weakish hand
				if(comp.percentile[board.roundNum] < 88 && player.numRaises[2] == 1) { comp.gameProbs[1] = 1; } //never raise river against strong player hand
				if(comp.percentile[board.roundNum] < 90 && player.numRaises[2] == 2) { comp.gameProbs[1] = 1; } //never raise river against strong player hand
				if (comp.percentile[board.roundNum] > 91 && player.numRaises[2] < 2) { comp.gameProbs = new double [] {0, 0}; } //make sure to raise
			}
			
			if(board.betRound == 2){
				if (comp.percentile[board.roundNum] < 92) { comp.gameProbs[1] = 1; }	//slow down, do not 3 bet
				if(comp.percentile[board.roundNum] < 95 && player.numRaises[2] >= 1) { comp.gameProbs[1] = 1; } //slow down, don't three bet
			}
			
			if (board.betRound == 3) {
				if (comp.percentile[board.roundNum] < 96.5) { comp.gameProbs[1] = 1; }	//slow down, do not 4 bet				
				if(comp.percentile[board.roundNum] < 98 && player.numRaises[2] >= 1) { comp.gameProbs[1] = 1; } //slow down, don't 4 bet
			}
		}
	}
	
}
/Users/Ted/AndroidstudioProjects/coolp/app/src/main/java/com/tdp/coolp/MainActivity.java