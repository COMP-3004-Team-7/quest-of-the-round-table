package comp3004.project.QotRT.controller.stratPatternBattlePoints;

import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class AllyBattlePointsOrBidsStrategy implements GetBattlePointsOrBidsStrategy{

    @Override
    public int getBattlePoints(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player) {
        int totalAllyPoints = 0;
        for(int i = 0; i < player.getAllies().size(); i++){
            if(player.getAllies().get(i).getName().equals("Sir Gawain")){
                totalAllyPoints += 10;
                if(game.getCurrentStoryCard().getName().equals("Test of the Green Knight")){
                    totalAllyPoints += 20;
                }
            }
            if(player.getAllies().get(i).getName().equals("King Pellinore")){
                totalAllyPoints += 10;
            }
            if(player.getAllies().get(i).getName().equals("Sir Percival")){
                totalAllyPoints += 5;
                if(game.getCurrentStoryCard().getName().equals("Search for the Holy Grail")){
                    totalAllyPoints += 20;
                }
            }
            if(player.getAllies().get(i).getName().equals("Sir Tristan")){
                totalAllyPoints += 10;
                if(iseultInPlay(game)){
                    totalAllyPoints += 20;
                }
            }
            if(player.getAllies().get(i).getName().equals("King Arthur")){
                totalAllyPoints += 10;
            }
            if(player.getAllies().get(i).getName().equals("Sir Lancelot")){
                totalAllyPoints += 15;
                if(game.getCurrentStoryCard().getName().equals("Defend the Queen's Honour")){
                    totalAllyPoints += 20;
                }
            }
            if(player.getAllies().get(i).getName().equals("Sir Galahad")){
                totalAllyPoints += 15;
            }
        }
        return totalAllyPoints;
    }

    @Override
    public int getBids(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player) {
        int totalBids = 0;
        for(int i = 0; i < player.getAllies().size(); i++){
            if(player.getAllies().get(i).getName().equals("Queen Iseult")){
                totalBids += 2;
                if(sirTristanInPlay(game)){
                    totalBids += 2;
                }
            }
            if(player.getAllies().get(i).getName().equals("Queen Guinevere")){
                totalBids += 3;
            }
            if(player.getAllies().get(i).getName().equals("King Pellinore")){
                if(game.getCurrentStoryCard().getName().equals("Search for the Questing Beast")){
                    totalBids += 4;
                }
            }
            if(player.getAllies().get(i).getName().equals("King Arthur")){
                totalBids += 2;
            }
        }
        return totalBids;
    }


    //HELPER METHODS
    public Boolean iseultInPlay(Game game){
        for(int i = 0; i < game.getPlayers().size(); i++){
            for(int j = 0; j < game.getPlayers().get(i).getAllies().size(); j++){
                if(game.getPlayers().get(i).getAllies().get(j).getName().equals("Queen Iseult")){
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean sirTristanInPlay(Game game){
        for(int i = 0; i < game.getPlayers().size(); i++){
            for(int j = 0; j < game.getPlayers().get(i).getAllies().size(); j++){
                if(game.getPlayers().get(i).getAllies().get(j).getName().equals("Sir Tristan")){
                    return true;
                }
            }
        }
        return false;
    }
}
