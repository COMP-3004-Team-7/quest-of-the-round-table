package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.SelectSponsorCardRequest;
import comp3004.project.QotRT.controller.dto.SubmitStageRequest;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class QuestService {
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();

    //Player has chosen to decline to sponsor the Quest
    public String declineSponsorQuest(String gameId , ConnectRequest request , SimpMessagingTemplate simpMessagingTemplate, GameService gameService){
        Game game = gameService.getGame(gameId);
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i=0; i<game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                index = i;
                break;
            }
        }
        // Checking if everyone declines Quest
        // Mod 4 for each index -> circular array
        if ((index+1) %sizeOfPlayersList == game.getPlayers().indexOf(game.getMainPlayer())){
            newStoryCardDealer.dealWithNewStoryCard(game, simpMessagingTemplate);

            if(sizeOfPlayersList==2){ //IF the player size is 2 -> then the last player becomes the main player (race condition)
                return "Two player game";
            }
        }
        else{
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get((index+1)%sizeOfPlayersList).getName(),"/topic/sponsor-quest/"+gameId
                    ,game.getCurrentStoryCard());
        }
        return "";
    }

    //Player has chosen to sponsor the Quest
    public StoryCard sponsorQuest(String gameId , ConnectRequest request , SimpMessagingTemplate simpMessagingTemplate, GameService gameService){
        //Get player who sponsored the quest -> update them to be the main player
        Game game = gameService.getGame(gameId);
        for (int i=0; i<game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                game.setMainPlayer(game.getPlayers().get(i));
                break;
            }
        }
        //Return back to the sponsorer the story card that the sponsored
        return game.getCurrentStoryCard();
    }

    //Player has selected a Card for a stage to build
    public ResponseEntity selectCardForSponsorStage(String gameId, SelectSponsorCardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Error checking -> if this is first card submitted for this stage number, then check if it is a foe
        //Otherwise we check if it is a weapon card and check if there are any duplicate weapons
        Game game = gameService.getGame(gameId);

        if(game.getStage(request.getStage()).isEmpty()){
            if(!request.getCard().getType().equals("Foe") && (!request.getCard().getType().equals("Test"))){
                return ResponseEntity.badRequest().body("Must submit Foe Card or Test First ");
            }
        }
        else{
            if(game.getStage(request.getStage()).get(0).getType().equals("Foe")){
                if(!request.getCard().getType().equals("Weapon")){
                    return ResponseEntity.badRequest().body("Must supplement Foe card with Weapon Cards Only");
                }
                else{
                    //Check for duplicate weapons
                    for(int i = 0; i < game.getStage(request.getStage()).size(); i++){
                        if(game.getStage(request.getStage()).get(i).getName().equals(request.getCard().getName())){
                            return ResponseEntity.badRequest().body("Cannot submit duplicate weapon in same stage");
                        }
                    }
                }
            }else{
                return ResponseEntity.badRequest().body("Can't add a card to a test card");
            }

        }
        //If we get here then no errors have occurred, and we can add card to current stage
        game.addCardToStage(request.getCard(), request.getStage());

        //Update players hand
        for (int i=0; i<game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                Player p = game.getPlayers().get(i);
                for(int k = 0; k < p.getCards().size(); k++){
                    if(p.getCards().get(k).getName().equals(request.getCard().getName())){
                        p.getCards().remove(k);
                        break;
                    }
                }
                //Send back updated player hand
                simpMessagingTemplate.convertAndSendToUser(p.getName(),"/topic/cards-in-hand/"+gameId
                        ,p.getCards());
                break;
            }
        }

        return ResponseEntity.ok().body("Successfully added a card to stage " + request.getStage());
    }


    public ResponseEntity submitSponsorStage(String gameId, SubmitStageRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Check if current stage is greater than all previous stages battlepoints
        Boolean isBigger = true;
        Game game = gameService.getGame(gameId);
        int numStages = game.getCurrentStoryCard().getStages();
        int totalBattlePointsInSubmittedStage = 0;
        for(int i = 0; i < game.getStage(request.getStage()).size(); i++){
            //Checking if we should use Min vs Max battlepoints
            if(game.getCurrentStoryCard().getFoevalue().equals("All")){
                totalBattlePointsInSubmittedStage += game.getStage(request.getStage()).get(i).getMAXbattlepoints();
            }
            else if(game.getCurrentStoryCard().getFoevalue().equals("All Saxons")
                    && game.getStage(request.getStage()).get(i).getName().contains("Saxon")){
                totalBattlePointsInSubmittedStage += game.getStage(request.getStage()).get(i).getMAXbattlepoints();
            }
            else if(game.getCurrentStoryCard().getFoevalue().equals(game.getStage(request.getStage()).get(i).getName())){
                totalBattlePointsInSubmittedStage += game.getStage(request.getStage()).get(i).getMAXbattlepoints();
            }
            else{
                totalBattlePointsInSubmittedStage += game.getStage(request.getStage()).get(i).getMINbattlepoints();
            }
        }

        for(int i = 1; i < numStages+1; i++){
            int numCardsInStage = game.getStage(i).size();
            int totalBattlePointsInStage = 0;
            for(int j = 0; j < numCardsInStage; j++){
                //NEED LOGIC FOR MIN OR MAX BATTLEPOINTS
                totalBattlePointsInStage += game.getStage(i).get(j).getMINbattlepoints();
            }
            if(totalBattlePointsInStage > totalBattlePointsInSubmittedStage){
                isBigger = false;
                break;
            }
        }
        //If False, return an error and return cards back to player
        if(!isBigger){
            //send error. update cards back into players hand. send cards back.
            for (int i=0; i<game.getPlayers().size(); i++){
                if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                    Player p = game.getPlayers().get(i);
                    p.getCards().addAll(game.getStage(request.getStage()));
                    //Send back updated player hand
                    simpMessagingTemplate.convertAndSendToUser(p.getName(),"/topic/cards-in-hand/"+gameId
                            ,p.getCards());
                    break;
                }
            }
            return ResponseEntity.badRequest().body("Submitted stage battlepoints lower than previous stages");
        }
        //Otherwise all is good and we can check if it is the last submitted quest or not
        else{
            //Checking if this is the final submitted stage
            if(game.getCurrentStoryCard().getStages() == request.getStage()){
                simpMessagingTemplate.convertAndSend("/topic/quest-build-complete/"+gameId
                        , game.getCurrentStoryCard());
                //Update player statuses and send to next user in line to see if they want to join or not
                int indexToSendTo = game.getPlayers().indexOf(game.getMainPlayer())+1%game.getPlayers().size();
                updatePlayerStatusesClockwise(game, game.getPlayers().indexOf(game.getMainPlayer()));
                simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
                        "/topic/quest-build-complete/"+gameId, game.getCurrentStoryCard());
            }
            //Not last stage --> Ask Player to complete the next quest stage
            else{
                simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),
                        "/topic/build-quest-stage/"+gameId, request.getStage()+1);
            }
        }

        return ResponseEntity.ok().body(request.getStage()+1);
    }

    public ResponseEntity joinCurrentQuest(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Add player to the current questing array
        Game game = gameService.getGame(gameId);
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                game.addQuestingPlayer(game.getPlayers().get(i));
                index = i;
                break;
            }
        }
        //Check if this is the last person to join the quest
        if((index+1) % sizeOfPlayersList == game.getPlayers().indexOf(game.getMainPlayer())) {
            //Give 1 card to each player currently on quest, update each player on current quest to 'current'
            //Alert to all players currently on the quest whether first stage is Test or Foe
            //and ask them to select cards to play on the quest
            for(int i = 0; i < game.getQuestingPlayers().size();i++){
                Card card = game.getAdventureDeck().drawCard();
                game.getQuestingPlayers().get(i).getCards().add(card);
                game.getQuestingPlayers().get(i).setStatus("current");
                simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                        "/topic/play-against-quest-stage/"+gameId, game.getStage(1).get(0).getType());
                simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                        "/topic/cards-in-hand/"+gameId, game.getQuestingPlayers().get(i).getCards());
            }
        }
        else{
            //Send to the next person asking them if they wish to join the quest
            int indexToSendTo = index+1%game.getPlayers().size();
            updatePlayerStatusesClockwise(game, index);
//            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
//                        "/topic/cards-in-hand/"+gameId, game.getPlayers().get(indexToSendTo).getCards());
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
                    "/topic/quest-build-complete/"+gameId, game.getCurrentStoryCard());
        }

        return ResponseEntity.ok().body("Successfully joined the quest!");
    }

    public ResponseEntity declineToJoinCurrentQuest(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Check if this is the last person to join the quest (also update the decliner to 'waiting')
        Game game = gameService.getGame(gameId);
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                game.getPlayers().get(i).setStatus("waiting");
                index = i;
                break;
            }
        }
        if((index+1) % sizeOfPlayersList == game.getPlayers().indexOf(game.getMainPlayer())) {
            //Checking if nobody joined quest -> main player draws card, update main player, draw new story card
            if(game.getQuestingPlayers().size()==0){
                drawCardsForSponsor(game);
                //Put all cards in quest in discard pile
                for(int i = 1; i < 6; i++){
                    for(int j = 0; j < game.getStage(i).size(); j++){
                        game.getAdventureDeck().discardCard(game.getStage(i).get(j));
                    }
                }
                simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),
                        "/topic/cards-in-hand/"+gameId, game.getMainPlayer().getCards());
                newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
            }
            else {
                //1 card to each current questing player, send whether 1st stage is Foe or Test, send updated hands
                sendNextStageToQuestingPlayer(gameId, simpMessagingTemplate, game, 1);
            }
        }
        else{
            //Send to the next person asking them if they wish to join the quest
            int indexToSendTo = index+1%game.getPlayers().size();
            updatePlayerStatusesClockwise(game, index);
//            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
//                    "/topic/cards-in-hand/"+gameId, game.getPlayers().get(indexToSendTo).getCards());
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
                    "/topic/quest-build-complete/"+gameId, game.getCurrentStoryCard());
        }

        return ResponseEntity.ok().body("Successfully declined to join quest!");
    }

    public ResponseEntity submitCardAgainstFoe(String gameId, SelectSponsorCardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Error checking (is card test or foe or any other nonplayable cards?)
        Game game = gameService.getGame(gameId);
        if(request.getCard().getName().equals("Foe") || request.getCard().getName().equals("Test")){
            return ResponseEntity.badRequest().body("Must submit weapon/ally/amour");
        }

        //Check for duplicate weapons
        for(int i = 0; i < game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                Player p = game.getPlayers().get(i);
                for(int j = 0; j < p.getWeaponCardsPlayed().size(); j++){
                    if(p.getWeaponCardsPlayed().get(j).getName().equals(request.getCard().getName())){
                        return ResponseEntity.badRequest().body("Cannot submit duplicate weapons");
                    }
                }
                //Otherwise, all good and we add weapon card to players played cards
                p.getWeaponCardsPlayed().add(request.getCard());
                break;
            }
        }

        return ResponseEntity.ok().body("Submitted card to battle the foe");
    }

    public ResponseEntity completeCardsPlayedAgainstFoe(String gameId, SubmitStageRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Update this player to 'waiting'
        Game game = gameService.getGame(gameId);
        for(int i = 0; i < game.getQuestingPlayers().size(); i++){
            if(game.getQuestingPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
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
            int foeStagePoints = 0;
            for (int i=0 ; i<game.getStage(request.getStage()).size();i++){
                //CHECK TO SEE IF THE MAX BATTLE POINT NEEDS TO BE USED OR MIN
                //Checking if we should use Min vs Max battlepoints
                if(game.getCurrentStoryCard().getFoevalue().equals("All")){
                    foeStagePoints += game.getStage(request.getStage()).get(i).getMAXbattlepoints();
                }
                else if(game.getCurrentStoryCard().getFoevalue().equals("All Saxons")
                        && game.getStage(request.getStage()).get(i).getName().contains("Saxon")){
                    foeStagePoints += game.getStage(request.getStage()).get(i).getMAXbattlepoints();
                }
                else if(game.getCurrentStoryCard().getFoevalue().equals(game.getStage(request.getStage()).get(i).getName())){
                    foeStagePoints += game.getStage(request.getStage()).get(i).getMAXbattlepoints();
                }
                else{
                    foeStagePoints += game.getStage(request.getStage()).get(i).getMINbattlepoints();
                }
            }
            //COMPARING THE WEAPONS CARD PLAYED TO THE TOTAL FOE BATTLE POINTS OF THE CURRENT STAGE
            int weaponCardsPlayed = 0;
            for(int i=0; i<game.getQuestingPlayers().size(); i++){
                for(int j=0 ; j<game.getQuestingPlayers().get(i).getWeaponCardsPlayed().size(); j++) {
                    weaponCardsPlayed += game.getQuestingPlayers().get(i).getWeaponCardsPlayed().get(i).getMAXbattlepoints();
                    if(weaponCardsPlayed>=foeStagePoints){
                        //THIS PLAYER HAS MOVED ON TO THE NEXT STAGE
                        break;
                    }
                }
                if(weaponCardsPlayed<foeStagePoints){
                    //THIS PLAYER IS ELIMINATED FROM THE QUEST -> REMOVED FROM THE QUESTINGPLAYERSLIST
                    //SEND SIMPMESSAGING TEMPLATE TO THE USER THAT HAVE BEEN REMOVED FROM THE LIST
                    removeWeaponCards(game, game.getQuestingPlayers().get(i));
                    game.getQuestingPlayers().remove(i);
                    i--;
                }
                weaponCardsPlayed = 0;
            }
            if(game.getCurrentStoryCard().getStages() == request.getStage()){
             //TODO
                //CHECK IF PLAYER(S) HAVE WON
                //OTHERWISE, DRAW ANOTHER STORY CARD
                for (int i=0; i < game.getQuestingPlayers().size(); i++){
                    game.getQuestingPlayers().get(i).setShields(request.getStage()+game.getBonusShield());
                    game.setBonusShield(0);
                    game.getQuestingPlayers().get(i).setRank();
                    removeWeaponCards(game, game.getQuestingPlayers().get(i));
                }
                drawCardsForSponsor(game);
            }
            else{
                sendNextStageToQuestingPlayer(gameId, simpMessagingTemplate, game, request.getStage());
            }
        }
        return ResponseEntity.ok().body("Success");
    }


    //HELPER METHODS

    private void sendNextStageToQuestingPlayer(String gameId, SimpMessagingTemplate simpMessagingTemplate, Game game, int stage) {
        //Update players (everyone except main player gets set to current)
        for(int i = 0; i < game.getPlayers().size();i++){
            if(game.getPlayers().get(i).equals(game.getMainPlayer())){
                game.getPlayers().get(i).setStatus("waiting");
            }
            else{
                game.getPlayers().get(i).setStatus("current");
            }
        }
        for (int i=0 ; i < game.getQuestingPlayers().size(); i++){
            Card card = game.getAdventureDeck().drawCard();
            game.getQuestingPlayers().get(i).getCards().add(card);
            simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                    "/topic/play-against-quest-stage/"+gameId, game.getStage(stage).get(0));
            simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                    "/topic/cards-in-hand/"+gameId, game.getQuestingPlayers().get(i).getCards());
        }
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

    private void updatePlayerStatuses(Game game) {
        //Quest card -> Main player is current player
        if(game.getCurrentStoryCard().getType().equals("Quest")){
            for(int i = 0; i < game.getPlayers().size();i++){
                if(game.getPlayers().get(i).equals(game.getMainPlayer())){
                    game.getPlayers().get(i).setStatus("current");
                }
                else{
                    game.getPlayers().get(i).setStatus("waiting");
                }
            }
        }
        //Event card -> set everyone to waiting (Event Service will change players to current if they need to do something)
        else if(game.getCurrentStoryCard().getType().equals("Event")){
            for(int i = 0; i < game.getPlayers().size();i++){
                game.getPlayers().get(i).setStatus("waiting");
            }
        }
    }

    private void updatePlayerStatusesClockwise(Game game, int indexOfWaiting) {
        int indexOfNextCurrent = indexOfWaiting+1%game.getPlayers().size();
        game.getPlayers().get(indexOfWaiting).setStatus("waiting");
        game.getPlayers().get(indexOfNextCurrent).setStatus("current");
    }

    private void removeWeaponCards(Game game, Player p){
        for(Card c: p.getWeaponCardsPlayed()){
            game.getAdventureDeck().discardCard(c);
        }
        p.setWeaponCardsPlayed(new ArrayList<>());
    }
}
