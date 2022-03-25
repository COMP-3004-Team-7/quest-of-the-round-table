package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class TestStageDeclineBidStrategy implements ProceedQuestStageStrategy{
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();

    @Override
    public void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player) {
        String gameId = game.getGameId();
        //Get player who sent request
        int index = 0;
        int currentMaxBid = getCurrentMaxBid(game, stage);
        for(int i = 0; i < game.getQuestingPlayers().size(); i++){
            if(game.getQuestingPlayers().get(i).getUsername().equals(player.getUsername())){
                index = i;
            }
        }
        //Check if this is the last person in the quest to submit their response to the test card
        if( index == game.getQuestingPlayers().size()-1 ){
            Player removedPlayer = game.getQuestingPlayers().remove(index);
            //nobody left -> end quest, no rewards, pull new story card
            if(game.getQuestingPlayers().size()==0){
                drawCardsForSponsor(game);
                //Put all cards in quest in discard pile
                for(int i = 1; i < 6; i++){
                    for(int j = 0; j < game.getStage(i).size(); j++){
                        game.getAdventureDeck().discardCard(game.getStage(i).get(j));
                    }
                }
                simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                        game.getMainPlayer().getName(), game.getMainPlayer().getCards());
                newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
            }
            //Only person left in questing array will advance to next stage (unless this is last stage)
            else{
                //Make them fulfill the test by discarding cards
                removedPlayer.setStatus("waiting");
                game.getQuestingPlayers().get(0).setStatus("current");
                simpMessagingTemplate.convertAndSend("/topic/fulfill-bid/" + gameId + "/" +
                        game.getQuestingPlayers().get(0).getName(), game.getQuestingPlayers().get(0).getBid());
            }
        }
        //Ask next person to bid, then remove this player from questing array
        else{
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(index+1).getName(),
                    "/topic/play-against-test-stage/"+gameId, currentMaxBid);
            game.getQuestingPlayers().remove(index);
        }
    }

    private int getCurrentMaxBid(Game game, int stage){
        int numbids = game.getStage(stage).get(0).getBids();
        if(game.getStage(stage).get(0).getName().equals("Test of Questing Beast")){
            if(!game.getCurrentStoryCard().getName().equals("Search for the Questing Beast")){
                numbids = 2;
            }
        }
        for(int i = 0; i < game.getQuestingPlayers().size();i++){

            int bid = game.getQuestingPlayers().get(i).getBid();
            if(bid > numbids){
                numbids = bid;
            }
        }
        return numbids;
    }

    private void drawCardsForSponsor(Game game) {
        int numStages = game.getCurrentStoryCard().getStages();
        int numCardsPlayed = 0;
        for(int i = 0; i < numStages; i++){
            for(int j = 0; j < game.getStage(i).size(); j++){
                numCardsPlayed++;
            }
        }
        for(int i = 0; i < numStages+numCardsPlayed; i++){
            Card card = game.getAdventureDeck().drawCard();
            game.getMainPlayer().getCards().add(card);
        }
    }
}
