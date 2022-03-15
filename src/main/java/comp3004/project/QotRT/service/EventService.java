package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.*;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.HashMap;

public class EventService {

    public void doEvent(Game game, SimpMessagingTemplate simpMessagingTemplate){
        if(game.getCurrentStoryCard() instanceof ChivalrousDeed) {
            String lowRank = "";
            int leastNumShields = 0;
            ArrayList<Player> playerArrayList = new ArrayList<>();
            HashMap<String, Integer> ranks = new HashMap<>();
            ranks.put("Squire", 1);
            ranks.put("Knight", 2);
            ranks.put("Champion Knight", 3);
            ranks.put("Knight of the Round Table", 4);

            //GETTING THE LOW RANK PLAYERS AND STORING IT IN A LIST
            for (int i = 0; i < game.getPlayers().size(); i++) {
                String currRank = game.getPlayers().get(i).getRank();
                if (i == 0) {
                    lowRank = currRank;
                    playerArrayList.add(game.getPlayers().get(i));
                } else {
                    if (ranks.get(currRank) == ranks.get(lowRank)) {
                        playerArrayList.add(game.getPlayers().get(i));
                    } else if (ranks.get(currRank) < ranks.get(lowRank)) {
                        playerArrayList = new ArrayList<>();
                        playerArrayList.add(game.getPlayers().get(i));
                        lowRank = currRank;
                    }
                }
            }
            leastNumShields = playerArrayList.get(0).getShields();
            for (int i=1; i < playerArrayList.size(); i++){
                if(leastNumShields>playerArrayList.get(i).getShields()){
                   playerArrayList.remove(i);
                   i--;
                }
            }

            for (int i=0 ; i<playerArrayList.size();i++){
                playerArrayList.get(i).setShields(playerArrayList.get(i).getShields() + 3);
                playerArrayList.get(i).setRank();
                //SEND MESSAGE TO UPDATE PLAYER RANK (IF CHANGED)
            }
        }
        //All players except player drawing this card lose 1 shield
        else if(game.getCurrentStoryCard() instanceof Pox){
            for(int i = 0; i < game.getPlayers().size(); i++){
                if(!game.getMainPlayer().getUsername().equals(game.getPlayers().get(i).getUsername())){
                    if(game.getPlayers().get(i).getShields() >= 1){
                        game.getPlayers().get(i).setShields(game.getPlayers().get(i).getShields()-1);
                        //Send simp message to player
                    }
                }
            }
        }
        //Drawer loses 2 shields if possible
        else if(game.getCurrentStoryCard() instanceof Plague){
            if(game.getMainPlayer().getShields() >= 2){
                game.getMainPlayer().setShields(game.getMainPlayer().getShields()-2);
                //Send simp message to player
            }
        }
        else if(game.getCurrentStoryCard() instanceof KingsRecognition){
            //FIND THE HIGHEST RANK PLAYER
            //THEN SEND MESSAGE TO THAT PLAYER TO DISCARD ONE OF THEIR WEAPON CARDS
            //IF THAT CANNOT BE DONE - THEN DISCARD 2 FOE CARD

            //QUESTION ?? WHAT HAPPENS IF THE PLAYER DOES NOT HAVE 2 FOE CARDS?
            //ASSUMPTION - IF THE PLAYER HAS LESS THAN 2 CARDS, DISCARD WHATEVER YOU HAVE I.E., THE ONE FOE CARD

            ArrayList<Player> highRankingPlayers = new ArrayList<>();
            HashMap<String, Integer> ranks = new HashMap<>();
            ranks.put("Squire",1);
            ranks.put("Knight",2);
            ranks.put("Champion Knight",3);
            ranks.put("Knight of the Round Table",4);

            int highestCurrRank =  1;

            for (int i =0; i<game.getPlayers().size(); i++){
                if(highestCurrRank == ranks.get(game.getPlayers().get(i).getRank())){
                    highRankingPlayers.add(game.getPlayers().get(i));
                }
                else if(highestCurrRank > ranks.get(game.getPlayers().get(i).getRank())){
                    highRankingPlayers = new ArrayList<>();
                    highestCurrRank = ranks.get(game.getPlayers().get(i).getRank());
                    highRankingPlayers.add(game.getPlayers().get(i));
                }
            }

            int numFoes = 0;
            HashMap<Player,Integer> numFoeCardsForHighPlayers = new HashMap<>();
            boolean flag = false;
            for (int i=0 ; i<highRankingPlayers.size(); i++){
                numFoeCardsForHighPlayers.put(highRankingPlayers.get(i),numFoes);
                for (int j=0; j<highRankingPlayers.get(i).getCards().size(); j++){
                    if (highRankingPlayers.get(i).getCards().get(j).getType().equals("Weapon")){
                        //TODO
                        //SEND MESSAGE TO USER TO DISCARD A WEAPON CARD
                        //simpMessagingTemplate.convertAndSendToUser();
                        //flag = true;
                        //break
                    }
                    else if (highRankingPlayers.get(i).getCards().get(j).getType().equals("Foe")){
                        numFoes ++;
                        numFoeCardsForHighPlayers.replace(highRankingPlayers.get(i),numFoes);
                    }
                }
                numFoes = 0;
            }

            if (flag){
                //TODO
                //DO SOMETHING TO HANDLE WEAPON CARDS
                //SEND SIMPMESSAGETEMPLATE
            }
            else {
                for (int i = 0; i < highRankingPlayers.size(); i++) {
                    if (numFoeCardsForHighPlayers.get(highRankingPlayers.get(i)) > 2) {
                        //TODO
                        //DISCARD 2 FOE CARDS
                        //SEND MESSAGE TO USER TO DISCARD 2 FOE CARDS
                    } else {
                        //SEND MESSAGE TO USER THAT WE DISCARDED YOUR FOE CARD!
                        for (int j = 0; j < highRankingPlayers.get(i).getCards().size(); j++) {
                            if (highRankingPlayers.get(i).getCards().get(j).getType().equals("Foe")) {
                                highRankingPlayers.get(i).getCards().remove(j);
                                j--;
                            }
                        }
                        simpMessagingTemplate.convertAndSendToUser(highRankingPlayers.get(i).getName(),
                                "/topic/cards-in-hand/" + game.getGameId(), highRankingPlayers.get(i).getCards());
                    }
                }
            }
        }
        //Lowest ranked player(s) draw 2 adventure cards
        else if(game.getCurrentStoryCard() instanceof QueensFavor){
            //Get lowest ranked player(s)
            ArrayList<Player> lowestRankedPlayers = new ArrayList<>();
            int lowestRank = 99999;
            for(int i = 0; i < game.getPlayers().size(); i++){
                if(game.getPlayers().get(i).getBattlePoints() < lowestRank){
                    lowestRankedPlayers.clear();
                    lowestRankedPlayers.add(game.getPlayers().get(i));
                    lowestRank = game.getPlayers().get(i).getBattlePoints();
                }
                else if(game.getPlayers().get(i).getBattlePoints().equals(lowestRank)){
                    lowestRankedPlayers.add(game.getPlayers().get(i));
                }
            }
            //Give them 2 cards each
            for(int i = 0; i < lowestRankedPlayers.size(); i++){
                Card c1 = game.getAdventureDeck().drawCard();
                Card c2 = game.getAdventureDeck().drawCard();
                lowestRankedPlayers.get(i).getCards().add(c1); lowestRankedPlayers.get(i).getCards().add(c2);
                simpMessagingTemplate.convertAndSendToUser(lowestRankedPlayers.get(i).getName(),
                        "/topic/cards-in-hand/"+game.getGameId(), lowestRankedPlayers.get(i).getCards());
            }
        }
        //All allies in play must be discarded
        else if(game.getCurrentStoryCard() instanceof CourtCamelot){
            for(int i =0; i < game.getPlayers().size(); i++){
                game.getPlayers().get(i).setAllies(new ArrayList<>());
                //Send Simp Message to the player with updated allies (i.e. none)
            }
        }
        else if(game.getCurrentStoryCard() instanceof KingsArms){
            game.setBonusShield(2);
        }
        //All players draw 2 adventure cards
        else if(game.getCurrentStoryCard() instanceof ProsperityRealm){
            for(int i = 0; i < game.getPlayers().size(); i++){
                Card c1 = game.getAdventureDeck().drawCard();
                Card c2 = game.getAdventureDeck().drawCard();
                game.getPlayers().get(i).getCards().add(c1); game.getPlayers().get(i).getCards().add(c2);
                simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(i).getName(),
                        "/topic/cards-in-hand/"+game.getGameId(), game.getPlayers().get(i).getCards());
            }
        }
    }

}
