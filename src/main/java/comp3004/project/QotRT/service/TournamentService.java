package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardStrategy;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TournamentService {
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();

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
                int indexOfNewMain = (game.getPlayers().indexOf(game.getMainPlayer())+1) % game.getPlayers().size();
                game.setMainPlayer((game.getPlayers().get(indexOfNewMain)));
                newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
            }
            else{
                //Send to all players in tournament to pick cards to play in tournament (also give them 1 new card each and set status to current)
                for(int i = 0; i < game.getTournamentPlayers().size(); i++){
                    Card c = game.getAdventureDeck().drawCard();
                    game.getTournamentPlayers().get(i).getCards().add(c);
                    game.getTournamentPlayers().get(i).setStatus("current");
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
        if(!request.getCard().getType().equals("Weapon")){
            return ResponseEntity.badRequest().body("Must play weapon cards in tournament");
        }
        //Otherwise, check for duplicate weapon cards
        else{
            int index = 0;
            for (int i=0; i<game.getPlayers().size(); i++){
                if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                    for(int j = 0; j < game.getPlayers().get(i).getWeaponCardsPlayed().size(); j++){
                        if(game.getPlayers().get(i).getWeaponCardsPlayed().get(j).getName().equals(request.getCard().getName())){
                            return ResponseEntity.badRequest().body("Cannot play duplicate weapon cards");
                        }
                    }
                    //Otherwise, all good and we add weapon card to players played cards
                    game.getPlayers().get(i).getWeaponCardsPlayed().add(request.getCard());
                    break;
                }
            }
        }

        return ResponseEntity.ok().body("You have successfully submitted a card in the tournament");
    }

    public ResponseEntity completeCardsPlayedInTournament(String gameId, ConnectRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
        //TODO Finish complete cards played in tournament method
        return ResponseEntity.ok().body("You have successfully completed all cards you wish to play in the tournament");
    }


    //HELPER METHODS
    private void updatePlayerStatusesClockwise(Game game, int indexOfWaiting) {
        int indexOfNextCurrent = (indexOfWaiting+1)%game.getPlayers().size();
        game.getPlayers().get(indexOfWaiting).setStatus("waiting");
        game.getPlayers().get(indexOfNextCurrent).setStatus("current");
    }



}
