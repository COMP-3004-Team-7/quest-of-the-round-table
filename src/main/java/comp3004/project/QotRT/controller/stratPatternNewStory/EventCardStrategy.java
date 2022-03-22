package comp3004.project.QotRT.controller.stratPatternNewStory;

import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.service.EventService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class EventCardStrategy implements NewStoryCardStrategy{
    private final EventService eventService = new EventService();

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
        simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+game.getGameId(), game.getCurrentStoryCard());
        eventService.doEvent(game, simpMessagingTemplate);
    }
}
