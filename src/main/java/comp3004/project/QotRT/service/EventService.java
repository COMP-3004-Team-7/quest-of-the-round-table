package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.model.Game;

public class EventService {

    public void doEvent(Game game){
        if(game.getCurrentStoryCard().getName().equals("Chivalrous Deed")){

        }
        //All players except player drawing this card lose 1 shield
        else if(game.getCurrentStoryCard().getName().equals("Pox")){
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
        else if(game.getCurrentStoryCard().getName().equals("Plague")){
            if(game.getMainPlayer().getShields() >= 2){
                game.getMainPlayer().setShields(game.getMainPlayer().getShields()-2);
                //Send simp message to player
            }
        }
        else if(game.getCurrentStoryCard().getName().equals("King's Recognition")){

        }
        else if(game.getCurrentStoryCard().getName().equals("Queen's Favour")){

        }
        else if(game.getCurrentStoryCard().getName().equals("Court Called to Camelot")){

        }
        else if(game.getCurrentStoryCard().getName().equals("King's Call to Arms")){

        }
        else if(game.getCurrentStoryCard().getName().equals("Prosperity Throughout the Realm")){

        }
    }

}
