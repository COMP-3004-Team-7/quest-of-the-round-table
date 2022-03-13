package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.storage.GameStorage;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;

@Service
public class CardService {

    private EventService eventService;
    public CardService(){
        eventService = new EventService();
    }

    public String startGame(GameService gameService, String gameId, SimpMessagingTemplate simpMessagingTemplate){
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
            simpMessagingTemplate.convertAndSendToUser(
                    game.getPlayers().get(i).getName(),"/topic/cards-in-hand/"+gameId, game.getPlayers().get(i).getCards());
        }
        StoryCard storyCard = game.getStoryDeck().drawCard();
        game.setCurrentStoryCard(storyCard);
        simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+gameId, storyCard);
        if(storyCard.getType().equals("Quest")){
            simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),"/topic/sponsor-quest/"+gameId,storyCard);
        }
        else if(storyCard.getType().equals("Event")){
            eventService.doEvent(game);
        }


        return "Dummy Data";
    }

    public ArrayList<Card> discardCards(String gameId, ConnectRequest request, GameService gameService, SimpMessagingTemplate simpMessagingTemplate){
        System.out.println("discard-cards request");
        Game game = gameService.getGame(gameId);
        Card discardedCard = null;
        //Remove discarded cards from players hand and move to adventure deck discard pile
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())) {
                discardedCard = game.getPlayers().get(i).getCards().remove(0);
                //Send discarded card back to everyone
                simpMessagingTemplate.convertAndSend(
                        "/topic/discard-pile/" + gameId, discardedCard);
                //Send cards-in-hand back to player
                return game.getPlayers().get(i).getCards();
            }
        }
        return null;
    }

    public ArrayList<Card> drawCard (String gameId, ConnectRequest request, GameService gameService){
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
                return game.getPlayers().get(i).getCards();
            }
        }
        return null;
    }

    //DISCARD CARDS
    //User Pressed 'Discard Card' Button to discard 1 or more cards
    //Body should be a different 'dto' instead of a Connect Request maybe a DiscardCardRequest
    //PART 2 (ADVANCE - NEEDS TO BE USED LATER IN THE PROJECT)
//    @MessageMapping("/discard-cards/{gameId}")
//    @SendTo("/topic/discard-pile/{gameId}")
//    public Card discardCards(@DestinationVariable String gameId, @RequestBody Card card, Principal principal) throws Exception {
//        System.out.println("discard-cards request");
//        System.out.println("PLAYER: " + principal.getName());
//        System.out.println("GAMEID: " + gameId);
//        Game game = gameService.getGame(gameId);
//        Card discarded;
//        //Remove discarded cards from players hand and move to adventure deck discard pile
//        for (int i = 0 ; i < game.getPlayers().size(); i++){
//            if (game.getPlayers().get(i).getName().equals(principal.getName())){
//               // Card discardedCard = game.getPlayers().get(i).getCards().remove(//INDEX)
//                for (int j = 0; j<game.getPlayers().get(i).getCards().size(); j++){
//                    if (game.getPlayers().get(i).getCards().get(j).equals(card)){
//                        discarded = game.getPlayers().get(i).getCards().remove(j);
//                        game.getAdventureDeck().discardCard(discarded);
//                        break;
//                    }
//                }
//                //Send card back to player
//                simpMessagingTemplate.convertAndSendToUser(principal.getName(),
//                        "/topic/discard-pile/" + gameId, game.getPlayers().get(i).getCards());
//                break;
//            }
//        }
//        return card;
//    }
}
