package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AllyBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AmourBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.BattlePointsOrBidsReceiver;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;

public class FoeStageStrategy implements ProceedQuestStageStrategy{
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();
    private final BattlePointsOrBidsReceiver battlePointsOrBidsReceiver = new BattlePointsOrBidsReceiver();

    @Override
    public void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player) {
        String gameId = game.getGameId();
        for(int i = 0; i < game.getQuestingPlayers().size(); i++){
            if(game.getQuestingPlayers().get(i).getUsername().equals(player.getUsername())){
                game.getQuestingPlayers().get(i).setStatus("waiting");
            }
        }
        //Check if all questing players have submitted their cards
        int numWaiting = 0;
        for(int i = 0; i < game.getQuestingPlayers().size(); i++){
            if(game.getQuestingPlayers().get(i).getStatus().equals("waiting")){
                numWaiting++;
            }
        }
        if(numWaiting == game.getQuestingPlayers().size()){
            //Check for who moves on to next stage, etc
            //Also check if this is the last stage of quest --> check for winners of quest

            //TOTAL FOE BATTLE POINTS
            int foeStagePoints = getBattlePointsOfStage(game, stage);

            //COMPARING THE WEAPONS CARD PLAYED + RANK TO THE TOTAL FOE BATTLE POINTS OF THE CURRENT STAGE
            int weaponCardsPlayed = 0;
            for(int i=0; i<game.getQuestingPlayers().size(); i++){
                for(int j=0 ; j<game.getQuestingPlayers().get(i).getWeaponCardsPlayed().size(); j++) {
                    weaponCardsPlayed += game.getQuestingPlayers().get(i).getWeaponCardsPlayed().get(j).getMAXbattlepoints();
                }
                int totalBattlePointsOfPlayer = weaponCardsPlayed + game.getQuestingPlayers().get(i).getBattlePoints();
                //Get Total Battle Points from Amours
                battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AmourBattlePointsOrBidsStrategy());
                totalBattlePointsOfPlayer += battlePointsOrBidsReceiver.receiveBattlePoints(game, simpMessagingTemplate, game.getQuestingPlayers().get(i));
                //Get Total Battle Points from Allies
                battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
                totalBattlePointsOfPlayer += battlePointsOrBidsReceiver.receiveBattlePoints(game, simpMessagingTemplate, game.getQuestingPlayers().get(i));

                if(totalBattlePointsOfPlayer<foeStagePoints){
                    //THIS PLAYER IS ELIMINATED FROM THE QUEST -> REMOVED FROM THE QUESTINGPLAYERSLIST
                    //SEND SIMPMESSAGING TEMPLATE TO THE USER THAT HAVE BEEN REMOVED FROM THE LIST
                    removeWeaponCards(game, game.getQuestingPlayers().get(i));
                    removeAmourCards(game, game.getQuestingPlayers().get(i));
                    simpMessagingTemplate.convertAndSend("/topic/quest-eliminated/"+gameId+"/"+game.getQuestingPlayers().remove(i).getName(), "Eliminated from Quest!");
                    i--;
                }
                weaponCardsPlayed = 0;
            }
            //Last Stage of Quest
            if(game.getCurrentStoryCard().getStages() == stage){
                //Give players their shields, check if anyone has reach knighthood, sponsor draws cards
                //check for winners of game, otherwise draw a new story card
                ArrayList<Player> winners = new ArrayList<>();
                for (int i=0; i < game.getQuestingPlayers().size(); i++){
                    int numShields = stage+game.getBonusShield();
                    game.getQuestingPlayers().get(i).setShields(numShields + game.getQuestingPlayers().get(i).getShields());
                    game.setBonusShield(0);
                    game.getQuestingPlayers().get(i).setRank();
                    removeWeaponCards(game, game.getQuestingPlayers().get(i));
                    removeAmourCards(game, game.getQuestingPlayers().get(i));
                    simpMessagingTemplate.convertAndSend("/topic/quest-winner/"+gameId+"/"+
                            game.getQuestingPlayers().get(i).getName(),
                            "You won the quest and were awarded " + numShields + " shields!" );
                    if(game.getQuestingPlayers().get(i).getRank().equals("Knight")){
                        winners.add(game.getQuestingPlayers().get(i));
                    }
                }
                drawCardsForSponsor(game);
                simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                        game.getMainPlayer().getName(), game.getMainPlayer().getCards());
                if(winners.size()>0){
                    String winnerString = "";
                    for (int i = 0; i < winners.size(); i++) {
                        winnerString += winners.get(i).getUsername();
                        if(i == winners.size()-1){
                            winnerString += " ";
                        }
                        else{
                            winnerString += " and ";
                        }
                    }
                    simpMessagingTemplate.convertAndSend("/topic/game-winner/" + gameId, winnerString + "won the game!");
                }
                else{
                    newStoryCardDealer.dealWithNewStoryCard(game, simpMessagingTemplate);
                }
            }
            //Not last stage -> go to next stage
            else{
                sendNextStageToQuestingPlayer(gameId, simpMessagingTemplate, game, stage);
            }
        }
    }

    private int getBattlePointsOfStage(Game game, Integer stage){
        int totalBattlePointsInSubmittedStage = 0;
        for(int i = 0; i < game.getStage(stage).size(); i++){
            //Checking if we should use Min vs Max battlepoints
            if(game.getCurrentStoryCard().getFoevalue().equals("All")){
                totalBattlePointsInSubmittedStage += game.getStage(stage).get(i).getMAXbattlepoints();
            }
            else if(game.getCurrentStoryCard().getFoevalue().equals("All Saxons")
                    && game.getStage(stage).get(i).getName().contains("Saxon")){
                totalBattlePointsInSubmittedStage += game.getStage(stage).get(i).getMAXbattlepoints();
            }
            else if(game.getCurrentStoryCard().getFoevalue().equals(game.getStage(stage).get(i).getName())){
                totalBattlePointsInSubmittedStage += game.getStage(stage).get(i).getMAXbattlepoints();
            }
            else{
                totalBattlePointsInSubmittedStage += game.getStage(stage).get(i).getMINbattlepoints();
            }
        }
        return totalBattlePointsInSubmittedStage;
    }

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

    private void drawCardsForSponsor(Game game) {
        int numStages = game.getCurrentStoryCard().getStages();
        int numCardsPlayed = 0;
        for(int i = 1; i < numStages+1; i++){
            numCardsPlayed += game.getStage(i).size();
        }
        for(int i = 0; i < numStages+numCardsPlayed; i++){
            Card card = game.getAdventureDeck().drawCard();
            game.getMainPlayer().getCards().add(card);
        }
    }

    private void sendNextStageToQuestingPlayer(String gameId, SimpMessagingTemplate simpMessagingTemplate, Game game, int stage) {
        //remove weapon cards
        for (int i=0; i < game.getQuestingPlayers().size(); i++) {
            removeWeaponCards(game, game.getQuestingPlayers().get(i));
        }
        //Update players
        for (int i=0 ; i < game.getQuestingPlayers().size(); i++){
            game.getPlayers().get(i).setStatus("current");
            Card card = game.getAdventureDeck().drawCard();
            game.getQuestingPlayers().get(i).getCards().add(card);
            simpMessagingTemplate.convertAndSend("/topic/fight-quest-stage/"+game.getGameId()+"/"+
                    game.getQuestingPlayers().get(i).getName(), stage +1);
            simpMessagingTemplate.convertAndSend("/topic/play-against-quest-stage/"+gameId+"/"+
                    game.getQuestingPlayers().get(i).getName(), game.getStage(stage).get(0));
            simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                    game.getQuestingPlayers().get(i).getName(), game.getQuestingPlayers().get(i).getCards());
        }
    }
}
