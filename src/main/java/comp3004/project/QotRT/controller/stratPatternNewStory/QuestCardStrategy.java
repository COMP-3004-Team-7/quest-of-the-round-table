package comp3004.project.QotRT.controller.stratPatternNewStory;

import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.model.Game;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

public class QuestCardStrategy implements NewStoryCardStrategy{

    @Override
    public void dealWithNewStoryCard(Game game, SimpMessagingTemplate simpMessagingTemplate) {
        //Updating main player
        int indexOfNewMain = (game.getPlayers().indexOf(game.getMainPlayer())+1)%game.getPlayers().size();
        game.setMainPlayer(game.getPlayers().get(indexOfNewMain));
        //Update player statuses
        for(int i = 0; i < game.getPlayers().size();i++){
            if(game.getPlayers().get(i).equals(game.getMainPlayer())){
                game.getPlayers().get(i).setStatus("current");
            }
            else{
                game.getPlayers().get(i).setStatus("waiting");
            }
        }
        //Set questing players to empty before starting
        game.setQuestingPlayers(new ArrayList<>());

        simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+game.getGameId(), game.getCurrentStoryCard());
        simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),"/topic/sponsor-quest/"+game.getGameId(),game.getCurrentStoryCard());
    }
}
