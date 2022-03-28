package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

public class TestStageSubmitBidStrategy implements ProceedQuestStageStrategy{
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();

    @Override
    public void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player) {
        String gameId = game.getGameId();
        //Get index of player and remove previous players from questing array and remove previous players weapons and amours
        int index = 0;
        for(int i = 0; i < game.getQuestingPlayers().size();i++){
            if(game.getQuestingPlayers().get(i).getUsername().equals(player.getUsername())){
                index = i;
                break;
            }
            removeAmourCards(game, game.getQuestingPlayers().get(i));
            removeWeaponCards(game, game.getQuestingPlayers().get(i));
            game.getQuestingPlayers().remove(i);
            i--;
        }
        int playerBid = player.getBid();

        if(index == game.getQuestingPlayers().size()-1 ){
            int cardsToDiscard = playerBid;
            if(game.getQuestingPlayers().get(index).getAmours().size() == 1){
                cardsToDiscard -= game.getQuestingPlayers().get(index).getAmours().get(0).getBids();
            }
            //Todo deal with ally bids
            if(cardsToDiscard <= 0) {
                //Todo deal with new story card if test passes without needing to discard
            }
            else {
                removeWeaponCards(game, game.getQuestingPlayers().get(index));
                simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(index).getName(),
                        "/topic/fulfill-bid/" + gameId, cardsToDiscard);
            }
        }else{
            game.getQuestingPlayers().get(index).setStatus("waiting");
            game.getQuestingPlayers().get(index+1).setStatus("current");
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(index+1).getName(),
                    "/topic/play-against-test-stage/"+gameId, playerBid);
        }
    }


    //HELPER METHODS
    private void removeWeaponCards(Game game, Player p){
        for(Card c: p.getWeaponCardsPlayed()){
            game.getAdventureDeck().discardCard(c);
        }
        p.setWeaponCardsPlayed(new ArrayList<>());
    }

    private void removeAmourCards(Game game, Player p){
        for(Card c: p.getAmours()){
            game.getAdventureDeck().discardCard(c);
        }
        p.setAmours(new ArrayList<>());
    }
}
