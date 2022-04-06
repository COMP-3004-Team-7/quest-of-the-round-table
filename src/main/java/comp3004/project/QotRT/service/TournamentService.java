package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AllyBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AmourBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.BattlePointsOrBidsReceiver;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardStrategy;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


@Service
public class TournamentService {
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();
    private final BattlePointsOrBidsReceiver battlePointsOrBidsReceiver = new BattlePointsOrBidsReceiver();

    public ResponseEntity declineJoiningTournament(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        Game game = gameService.getGame(gameId);
        //Get index of player who declined
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i=0; i<game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                index = i;
                break;
            }
        }
        int nextPlayerIndex = (index+1) %sizeOfPlayersList;

        //Check if this is the last player to decline
        if (nextPlayerIndex == game.getPlayers().indexOf(game.getMainPlayer())){
            if(game.getTournamentPlayers().size() <= 1){
                if(game.getTournamentPlayers().size() == 1) {
                    Player p = game.getTournamentPlayers().get(0);
                    p.setShields(p.getShields() + 1 + game.getCurrentStoryCard().getStages());
                }
                newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
            }
            else{
                //Send to all players in tournament to pick cards to play in tournament (also give them 1 new card each and set status to current)
                for(int i = 0; i < game.getTournamentPlayers().size(); i++){
                    Card c = game.getAdventureDeck().drawCard();
                    game.getTournamentPlayers().get(i).getCards().add(c);
                    game.getTournamentPlayers().get(i).setStatus("current");
                    game.setNumOfTournamentPlayers(game.getTournamentPlayers().size()); //set original tournament size
                    simpMessagingTemplate.convertAndSendToUser(game.getTournamentPlayers().get(i).getName(),
                            "/topic/play-in-tournament/"+gameId ,game.getCurrentStoryCard());
                }
            }
        }
        //Otherwise ask next player clockwise if they want to join
        else{
            updatePlayerStatusesClockwise(game, index);
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(nextPlayerIndex).getName(),
                    "/topic/join-tournament/"+gameId, game.getCurrentStoryCard());
        }

        return ResponseEntity.ok().body("You have declined to join tournament");
    }

    public ResponseEntity joinTournament(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        Game game = gameService.getGame(gameId);
        //Get index of player who joined and add them to tournament players
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i=0; i<game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                game.getTournamentPlayers().add(game.getPlayers().get(i));
                index = i;
                break;
            }
        }
        int nextPlayerIndex = (index+1) %sizeOfPlayersList;

        //Check if this is the last player to join
        if (nextPlayerIndex == game.getPlayers().indexOf(game.getMainPlayer())){
            //Check if only 1 player joined, give them shields, update main player, draw new story card
            if(game.getTournamentPlayers().size() == 1){
                Player p = game.getTournamentPlayers().get(0);
                p.setShields(p.getShields() + 1 + game.getCurrentStoryCard().getStages());
//                int indexOfNewMain = (game.getPlayers().indexOf(game.getMainPlayer())+1) % game.getPlayers().size();
//                game.setMainPlayer((game.getPlayers().get(indexOfNewMain)));
                newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
            }
            else{
                //Send to all players in tournament to pick cards to play in tournament (also give them 1 new card each and set status to current)
                for(int i = 0; i < game.getTournamentPlayers().size(); i++){
                    Card c = game.getAdventureDeck().drawCard();
                    game.getTournamentPlayers().get(i).getCards().add(c);
                    game.getTournamentPlayers().get(i).setStatus("current");
                    game.setNumOfTournamentPlayers(game.getTournamentPlayers().size()); //set original tournament size
                    simpMessagingTemplate.convertAndSendToUser(game.getTournamentPlayers().get(i).getName(),
                            "/topic/play-in-tournament/"+gameId ,game.getCurrentStoryCard());
                }
            }
        }
        //Otherwise, ask next player clockwise if they want to join
        else{
            updatePlayerStatusesClockwise(game, index);
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(nextPlayerIndex).getName(),
                    "/topic/join-tournament/"+gameId, game.getCurrentStoryCard());
        }

        return ResponseEntity.ok().body("You have successfully joined the tournament");
    }

    public ResponseEntity submitTournamentCard(String gameId, DiscardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        Game game = gameService.getGame(gameId);
        //Check if card submitted is a weapon card -> if not then throw error
        if(request.getCard().getType().equals("Foe") || request.getCard().getType().equals("Test")){
            return ResponseEntity.badRequest().body("Can only play weapon, amour, or ally cards");
        }
        //Otherwise, check for duplicate weapon cards
        if(request.getCard().getType().equals("Weapon")) {
            int index = 0;
            for (int i = 0; i < game.getPlayers().size(); i++) {
                if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                    for (int j = 0; j < game.getPlayers().get(i).getWeaponCardsPlayed().size(); j++) {
                        if (game.getPlayers().get(i).getWeaponCardsPlayed().get(j).getName().equals(request.getCard().getName())) {
                            return ResponseEntity.badRequest().body("Cannot play duplicate weapon cards");
                        }
                    }
                    Player p = game.getPlayers().get(i);
                    //Otherwise, all good and we add weapon card to players played cards
                    for (int j = 0; j < p.getCards().size(); j++) {
                        if (p.getCards().get(j).getName().equals(request.getCard().getName())) {
                            Card c = p.getCards().remove(j);
                            p.getWeaponCardsPlayed().add(c);
                            simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                                    p.getName(), p.getCards());
                            break;
                        }
                    }
                }
            }
        }
        if(request.getCard().getType().equals("Amour")) {
            for (int i = 0; i < game.getTournamentPlayers().size(); i++) {
                if (game.getTournamentPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                    Player p = game.getTournamentPlayers().get(i);
                    for (int j = 0; j < p.getCards().size(); j++) {
                        if (p.getCards().get(j).getName().equals(request.getCard().getName())) {
                            Card c = p.getCards().remove(j);
                            p.getAmours().add(c);
                            simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                                    p.getName(), p.getCards());
                            break;
                        }
                    }
                }
            }
        }
        if(request.getCard().getType().equals("Ally")){
            for (int i = 0; i < game.getTournamentPlayers().size(); i++) {
                if (game.getTournamentPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                    Player p = game.getTournamentPlayers().get(i);
                    for (int j = 0; j < p.getCards().size(); j++) {
                        if (p.getCards().get(j).getName().equals(request.getCard().getName())) {
                            Card c = p.getCards().remove(j);
                            p.getAllies().add(c);
                            simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+gameId+"/"+
                                    p.getName(), p.getCards());
                            break;
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok().body("You have successfully submitted a card in the tournament");
    }

    public ResponseEntity completeCardsPlayedInTournament(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        Game game = gameService.getGame(gameId);
        //Get player who submitted their cards, set status to "waiting", and check if anyone else needs to submit their cards
        int numWaitingPlayers = 0;
        for (int i=0; i<game.getTournamentPlayers().size(); i++){
            if(game.getTournamentPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                game.getTournamentPlayers().get(i).setStatus("waiting");
            }
            if(game.getTournamentPlayers().get(i).getStatus().equals("waiting")){
                numWaitingPlayers++;
            }
        }

        //Check if this is the last player to submit their cards
        if(numWaitingPlayers == game.getTournamentPlayers().size()){
            //Check for winner
            HashMap<Player, Integer> playersPointsMap = new HashMap<>();
            for(int i = 0; i < game.getTournamentPlayers().size(); i++){
                int BattlePoints = 0;
                Player p = game.getTournamentPlayers().get(i);
                for(int j = 0; j < p.getWeaponCardsPlayed().size(); j++){
                    BattlePoints += p.getWeaponCardsPlayed().get(j).getMAXbattlepoints();
                }
                BattlePoints += p.getBattlePoints();
                //Get Total Battle Points from Amours
                battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AmourBattlePointsOrBidsStrategy());
                BattlePoints += battlePointsOrBidsReceiver.receiveBattlePoints(game, simpMessagingTemplate, game.getTournamentPlayers().get(i));
                //Get Total Battle Points from Allies
                battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
                BattlePoints += battlePointsOrBidsReceiver.receiveBattlePoints(game, simpMessagingTemplate, game.getTournamentPlayers().get(i));


                playersPointsMap.put(p,BattlePoints);
            }
            int maxValue = Collections.max(playersPointsMap.values());
            ArrayList<Player> winners = new ArrayList<>();
            for(int i = 0; i < game.getTournamentPlayers().size(); i++){
                if(playersPointsMap.get(game.getTournamentPlayers().get(i)) == maxValue){
                    winners.add(game.getTournamentPlayers().get(i));
                }
                else{
                    removeWeaponCards(game,game.getTournamentPlayers().get(i));
                    removeAmourCards(game, game.getTournamentPlayers().get(i));
                }
            }

            //Check for 1 winner
            if(winners.size() == 1){
                //give shields, remove weapons and amours, draw new story card
                winners.get(0).setShields(game.getCurrentStoryCard().getStages() + game.getNumOfTournamentPlayers());
                removeWeaponCards(game,winners.get(0));
                removeAmourCards(game, winners.get(0));
                simpMessagingTemplate.convertAndSendToUser(winners.get(0).getName(),"/topic/cards-in-hand/"+gameId
                        , winners.get(0).getCards());
                if(winners.get(0).getRank().equals("Knight")){
                    simpMessagingTemplate.convertAndSend("/topic/game-winner/" + gameId, winners.get(0).getUsername() + " won the game!");
                }
                else {
                    newStoryCardDealer.dealWithNewStoryCard(game, simpMessagingTemplate);
                }
            }
            //Check if multiple winners and if they need a tie-breaker
            else if(!game.getInTieBreakerTournament()){
                game.setTournamentPlayers(winners);
                for(Player p : winners){
                    removeWeaponCards(game,p);
                    Card c = game.getAdventureDeck().drawCard();
                    p.getCards().add(c);
                    simpMessagingTemplate.convertAndSendToUser(p.getName(),"/topic/cards-in-hand/"+gameId
                            ,p.getCards());
                    p.setStatus("current");
                    simpMessagingTemplate.convertAndSendToUser(p.getName(),
                            "/topic/play-in-tournament/"+gameId ,game.getCurrentStoryCard());
                }
                game.setInTieBreakerTournament(true);
            }
            //Otherwise, multiple winners even after a tie-breaker
            else{
                ArrayList<Player> gameWinners = new ArrayList<>();
                for(Player p : winners){
                    removeWeaponCards(game,p);
                    removeAmourCards(game,p);
                    p.setShields(game.getCurrentStoryCard().getStages() + game.getNumOfTournamentPlayers());
                    if(p.getRank().equals("Knight")){
                        gameWinners.add(p);
                    }
                }
                if(gameWinners.size() > 0) {
                    String winnerString = "";
                    for (int i = 0; i < winners.size(); i++) {
                        winnerString += winners.get(i).getUsername();
                        if (i == winners.size() - 1) {
                            winnerString += " ";
                        } else {
                            winnerString += " and ";
                        }
                    }
                    simpMessagingTemplate.convertAndSend("/topic/game-winner/" + gameId, winnerString + "won the game!");
                }
                else {
                    newStoryCardDealer.dealWithNewStoryCard(game, simpMessagingTemplate);
                }
            }
        }

        return ResponseEntity.ok().body("You have successfully completed all cards you wish to play in the tournament");
    }


    //HELPER METHODS
    private void updatePlayerStatusesClockwise(Game game, int indexOfWaiting) {
        int indexOfNextCurrent = (indexOfWaiting+1)%game.getPlayers().size();
        game.getPlayers().get(indexOfWaiting).setStatus("waiting");
        game.getPlayers().get(indexOfNextCurrent).setStatus("current");
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

}
