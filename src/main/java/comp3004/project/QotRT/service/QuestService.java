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
    public String selectFoeCardForSponsorStage(String gameId, SelectSponsorCardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //Error checking (is card submitted an actual foe card?)
        //...

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

        return "";
    }

    public ResponseEntity addWeaponToSponsorStage(String gameId, SelectSponsorCardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        Game game = gameService.getGame(gameId);
        //Error checking (is card submitted an actual weapon card?)
        //...
        //Has weapon been played already?
        for(int i = 0; i < game.getSponsoredQuestCards()[request.getStage()-1].length; i++){
            if(game.getSponsoredQuestCards()[request.getStage()-1][i].getName()==request.getCard().getName()){
                return ResponseEntity.badRequest().body("Cannot submit duplicate weapon in same stage");
            }
        }

        //Add card to current stage
        game.addToSponsoredQuestCards(request.getCard(), request.getStage());

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
            //CHECK HERE IF WE SHOULD USE MIN OR MAX BATTLEPOINTS
            totalBattlePointsInSubmittedStage += game.getSponsoredQuestCards()[request.getStage()][i].getMINbattlepoints();
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
            if(game.getCurrentStoryCard().getStages() == request.getStage()){
                //Final Stage --> Send to 'quest is fully setup' and we can start asking each player if they want to join
                //the quest
            }
            else{
                //Not last stage --> Ask Player to complete the next quest stage
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
        }
        else{
            //Send to the next person asking them if they wish to join the quest
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
            if(game.getQuestingPlayers().size()==0){
                //Nobody joined the quest -> main player draws cards equal to number of cards played
                //To make up the quest + additional cards equal to number of stages on the quest
                //Update main player to next person clockwise and draw them a story card
            }
            else {
                //Give 1 card to each player currently on the quest
                //Alert to all players currently on the quest whether first stage is Test or Foe
                //and ask them to select cards to play on the quest
            }
        }
        else{
            //Send to the next person asking them if they wish to join the quest
        }

        return ResponseEntity.ok().build();
    }
}
