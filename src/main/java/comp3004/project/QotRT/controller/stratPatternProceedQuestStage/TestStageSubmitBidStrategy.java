package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AllyBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AmourBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.BattlePointsOrBidsReceiver;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

public class TestStageSubmitBidStrategy implements ProceedQuestStageStrategy{
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();
    private final BattlePointsOrBidsReceiver battlePointsOrBidsReceiver = new BattlePointsOrBidsReceiver();

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
            //Subtract cards to discard by amour bids
            battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AmourBattlePointsOrBidsStrategy());
            cardsToDiscard -= battlePointsOrBidsReceiver.receiveBids(game, simpMessagingTemplate, game.getQuestingPlayers().get(index));
            //Subtract cards to discard by ally bids
            battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
            cardsToDiscard -= battlePointsOrBidsReceiver.receiveBids(game, simpMessagingTemplate, game.getQuestingPlayers().get(index));
            //If cards to discard is 0 or less, than player doesn't have to discard -> award shields and new story card is drawn
            //Or just proceed with next quest stage
            if(cardsToDiscard <= 0) {
                //Last stage was this Test card
                if(game.getCurrentStoryCard().getStages() == stage){
                    Player p = game.getQuestingPlayers().get(0);
                    int numShields = game.getBonusShield() + game.getCurrentStoryCard().getStages();
                    p.setShields(p.getShields()+numShields);
                    game.setBonusShield(0);
                    if(p.getRank().equals("Knight")){
                        simpMessagingTemplate.convertAndSend("/topic/game-winner/" + gameId, p.getUsername() +" won the game!");
                    }
                    else {
                        //Remove amour cards and weapon cards from player (end of quest), send message, and draw new story card
                        removeAmourCards(game, p);
                        simpMessagingTemplate.convertAndSend("/topic/quest-winner/"+gameId+"/"+
                                        p.getName(),
                                "You won the quest and were awarded " + numShields + " shields!" );
                        newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
                    }
                }
                //Not last stage -> move the only person who passed onto the next stage
                else{
                    sendNextStageToQuestingPlayer(game.getGameId(), simpMessagingTemplate, game, stage);
                }
            }
            else {
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

    private void sendNextStageToQuestingPlayer(String gameId, SimpMessagingTemplate simpMessagingTemplate, Game game, int stage) {
        //Update players
        for (int i=0 ; i < game.getQuestingPlayers().size(); i++){
            game.getPlayers().get(i).setStatus("current");
            Card card = game.getAdventureDeck().drawCard();
            game.getQuestingPlayers().get(i).getCards().add(card);
            simpMessagingTemplate.convertAndSend("/topic/play-against-quest-stage/"+gameId+"/"+
                    game.getQuestingPlayers().get(i).getName(), game.getStage(stage).get(0));
            simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                    game.getQuestingPlayers().get(i).getName(), game.getQuestingPlayers().get(i).getCards());
        }
    }
}
