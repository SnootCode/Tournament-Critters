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
6/15/2020
- Successful testing, various Critters including SoloSIVA, StringFish alwasy won in the end!
- Current decisions are primarily based upon # of similar Critters nearby, then on enemies nearby.
- Next: Add time gate mechanic, so smaller groups break apart?
- Testing Round:
   > 30 Bear; 60 Lion; 30 Giant; 30 SoloSIVA; 60 FlyTrap; 30 StringFish; Win
   > 60 Bear; 60 Lion; 30 Giant; 30 SoloSIVA; 30 FlyTrap; 30 StringFish; Some Loss - overwhelmed by SIVA
   > 1 & 2 neighbors & walls = spinning


*/

/*
Author: Andrey Risukhin
Class: Just for Fun
TA: Internet
Project: Social Critter
*/ 

// This Critter favors groups. If lonely, it prefers to flee. Once it finds a
// similar critter it forms a group, assuming a defensive formation, and is
// more likely to attack. When moving, it either favors left or right, depending
// on how it was constructed (randomly Australian). Large groups tend to form
// adjacent to walls, because it is safer (walls don't attack). This means it
// cycles both clockwise and anti-clockwise, eliminating Bears and Giants.
// It follows targets, and attempts to track those which pass by it.

// If there's no way to avoid infection, it rotates toward infecter to impede
// their progress, helping fellow Huskies revert it back to a Husky.

import java.awt.*;
import java.util.*;

public class StringFish extends Critter {
   
   // Fields
   private Color color;
   private final Color tropicalFish;
   private final Color yellow;
   private Action action;
   private String displayedLabel; // Current label
   private String[] label; // Some possible labels
   private boolean australian; // Bias for right/left turns
   private Random r = new Random(); 
   private int lifespan;
   private int friendCount; // # of same critters around
   private int enemyCount; // # of other critters around
   private int timeLimit; // # turns left to stay in group
   private final int TIME_LIMIT; // # turns critter stays in group
   private int timeCooldown; // # turns until critter will find another group
   private String status;
         
   // Constructor
   public StringFish() {
      tropicalFish = new Color(201, 255, 229); // red, green, blue
      yellow = new Color(255, 255, 0);
      color = tropicalFish; 
      displayedLabel = ":(";
      label = new String[] {":(", ":|", ":)", ":D", "XD"};
      australian = r.nextBoolean(); // True -> Right, False -> Left
      lifespan = 0;
      TIME_LIMIT = 10;
      timeLimit = TIME_LIMIT; // How many turns until they leave to find new friends
      timeCooldown = 0;
      friendCount = 0;
      status = "";
      enemyCount = 0;
   }

   // SocialFish Behavior, group forming 
   public Action getMove(CritterInfo info) {
      
      // ******** // Information
      color = tropicalFish; 
      status = ""; // Re-create status each turn, updating various counters.
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
         displayedLabel = "8("; // Alarm at noticing enemy/enemies
         color = yellow;
      }
      
      // ********** // Actions
      
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
         } else if (friendCount == 1) { // 1 SocialFish nearby <>
            if (enemyCount > 0) {
               if ((info.getFront() == Neighbor.EMPTY) && (info.backThreat() 
               || info.leftThreat() || info.rightThreat())) { // Threatened
                  action = Action.HOP;
               } else { // Not Threatened Yet
                  if (info.getLeft() == Neighbor.OTHER) {
                     action = Action.LEFT;
                  } else if (info.getRight() == Neighbor.OTHER) {
                     action = Action.RIGHT;
                  } else { // Enemy behind, Not in front
                     // Turn 180, favoring empty space
                     if (info.getRight() == Neighbor.EMPTY) {
                        action = Action.RIGHT;
                     } else { // Hopefully empty
                        action = Action.LEFT;
                     }
                  }
               }
            } else { // No enemies nearby
               if (info.getBack() == Neighbor.SAME && info.getFront() == Neighbor.EMPTY) {
                  // Safest Outcome
                  action = Action.INFECT;
               } else if (info.getBack() == Neighbor.SAME && info.getFront() == Neighbor.WALL) {
                  if (info.getLeft() == Neighbor.WALL) {
                     if (info.getRight() == Neighbor.WALL) {
                        if (info.getBack() == Neighbor.WALL) {
                           action = Action.INFECT; // Should never be reached, no 3 walls touch
                        } else { // Turn 180, randomly
                           if (australian) {
                              action = Action.RIGHT;
                           } else {
                              action = Action.LEFT;
                           } 
                        }
                     } else {
                        action = Action.RIGHT;
                     }
                  } else {
                     action = Action.LEFT;
                  }  
               } else { // Get into <> or ^> position
                  if (info.getFront() == Neighbor.SAME) {
                     if (australian) {
                        action = Action.RIGHT;
                     } else {
                        action = Action.LEFT;
                     }
                  } else if (info.getLeft() == Neighbor.SAME || info.getLeft() == Neighbor.WALL) {
                     action = Action.RIGHT;
                  } else if (info.getRight() == Neighbor.SAME || info.getRight() == Neighbor.WALL) { 
                     action = Action.LEFT;
                  } else {
                     action = Action.INFECT;
                  }
               }
            }  
         } else if (friendCount == 2) { // 2 SocialFish nearby <^>       
            if (enemyCount > 0) {
               if ((info.getFront() == Neighbor.EMPTY) && (info.backThreat() 
               || info.leftThreat() || info.rightThreat())) { // Threatened
                  action = Action.HOP;
               } else { // Not Threatened Yet
                  if (info.getLeft() == Neighbor.OTHER) {
                     action = Action.LEFT;
                  } else if (info.getRight() == Neighbor.OTHER) {
                     action = Action.RIGHT;
                  } else { // Enemy behind, Not in front
                     // Turn 180, favoring empty space
                     if (info.getRight() == Neighbor.EMPTY) {
                        action = Action.RIGHT;
                     } else { // Hopefully empty
                        action = Action.LEFT;
                     }
                  }
               }
            } else { // No enemies nearby
               // Make <^> shape, or L shape
               if (info.getFront() == Neighbor.SAME || info.getFront() == Neighbor.WALL) { // Turn away
                  if (info.getLeft() == Neighbor.SAME || info.getLeft() == Neighbor.WALL) { // Not turn left
                     if (info.getRight() == Neighbor.SAME || info.getRight() == Neighbor.WALL) { // Not turn right
                        // Must turn around
                        if (info.getRight() == Neighbor.WALL) { // Avoid facing wall at any point
                           action = Action.LEFT;
                        } else if (info.getLeft() == Neighbor.WALL) {
                           action = Action.INFECT;
                        } else {
                           action = Action.RIGHT;
                        }
                     } else {
                        action = Action.RIGHT;
                     } 
                  } action = Action.LEFT;
               } else {
                  action = Action.INFECT;
               }
            }
         } else if (friendCount == 3) { // 3 SocialFish nearby
            // Make a school. If you have 3 nearby, move to the open space and take them with you. 
               // Or if I don't learn to do that, send this one off on its own?
            if (info.getFront() == Neighbor.EMPTY) { 
               action = Action.HOP;
            } else if (info.getLeft() == Neighbor.EMPTY) { // By looking for empty space, it will also face an enemy
               action = Action.LEFT;
            } else if (info.getRight() == Neighbor.EMPTY) {
               action = Action.RIGHT;
            } else if (info.getBack() != Neighbor.WALL) { // Back empty/enemy, must be if other three are taken up
               action = Action.LEFT; 
            } else {
               action = Action.INFECT; // Infect attempt if have back to a wall & 3 neighbors
            }     
         } else { // 4 SocialFish nearby
            action = Action.INFECT; // Nothing else to do :)
         }      
      }
      return action;   
   }
   
   // Post: returns Critter color.
   public Color getColor() {
      return color;
   }
   
   // Post: returns Critter label.
   public String toString() {
      return displayedLabel;
   }
}