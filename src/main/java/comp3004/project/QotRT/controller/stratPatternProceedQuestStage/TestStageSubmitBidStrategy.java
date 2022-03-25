package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class TestStageSubmitBidStrategy implements ProceedQuestStageStrategy{
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();

    @Override
    public void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player) {
        String gameId = game.getGameId();
        int index = 0;
        for(int i = 0; i < game.getQuestingPlayers().size();i++){
            if(game.getQuestingPlayers().get(i).getUsername().equals(player.getUsername())){
                index = i;
            }
        }
        int playerBid = player.getBid();
        if(index == game.getQuestingPlayers().size()-1 ){
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(index).getName(),
                    "/topic/fulfill-bid/"+gameId, playerBid);
        }else{
            game.getQuestingPlayers().get(index).setStatus("waiting");
            game.getQuestingPlayers().get(index+1).setStatus("current");
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(index+1).getName(),
                    "/topic/play-against-test-stage/"+gameId, playerBid);
        }
    }
}
