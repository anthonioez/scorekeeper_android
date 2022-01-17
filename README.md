# scorekeeper_android
 
A simple scorekeeper for an arbitrary number of players 
Users log in as either a player or a score monitor.

Once a player logs in, their screen shows:
Three buttons
- One to increment their score
- One to decrement their score
- One to reset their score to 0

Displays their score at the top

When a scorekeeper logs in, their screen shows each player name and score, 
with the current highest score highlighted.

Players/scorekeepers should be able to log in asynchronously. I.e. 

players should be able to join a game and start scorekeeping and 
then the scorekeeper can come in after and see everyone's current score.

Optional features
Set an alert for the scorekeeper when a new player has taken the lead 
(I.e. player A has 5 points and Player B has 6 points; then player A 

increments their score 2 points bringing the score to A-7 vs B-6; 

the scorekeeper's device would then buzz to alert them someone else has taken the lead.
