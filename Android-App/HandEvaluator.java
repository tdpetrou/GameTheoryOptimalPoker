package com.tdp.coolp;


//import java.util.ArrayList;
//import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.*;



public class HandEvaluator{
  
  double score = 0;
  String winMessage = "";
  int[] totalHands; 
  int[] flushCards;
  boolean isFlush;
  int flushSuit;
  int straightCard;    
  int[] cards;
  int[] ranks;
  int[] suits;
  int[] ucards;
  int numCards;
  
 public HandEvaluator(int[] hand, int[] totalHands){
      this.score = 0;
      this.winMessage ="";
      int[] cards = hand;
      this.totalHands = totalHands;
      this.isFlush = false;
      this.flushSuit = -1;
      this.straightCard = -1;
      this.cards = hand;
      this.ranks = getRank(cards);
      this.suits = getSuit(cards);       
      this.numCards = this.cards.length;
            
  }
  
 //empty constructor in case I want to make an entire new object
 public HandEvaluator(){  
      this.score = 0;
      this.winMessage ="";
      int[] cards = new int[7];
      this.totalHands = new int[9];
      this.isFlush = false;
      this.flushSuit = -1;
      this.straightCard = -1;
      this.cards = new int[7];
      this.ranks = getRank(cards);
      this.suits = getSuit(cards);
      this.ucards= getUniqueCards(this.ranks);
       
  }
 
 public void reset(){
     
     this.score = 0;
      this.winMessage ="";       

      this.isFlush = false;
      this.flushSuit = -1;
      this.straightCard = -1;       
      this.ranks = getRank(cards);
      this.suits = getSuit(cards);        
      this.numCards = this.cards.length;   
 }
 
  public void handValue(int cards[]) {              
      
      this.cards = cards;
      reset();
      checkFlush(); //check for straight flush first
      if(this.isFlush){            
          checkStraight(this.flushCards);
          if(this.straightCard > 0){
               this.score = (double) (this.straightCard + 4) / 14 * 400000000;  
               this.totalHands[8]++;
               this.winMessage = " straight flush " + getCardName(this.straightCard + 4) + " high";                 
               return;
          }
      }
       check4(); //check for all other types of hands
       return;
  }
  
  public void checkFlush() {
        
      int countFlushCards = 0;     
      
      //check if there are 5 or more of same suit
      for(int i = 0; i < 4; i++){
          countFlushCards = 0;
          for(int j = 0; j < this.numCards; j++){
              if(i == this.suits[j]) { countFlushCards++; }
          }
          if(countFlushCards >= 5) {
              this.isFlush = true;
              this.flushSuit = i;
              getFlushCards(countFlushCards);
              break;
          }
      }
  }     
  
  //get only flush cards
  public void getFlushCards(int count){
      this.flushCards = new int[count];
      int ct = 0; //counts just the flush cards
      for(int i = 0; i < this.suits.length; i++){
          if(this.suits[i] == this.flushSuit){ this.flushCards[ct++] =  this.ranks[i]; }
      }
  }
  
  public void checkStraight(int[] cards) {        
      
      int straightCard = -1;   
      int[] ucards = getUniqueCards(cards);      
      
      Arrays.sort(ucards); //descending?
      straightCard = rowStraight(ucards);
      
      ucards = changeAce(ucards); //check ace high straight
      Arrays.sort(ucards);     
      this.straightCard = Math.max(rowStraight(ucards), straightCard); 
  }   

  
  public static int rowStraight(int[] cards){
      int straightCard = -1;
      
      for(int i = 0; i < cards.length - 4; i++){ //check if in a row      
          if(cards[i + 4] - cards[i] == 4) straightCard = cards[i];   
      }
      return(straightCard + 1);
  } 
  
  public void check4(){ //check for 4 of a kind
      int fourRank = 0;
      Integer[] threeRank = {0,0,0};
      int threeCount = 0;
      Integer[] twoRank = {0,0,0};
      int twoCount = 0;
      Integer[] oneRank = {0,0,0,0,0,0,0};
      int oneCount = 0;
      
      
      int countRank = 0;          
      int maxKicker = 0;
      
      //go through rank by rank and count the occurence of each rank
      for(int i = 0; i < 13; i++){ 
          countRank = 0;
          for(int j = 0; j < cards.length; j++){
              if(i == this.ranks[j]){ countRank++; }                
          }
          
          switch(countRank) {
              case 4:
                  fourRank = changeAceValue(i);
                  break;
              case 3:
                  threeRank[threeCount++] = changeAceValue(i);
                  break;
              case 2:
                  twoRank[twoCount++] = changeAceValue(i);
                  break;
              case 1:
                  oneRank[oneCount++] = changeAceValue(i);
                  break;
          }
      }
     
   if(oneCount > 0) Arrays.sort(oneRank, Collections.reverseOrder()); //descending order
   if(twoCount > 0) Arrays.sort(twoRank, Collections.reverseOrder());
   if(threeCount > 0) Arrays.sort(threeRank, Collections.reverseOrder());
   
   
   //check for four of a kind
   if (fourRank > 0) {     
       maxKicker = Math.max(Math.max(oneRank[0], twoRank[0]), threeRank[0]); 
       this.score = (double) (fourRank + 1) / 13 * 20000000 + (double) maxKicker / 13;
       this.totalHands[7]++;
       this.winMessage = " four of a kind " + getCardName(fourRank + 1) + "'s " + getCardName(maxKicker + 1) + " kicker";
       return;
   }
   
   //check for full house
   if(threeCount == 2){
       this.totalHands[6]++;
       this.score = ((double) (threeRank[0] / 13) * 20 + (double) threeRank[1] / 13) / 11 * 1000000;         
       this.winMessage = " full house " + getCardName(threeRank[0] + 1) + "'s full of " + getCardName(threeRank[1] + 1) + "'s";
       return;
   }
   
   if(threeCount == 1 && twoCount >= 1){ 
       this.totalHands[6]++;
       this.score = ((double) threeRank[0] / 13 * 20 + (double) twoRank[0] / 13)/11 * 1000000;
       this.winMessage = " full house " + getCardName(threeRank[0] + 1) + "'s full of " + getCardName(twoRank[0] + 1) + "'s";
       return;
   }
   
   //check for flush
   if(this.isFlush){
       this.totalHands[5]++;
       this.flushCards = changeAce(getRank(this.flushCards));
       Arrays.sort(this.flushCards);
       int len = flushCards.length;
       this.score = (double) ((double) flushCards[len - 1] / 13 * 10000 + (double) flushCards[len - 2] / 13 * 1000 + (double) flushCards[len - 3] / 13 * 100 + (double) flushCards[len - 4] / 13 * 10 + (double) flushCards[len - 5] / 13) / 11111 * 100000;
       this.winMessage = " flush " + getCardName(flushCards[len - 1] + 1) + " high";         
       return;
   }
   
   //check for straight
   checkStraight(this.ranks);
   if(this.straightCard > 0){
       this.score = (double) (this.straightCard + 4) / 14 * 10000; 
       this.totalHands[4]++;
       this.winMessage = " straight " + getCardName(this.straightCard + 4) + " high";
       return;
   }
   
   //check for trips
   if(threeCount == 1){
       this.totalHands[3]++;
       this.score = ((double) threeRank[0] / 13 * 200 + (double) oneRank[0] / 13 * 10 + (double) oneRank[1] / 13) / 111 * 1000;
       this.winMessage = " three of kind " + getCardName(threeRank[0] + 1) + "'s "+ getCardName(oneRank[0] + 1) + " kicker";
       return;
   }
   
   //check for two of a kind
   if(twoCount >= 2){
       this.totalHands[2]++;
       maxKicker = Math.max(oneRank[0], twoRank[2]);
       this.score = ((double) twoRank[0] / 13 * 200 + (double) twoRank[1] / 13 * 10 + (double) maxKicker / 13) / 111 * 100;        
       this.winMessage = " two pair " + getCardName(twoRank[0] + 1) + "'s and " + getCardName(twoRank[1] + 1) + "'s " + getCardName(maxKicker + 1) + " kicker";
       return;
   }
   
   //check for one pair
   if (twoCount == 1) {
       this.totalHands[1]++;
       this.score = ((double) twoRank[0] / 13 * 1000 + (double) oneRank[0] / 13 * 100 + (double) oneRank[1] / 13 * 10 + (double) oneRank[2] / 13) / 1111 * 10;
       this.winMessage = " one pair " + getCardName(twoRank[0] + 1) + "'s " + getCardName(oneRank[0] + 1) + " kicker";         
       return;
   }
   else { // no pair
       this.totalHands[0]++;
       this.score = (double) ((double) oneRank[0] / 13 * 10000 + (double) oneRank[1] / 13 * 1000 + (double) oneRank[2] / 13 * 100 + (double) oneRank[3] / 13 * 10 + (double) oneRank[4] / 13) / 11111 * 1;
       this.winMessage = " high card " + getCardName(oneRank[0] + 1) + " " + getCardName(oneRank[1] + 1) + " high";
       return;      
   }
   }   
  
  //change entire hand ace to high
  public static int[] changeAce(int[] cards){
      for(int i = 0; i < cards.length; i++){
          if (cards[i] == 0){
              cards[i] = 13;
          }
      }
      return(cards);
  }
  
  //change single card Ace to high
  public static int changeAceValue(int n){
      if(n == 0){
          return(13);
      }
      else { return(n); }
  }
  
  //show cards
  public void showCards(){
    
      cards = changeAce(this.ranks);
      for(int i = 0; i < this.numCards; i++){
          System.out.println("card " + i + " + is " + (this.cards[i]) + " rank is "   + getCardName(this.cards[i] +1));
      }           
  }
  
  //print to file
  public String printFile(){
      String content = "";
      for(int i = 0; i < cards.length; i++){
          content += " " + getCardShortName(this.ranks[i] + 1) + "" + getCardSuit(this.suits[i]);
      }        
      return(content + "\r");        
  }
  
  //get just the rank (0 - 12) of the cards 
  public static int[] getRank(int[] cards){
      int[] scards = new int[cards.length];
      for(int i = 0; i < cards.length; i++){ //get rank
          scards[i] = cards[i] % 13;      
      }
      return(scards);
  }
  
  //get the suit (0 - 3)
  public static int[] getSuit(int[] cards){
      int[] scards = new int[cards.length];
      for(int i = 0; i < cards.length; i++){ //get rank
          scards[i] = (int) Math.floor(cards[i] / 13);      
      }
      return(scards);
  }
  
  //return the name of the card
  public String getCardName(int card){
      if(card <= 10 && card >= 2){
          return(Integer.toString(card));
      }
      else {
          switch(card) {
              case 11:
                  return("jack");
                  
              case 12:
                  return("queen");
                  
              case 13:
                  return("king");
                  
              default:
                  return("ace");
                        
          }      
      }    
  }
  
  //get single charachter for card
  public String getCardShortName(int card){
      if(card <= 9 && card >= 2){
          return(Integer.toString(card));
      }
      else {
          switch(card) {
              case 10:
                  return("T");
              case 11:
                  return("J");
                  
              case 12:
                  return("Q");
                  
              case 13:
                  return("K");
                  
              default:
                  return("A");                          
          }      
      }    
  }
  
  //get the single character suit of card
  public String getCardSuit(int card){
          switch(card) {
              case 0:
                  return("s");
                  
              case 1:
                  return("h");
                  
              case 2:
                  return("c");
                  
              default:
                  return("d");
                        
          }      
  }    
  
  //return only unique ranks
  public static int[] getUniqueCards(int[] cards){        
      int[] uCards = new int[7];
      uCards[0] = cards[0];
      int ct = 1; //unique counter
     
      
      boolean dupe = false;
       for(int i = 1; i < cards.length; i++){
           dupe = false;
            for(int j = 0; j < ct; j++){
                if(uCards[j] == cards[i]){
                    dupe = true;
                    break;
                }
            }
            if(!dupe) uCards[ct++] = cards[i]; 
       }
      
      return(Arrays.copyOfRange(uCards, 0, ct)); //return just the unique part of array
  }
  
  public static void main(String[] args){        
      
      /*change these numbers to enter hand
       * the suits change every thirteen
       * (0 - 12) is one suit and so on
       * the suits are arbitrary 
       */
      int[] hand = {30, 31, 32, 33, 40, 41, 44};
      HandEvaluator t = new HandEvaluator();
      t.handValue(hand);  
    
      System.out.println("Value is " + t.score);
      System.out.println("winner message is " + t.winMessage);
      
  }
}
