package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.SelectSponsorCardRequest;
import comp3004.project.QotRT.controller.dto.SubmitStageRequest;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class QuestService {

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
            game.getStoryDeck().discardCard(game.getCurrentStoryCard()); //Discarded card
            game.setCurrentStoryCard(game.getStoryDeck().drawCard());

            game.setMainPlayer(game.getPlayers().get((game.getPlayers().indexOf(game.getMainPlayer())+1)%sizeOfPlayersList));
            System.out.println(game.getMainPlayer().getUsername());

            simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+gameId, game.getCurrentStoryCard());

            simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),"/topic/sponsor-quest/"+gameId
                    ,game.getCurrentStoryCard()); // In the future we will have to check if the drawn card is  Quest
                                                // card. This would be done in the Quest Service.

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

    //Player has selected a Card for a stage
    public ResponseEntity selectFoeCardForSponsorStage(String gameId, SelectSponsorCardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Error checking (is card submitted an actual foe card?)
        if(!request.getCard().getType().equals("Foe")){
            return ResponseEntity.badRequest().body("Must submit Foe Card first before weapons");
        }

        //Add card to the current stage
        Game game = gameService.getGame(gameId);
        game.addToSponsoredQuestCards(request.getCard(), request.getStage()-1);

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

        return ResponseEntity.ok().build();
    }

    public ResponseEntity addWeaponToSponsorStage(String gameId, SelectSponsorCardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        Game game = gameService.getGame(gameId);
        //Error checking (is card submitted an actual weapon card?)
        if(!request.getCard().getType().equals("Weapon")){
            return ResponseEntity.badRequest().body("Must submit weapons only to stage that already has a Foe");
        }

        //Has weapon been played already?
        for(int i = 0; i < game.getSponsoredQuestCards()[request.getStage()-1].length; i++){
            if(game.getSponsoredQuestCards()[request.getStage()-1][i].getName()==request.getCard().getName()){
                return ResponseEntity.badRequest().body("Cannot submit duplicate weapon in same stage");
            }
        }

        //Add card to current stage
        game.addToSponsoredQuestCards(request.getCard(), request.getStage()-1);

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

        return ResponseEntity.ok().build();
    }

    public ResponseEntity submitSponsorStage(String gameId, SubmitStageRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Check if current stage is greater than all previous stages battlepoints
        Boolean isBigger = true;
        Game game = gameService.getGame(gameId);
        int numStages = game.getCurrentStoryCard().getStages();
        int totalBattlePointsInSubmittedStage = 0;
        for(int i = 0; i < game.getSponsoredQuestCards()[request.getStage()-1].length; i++){
            //Checking if we should use Min vs Max battlepoints
            if(game.getCurrentStoryCard().getFoevalue().equals("All")){
                totalBattlePointsInSubmittedStage += game.getSponsoredQuestCards()[request.getStage()][i].getMAXbattlepoints();
            }
            else if(game.getCurrentStoryCard().getFoevalue().equals("All Saxons")
                    && game.getSponsoredQuestCards()[request.getStage()][i].getName().contains("Saxon")){
                totalBattlePointsInSubmittedStage += game.getSponsoredQuestCards()[request.getStage()][i].getMAXbattlepoints();
            }
            else if(game.getCurrentStoryCard().getFoevalue().equals(game.getSponsoredQuestCards()[request.getStage()][i].getName()) ){
                totalBattlePointsInSubmittedStage += game.getSponsoredQuestCards()[request.getStage()][i].getMAXbattlepoints();
            }
            else{
                totalBattlePointsInSubmittedStage += game.getSponsoredQuestCards()[request.getStage()][i].getMINbattlepoints();
            }
        }
        ArrayList<Card> cards = new ArrayList<>();
        for(int i = 0; i < numStages-1; i++){
            int numCardsInStage = game.getSponsoredQuestCards()[i].length;
            int totalBattlePointsInStage = 0;
            cards = new ArrayList<>();
            for(int j = 0; j < numCardsInStage; j++){
                totalBattlePointsInStage += game.getSponsoredQuestCards()[i][j].getMINbattlepoints();
                cards.add(game.getSponsoredQuestCards()[i][j]);
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
                    cards.addAll(p.getCards());
                    p.setCards(cards);
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
                int indexToSendTo = game.getPlayers().indexOf(game.getMainPlayer())+1%game.getPlayers().size();
                simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
                        "/topic/quest-build-complete/"+gameId, game.getCurrentStoryCard());
            }
            //Not last stage --> Ask Player to complete the next quest stage
            else{
                simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),
                        "/topic/build-quest-stage/"+gameId, request.getStage()+1);
            }
        }

        return ResponseEntity.ok().build();
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
                simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                        "/topic/play-against-quest-stage/"+gameId, game.getSponsoredQuestCards()[0][0].getType());
                simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                        "/topic/cards-in-hand/"+gameId, game.getQuestingPlayers().get(i).getCards());
            }
        }
        else{
            //Send to the next person asking them if they wish to join the quest
            int indexToSendTo = index+1%game.getPlayers().size();
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
                        "/topic/cards-in-hand/"+gameId, game.getPlayers().get(indexToSendTo).getCards());
        }

        return ResponseEntity.ok().build();
    }

    public ResponseEntity declineToJoinCurrentQuest(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Check if this is the last person to join the quest
        Game game = gameService.getGame(gameId);
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                index = i;
                break;
            }
        }
        if((index+1) % sizeOfPlayersList == game.getPlayers().indexOf(game.getMainPlayer())) {
            //Checking if nobody joined quest -> main player draws card, update main player, draw new story card
            if(game.getQuestingPlayers().size()==0){
                int numStages = game.getCurrentStoryCard().getStages();
                int numCardsPlayed = 0;
                for(int i = 0; i < numStages; i++){
                    for(int j = 0; j < game.getSponsoredQuestCards()[i].length; j++){
                        numCardsPlayed++;
                    }
                }
                for(int i = 0; i < numStages+numCardsPlayed; i++){
                    Card card = game.getAdventureDeck().drawCard();
                    game.getMainPlayer().getCards().add(card);
                }
                simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),
                        "/topic/cards-in-hand/"+gameId, game.getMainPlayer().getCards());
                //Updating main player
                int indexOfNewMain = game.getPlayers().indexOf(game.getMainPlayer())+1%game.getPlayers().size();
                game.setMainPlayer(game.getPlayers().get(indexOfNewMain));
                //Send new drawn story card to everyone and ask main player if they wish to sponsor or not (if quest)
                StoryCard storyCard = game.getStoryDeck().drawCard();
                game.setCurrentStoryCard(storyCard);
                simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+gameId, storyCard);
                simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),"/topic/sponsor-quest/"+gameId,storyCard);
            }
            else {
                //1 card to each current questing player, send whether 1st stage is Foe or Test, send updated hands
                for(int i = 0; i < game.getQuestingPlayers().size();i++){
                    Card card = game.getAdventureDeck().drawCard();
                    game.getQuestingPlayers().get(i).getCards().add(card);
                    simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                            "/topic/play-against-quest-stage/"+gameId, game.getSponsoredQuestCards()[0][0].getType());
                    simpMessagingTemplate.convertAndSendToUser(game.getQuestingPlayers().get(i).getName(),
                            "/topic/cards-in-hand/"+gameId, game.getQuestingPlayers().get(i).getCards());
                }
            }
        }
        else{
            //Send to the next person asking them if they wish to join the quest
            int indexToSendTo = index+1%game.getPlayers().size();
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(indexToSendTo).getName(),
                    "/topic/cards-in-hand/"+gameId, game.getPlayers().get(indexToSendTo).getCards());
        }

        return ResponseEntity.ok().build();
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

        return ResponseEntity.ok().build();
    }

    public ResponseEntity completeCardsPlayedAgainstFoe(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
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
        }

        return ResponseEntity.ok().build();
    }


}
