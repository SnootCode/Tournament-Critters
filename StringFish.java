/*
SocialFish Development
*****************
6/10/2020
- Copied SoloSIVAv2 (as of 4/21/20) to get a jump start on this critter.
- Goals include: Create enormous schools of these social fish
   1. When meet friendly, assume <> position for n turns.
   2. Make giant groups by emergent behavior.
   3. Move the entire groups as a school of fish.
- Can't create method nearby() for some reason, investigate.
- Much progress has been made!
   > SocialFish roam. If next to at least 1 SocialFish, stop and orient:
      ~ Keep facing empty space (CHECK THIS! NOT ALWAYS HAPPENING)
      ~ Turn rear to SocialFish
      ~ Turn rear to Wall
   > Next steps:
      ~ Check that SocialFish always face empty space, don't turn to face a 
         non-empty space if an empty space is avaliable.
      ~ Enable timeLimit, so that smaller groups break up faster than 
         larger ones, and SocialFish search for friends again.
      ~ When friendCount is increased, reset timeLimit.
      ~ Consider: threated, explode the school.
6/14/2020
- Begin "Next Steps"
   > Empty space priority should be near the leaves of the decisional tree, because
      it would short-circuit complex structures if near the root.
      ~ Giving an Infect command when no creature is detected serves no purpose, 
         poor style choice. Revising.
- Test Results:
   > BUG: If a creature passes next to a turtled fish, it doesn't turn. Make it turn.
   > BUG: On the bottom layer, two fish can appear as <^. Unsafe configuration.
      ~ IF Fish on left, fish on right, wall in back OR fish in back, safe. 
         - But if one of left or right is empty, face that way!
- Renamed to StringFish. Now, in information, gather surroundings into Strings:
   > "1234", where 1: Front, 2: Left, 3: Right, 4: Back
   > Made of 'S', 'W', 'E', or 'O' for Same, Wall, Other, or Enemy
- Some hardcoding relies on the arena being larger than certain distance wall-to-wall
   > Interesting Idea: Shrinking Arena, Walls replace Critters
*/

/*
Author: Andrey Risukhin
Class: Just for Fun
TA: Internet
Project: Social Critter
*/ 

// This Critter favors groups. It prefers to find a neighbor and attack only 
// with similar Critters next to it.

//      has a big tree of decisions. It prefers to attack, then moves.
// It randomly decides whether to favor turning right or left. This means it
// cycles both clockwise and anti-clockwise, eliminating Bears and Giants.
// It follows targets, and attempts to track those which pass by it.
// If there's no way to avoid infection, it rotates toward infecter to impede
// their progress, helping fellow Huskies revert it back to a Husky.

import java.awt.*;
import java.util.*;

public class StringFish extends Critter {
   
   // Fields
   private Color color;
   private Action action;
   private String displayedLabel;
   private int rotationCounter;
   private boolean australian; // Bias for right/left turns
   private Random r = new Random(); 
   private int lifespan;
   private int friendCount; // How many friends are around?
   private boolean northReached;
   private String[] label;
   private int timeLimit;
   private final int TIME_LIMIT;
   private int timeCooldown;
   private int resetTurtle;
   private String status;
   private int enemyCount;
         
   // Constructor
   public StringFish() {
      //Color jazzberry = new Color(179, 0, 89); // red, green, blue
      color = new Color(179, 0, 89); 
      displayedLabel = ":(";
      label = new String[] {":(", ":|", ":)", ":D", "XD"};
      australian = r.nextBoolean(); // True -> Right, False -> Left
      lifespan = 0;
      TIME_LIMIT = 10;
      timeLimit = TIME_LIMIT; // How many turns until they leave to find new friends
      timeCooldown = 0;
      friendCount = 0;
      rotationCounter = 0;
      resetTurtle = 0;
      status = "";
      enemyCount = 0;
   }

   // SocialFish Behavior, group forming 
   public Action getMove(CritterInfo info) {
      
      // ******** // Information
      color = new Color(179, 0, 89);
      
      // Re-create status each turn, updating various counters. 
         // Only time check should be performed, cleaner.
      status = "";
      friendCount = 0;
      enemyCount = 0;
         // Front Check
         if (info.getFront() == Neighbor.SAME) {
            status += "S";
            friendCount++;
         } else if (info.getFront() == Neighbor.WALL) {
            status += "W";
         } else if (info.getFront() == Neighbor.EMPTY) {
            status += "E";
         } else { // if (info.getFront() == Neighbor.OTHER)
            status += "O";
            enemyCount++;
         }
         // Left Check
         if (info.getLeft() == Neighbor.SAME) {
            status += "S";
            friendCount++;
         } else if (info.getLeft() == Neighbor.WALL) {
            status += "W";
         } else if (info.getLeft() == Neighbor.EMPTY) {
            status += "E";
         } else { // if (info.getLeft() == Neighbor.OTHER)
            status += "O";
            enemyCount++;
         }
         // Right Check
         if (info.getRight() == Neighbor.SAME) {
            status += "S";
            friendCount++;
         } else if (info.getRight() == Neighbor.WALL) {
            status += "W";
         } else if (info.getRight() == Neighbor.EMPTY) {
            status += "E";
         } else { // if (info.getRight() == Neighbor.OTHER)
            status += "O";
            enemyCount++;
         }
         // Back Check
         if (info.getBack() == Neighbor.SAME) {
            status += "S";
            friendCount++;
         } else if (info.getBack() == Neighbor.WALL) {
            status += "W";
         } else if (info.getBack() == Neighbor.EMPTY) {
            status += "E";
         } else { // if (info.getBack() == Neighbor.OTHER)
            status += "O";
            enemyCount++;
         }
      
         if (timeLimit == 0) {
            friendCount = 0; // Time is up, must seek new friends
            timeCooldown = 3; // Reset cooldown for timeLimit reset (to explore)
         }
         if (timeCooldown == 0) { // Finished exploring, ready to make friends
            timeLimit = TIME_LIMIT; // Reset timeLimit to maximum value
         }
      
      // Display happiness level. Turn into the happiness() method.
      displayedLabel = label[friendCount];
      if (enemyCount > 0) {
         displayedLabel = "!"; // Alarm at noticing enemy/enemies
         color = new Color(255, 0, 0);
      }
      
      // ********** // Actions
      
      if (status.equals("SSSS") || status.equals("SWSS") || status.equals("SSWS") 
      || status.equals("SSSW") || status.equals("SSWW") || status.equals("SWSW")) { 
         // Totally Safe
         action = Action.LEFT; // Party Spin!
      } else if (friendCount == 0) { // No friends, lonely mode
         // Lonely mode  
         if (status.substring(0,1).equals("E")) { // Empty in front
            action = Action.HOP;   
         } else if (status.substring(0,1).equals("O")) { // Other in front
            action = Action.INFECT;
         } else { // Wall in front (NOT Same in front)
            if (australian) {
               action = Action.RIGHT;
            } else {
               action = Action.LEFT;
            }            
         }        
      } else { // Some friends, not Total Safety
         if (friendCount > 2) {
         
         } else { // 1 friend
            if (status.substring(0,1).equals("E") || status.substring(3,4).equals("S")) { 
               
               
               
               action = Action.INFECT;
            } else { // Imperfect
            
            }
         }
         
         
         
         
         
         
         
         if (enemyCount > 0) {
            // Enemies nearby!
         
         } 
         
         // If first letter = wall, must turn
         // If 2nd, 3rd, 4th letter = other, must turn
      } 


      
      
      if (info.getFront() == Neighbor.OTHER) { // If enemy in front, infect
         action = Action.INFECT;
      } else {
         if (friendCount == 0) { // If no SocialFish nearby
            // This section of code gets repeated. Turn into wander() method.
            if (info.getFront() == Neighbor.EMPTY) {
               action = Action.HOP;   
            } else if (info.getFront() == Neighbor.OTHER) {
               action = Action.INFECT;
            } else {
               if (australian) {
                  action = Action.RIGHT;
               } else {
                  action = Action.LEFT;
               }            
            }          
         } else { // At least 1 SocialFish nearby
            if (timeCooldown != 0 || timeLimit == 0) { // Not ready to be friendly OR time is up
               // This section of code gets repeated. Turn into wander() method.
               if (info.getFront() == Neighbor.EMPTY) {
                  action = Action.HOP;   
               } else {
                  if (australian) {
                     action = Action.RIGHT;
                  } else {
                     action = Action.LEFT;
                  }            
               }  
            } else { // At least 1 SocialFish nearby AND I'm ready to socialize!
               if (rotationCounter > 0) { // If mid-turn, keep turning
                  // Repeated code, create turn() method
                  if (australian) {
                     action = Action.RIGHT;
                  } else {
                     action = Action.LEFT;
                  }            
                  rotationCounter--;
               } else { // Decide what to do
                  // Where are the other SocialFish, so I position myself well? <>
                  if (info.getBack() == Neighbor.SAME) { // Fish behind
                     if (info.getLeft() == Neighbor.SAME) { // Fish behind, left
                        if (info.getRight() == Neighbor.SAME) { // Fish behind, left, right
                           if (info.getFront() == Neighbor.SAME) { // Fish behind, left, right, front
                              // Totally safe, party spin
                              action = Action.LEFT;
                                 // If came into school, spin 180 to point outwards
                           }
                        }
                     } 
                  } else if (info.getBack() == Neighbor.WALL) { // Wall behind
                  
                  } else if (info.getBack() == Neighbor.OTHER) { // Enemy behind
                  
                  } else { // Empty behind
                  
                  }
                  // unfinished edit
                  
                  
                  
                  
                  
                  
                  
                  if ((info.getBack() == Neighbor.SAME && info.getFront() == Neighbor.EMPTY) || 
                     (info.getBack() == Neighbor.WALL && info.getFront() == Neighbor.EMPTY)) { // Safe. 
                     // Turtle Mode: stay put, face opponent
                     if (info.leftThreat()) { // reset turtle += -1; -1 is to the left, reset to zero
                        resetTurtle--;
                        action = Action.LEFT;
                     } else if (info.rightThreat()) { // reset turtle += 1; +1 is to the right, reset to zero
                        resetTurtle++;
                        action = Action.RIGHT;
                     } else {
                        if (resetTurtle != 0) { // Reset towards original position (0) 
                           if (resetTurtle < 0) {
                              resetTurtle++;
                              action = Action.RIGHT;
                           } else { // resetTurtle > 0
                              resetTurtle--;
                              action = Action.LEFT;
                           }
                           
                             
                        } else {
                           action = Action.INFECT; 
                        }
                     }
   
                  } else { // Not Safe. Become Safe.
                     if (info.getFront() == Neighbor.SAME) { // Facing SocialFish, face away!
                        // Repeated code, create turn() method
                        if (info.getLeft() == Neighbor.WALL) {
                           action = Action.RIGHT; // Face away from wall
                        } else if (info.getRight() == Neighbor.WALL) {
                           action = Action.LEFT; // Face away from wall
                        } else { // If Walls aren't nearby, randomize rotation             
                           if (australian) {
                              action = Action.RIGHT;
                           } else {
                              action = Action.LEFT;
                           }
                        }      
                     } else if (info.getLeft() == Neighbor.SAME) { // NOT facing SocialFish
                        if (info.getRight() == Neighbor.SAME) { // Have a SocialFish on both sides fFf
                           if (info.getFront() == Neighbor.WALL) { // <v>, become <^>
                              rotationCounter = 2;
                              // Repeated code, create turn() method
                              if (australian) {
                                 action = Action.RIGHT;
                              } else {
                                 action = Action.LEFT;
                              }            
                              rotationCounter--;
                           } else { // Neighbor both sides, not facing wall
                              action = Action.INFECT; // Safe
                           }
                        } else { // Neighbor only on Left                     
                           action = Action.RIGHT; // Turn to <> position, away from neighbor
                        }
                     } else { // No Neighbor on Left AND No Neighbor in Front
                        if (info.getRight() == Neighbor.SAME) { // 
                           action = Action.LEFT; // Turn to <> position, away from neighbor
                        }
                     }
                  }               
               }             
               // action = Action.LEFT; // If find a friend, spin in a circle
               timeLimit--;
               
               // timeLimit += friendCount; // Balance so that smaller groups break apart faster than larger groups
            }
         }
      }
      return action;   
   }
   
   /*
   // Returns the count of adjacent friendly Critters
   public int nearby() {
      int count = 0;
      if (info.getFront() == Neighbor.SAME) {
         count++;
      }
      if (info.getLeft() == Neighbor.SAME) {
         count++;
      }
      if (info.getRight() == Neighbor.SAME) {
         count++;
      }
      if (info.getBack() == Neighbor.SAME) {
         count++;
      }
      return count;
   } */
   
   public Color getColor() {
      return color;
   }
   
   public String toString() {
      return displayedLabel;
   }
}