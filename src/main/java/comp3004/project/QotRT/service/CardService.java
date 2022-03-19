package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;


import java.util.ArrayList;

@Service
public class CardService {

    private EventService eventService;
    public CardService(){
        eventService = new EventService();
    }

    public ArrayList<Card> startGame(GameService gameService, String gameId, SimpMessagingTemplate simpMessagingTemplate){
        System.out.println("play-game request");
        System.out.println("GAMEID = " + gameId);
        Game game = gameService.getGame(gameId);
        for(int i = 0; i < game.getPlayers().size(); i++){
            System.out.println(game.getPlayers().get(i).getName());
        }
        //Get adventure deck -> buildDeck -> shuffle Deck
        game.getAdventureDeck().buildStartingDeck().shuffleDeck();
        game.getStoryDeck().buildStoryDeck().shuffle();

        //Deal 12 Cards to Each Player
        for(int i = 0; i < game.getPlayers().size(); i++){
            Player p = game.getPlayers().get(i);
            for(int j = 0; j < 12; j++){
                p.getCards().add(game.getAdventureDeck().drawCard());
            }
        }
        //Send Cards to Each User
        for(int i = 0; i < game.getPlayers().size(); i++){
            simpMessagingTemplate.convertAndSend(
                    "/topic/cards-in-hand/"+gameId+"/"+game.getPlayers().get(i).getUsername(), game.getPlayers().get(i).getCards());
        }
        StoryCard storyCard = game.getStoryDeck().drawCard();
        game.setCurrentStoryCard(storyCard);
        simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+gameId, storyCard);
        //Update player statuses
        updatePlayerStatuses(game);
        if(storyCard.getType().equals("Quest")){
            simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),"/topic/sponsor-quest/"+gameId,storyCard);
        }
        else if(storyCard.getType().equals("Event")){
            eventService.doEvent(game, simpMessagingTemplate);
        }


        return game.getPlayers().get(0).getCards();
    }


    public ArrayList<Card> discardCards(DiscardRequest request, GameService gameService, SimpMessagingTemplate simpMessagingTemplate){
        System.out.println("discard-cards request");
        System.out.println("PLAYER: " + request.getPlayer());
        System.out.println("GAMEID: " + request.getGameId());
        Game game = gameService.getGame(request.getGameId());
        Card discarded;
        //Remove discarded cards from players hand and move to adventure deck discard pile
        for (int i = 0 ; i < game.getPlayers().size(); i++){
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
               // Card discardedCard = game.getPlayers().get(i).getCards().remove(//INDEX)
                for (int j = 0; j<game.getPlayers().get(i).getCards().size(); j++){
                    if (game.getPlayers().get(i).getCards().get(j).getName().equals(request.getCard().getName())){
                        discarded = game.getPlayers().get(i).getCards().remove(j);
                        game.getAdventureDeck().discardCard(discarded);
                        break;
                    }
                }
                //Send card back to player
                simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+request.getGameId()+"/"+
                        game.getPlayers().get(i).getUsername(), game.getPlayers().get(i).getCards());
                break;
            }
        }
        simpMessagingTemplate.convertAndSend("/topic/discard-pile/" + request.getGameId(), game.getAdventureDeck().getDiscardPile());
        return game.getAdventureDeck().getDiscardPile();
    }

    public ArrayList<Card> drawCard (String gameId, ConnectRequest request, GameService gameService, SimpMessagingTemplate simpMessagingTemplate){
        System.out.println("draw-card request");
        Game game = gameService.getGame(gameId);
        //Finding Principal Name of Player making draw-card request
        String principalName = null;
        for(int i = 0; i < game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                principalName = game.getPlayers().get(i).getName();
            }
            System.out.println(game.getPlayers().get(i).getName());
        }
        //Deal Random Card to Player
        //IF THEY PICK UP CARD -> THEN THEY GOT 13 CARDS
        //THEY CAN EITHER DROP THE CARD OR KEEP THE CARD AND DROP ANOTHER CARD

        for (int i = 0 ; i < game.getPlayers().size(); i++){
            if (game.getPlayers().get(i).getName().equals(principalName)){
                Card card  = game.getAdventureDeck().drawCard();
                game.getPlayers().get(i).getCards().add(card);
                //Send cards back to player
                simpMessagingTemplate.convertAndSend(
                        "/topic/cards-in-hand/"+gameId+"/"+game.getPlayers().get(i).getUsername(), game.getPlayers().get(i).getCards());
                return game.getPlayers().get(i).getCards();
            }
        }
        return null;
    }


    //HELPER METHODS
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
}
