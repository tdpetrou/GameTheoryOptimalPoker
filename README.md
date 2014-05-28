GameTheoryOptimalPoker
======================

The above javascript if run from any modern browser will have an artificial intelligence play heads-up limit hold'em poker against itself. The purpose of the ai playing itself is that by tweaking the strategy one can potentially find a strategy that is game theory optimal solving limit hold'em. While this would be impossible to prove analytically it could be tested empirically.

- To change the strategy of the player (cards closest to bottom of the screen) you will need to alter to two functions.

The first function is generalPlayerProbs, which initializes the probability of folding/calling/raising based on hand ranking.

The other function to change is playerProbAdjustment, which changes the strategy based on the action of the hand.

Variable Definitions:
<li>button: 0 is player and 1 is comp</li>

playerPerc[i]: is the percentile of the current player hand based on how it is ranked against every single other possible 2 card hand

playerProbs[i, j]: This array contains two number 0 to 1 and determines the action of the player. If the random number is less than i, the player will check/fold. If the random number is between i and j, the player will call and if the random number is greater than j the player will raise. For example [.2, .9] would mean the player would check/fold 20% of the time, call 70% of the time and raise 10%.

bluff[i]: i = 0 is for the player and the bluff is either 0 or 1 indicating whether or not to bluff

numCompRaises[i]: i is the round number (0 tot 3) and numCompRaises is the number of raises during that round (max of 2)

roundNum: indicates which round it is (0 to 3)

betRound: indicates which betting round it is for each round(flop, turn, etc...) and ranges from 0 to 4
