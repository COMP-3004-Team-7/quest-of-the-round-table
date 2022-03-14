package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.*;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

public class EventService {

    public void doEvent(Game game, SimpMessagingTemplate simpMessagingTemplate){
        if(game.getCurrentStoryCard() instanceof ChivalrousDeed){

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
