# IPD Tournament 

Álvaro Sánchez García

## The program has the basic players implemented and three more that are described below:

	* Tftt: it is basically a tft, but if the other player acts unfairly twice in a row, the tftt will always defects 
	from that moment on. The objective of this algorithm is to improve the weaknesses of tft, since if a strategy that works very 
	well with tft is, for example: CDDD ... With these changes this algorithm manages to beat Spitful on some occasions.

	* Pavlov: in this Pavlov implementation two possible outcomes are defined: success (if the player payoff is 5 or 3) 
	and defeat (if the player payoff is 1 or 0). If the result of the last round is succes the agent plays same move, 
	otherwise he change the move.

	* Simple: This agent is very simple: if the payoff of the last round was less or equal than 1 he defect, 
	otherwise plays the same action.The idea behind this agents is to try to maximize their points based on the 
	points they got in the previous round, if they get 3 or more points, either the two cooperated or my agent 
	acted unfairly while the other cooperated, for what makes up for it to continue with the same action. 
	If the other starts to defect, then my agent will remain defective.

+ I dont know what agent use in the tournament at this moment, but I think I will use the Tftt.

+ You need the jade library which is in /lib/jade.jar and the classes found in the Common package which are used by the Main and GUI.

+ I added a function to clean the console for this I simply write an empty string in the console.

+ When a game is over, a series of messages are displayed on the console indicating that the Main is waiting for a move, I think it 
	is an error when writing the messages (they are late), but it does not affect the program functionality.
	Also detect some problem with the function suspend and activate, although in most cases they work fine, sometimes it can happen that the program freezes.
	For reasons that I do not know in the GUI, all the essentials are only seen if it is in full screen, I could not correct the problem.
	
