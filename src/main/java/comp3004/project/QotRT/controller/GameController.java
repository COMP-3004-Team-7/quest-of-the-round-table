package comp3004.project.QotRT.controller;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Objects;

@RestController
@RequestMapping("/game")
public class GameController {

    private GameService gameService;
    private SimpMessagingTemplate simpMessagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    //User Created a new game
    @PostMapping("/start")
    public ResponseEntity<Game> start(@RequestBody Player player){
        System.out.println("start game request");
        System.out.println("start game request-playername: " + player.getUsername());
        //Set Cards
        player.setCards(new ArrayList<>());
        return ResponseEntity.ok(gameService.createGame(player));
    }

    //User Connected to Specific Game
    @PostMapping("/connect")
    @SendTo("/game-progress/{gameId}")
    public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws Exception {
        System.out.println("connect game request");
        //Set Cards
        request.getPlayer().setCards(new ArrayList<>());
        return ResponseEntity.ok(gameService.connectToGame(request.getPlayer(), request.getGameId()));
    }

    //User Pressed 'Start Game' Button
    @PostMapping("/play-game")
    //@MessageMapping("/play-game/{gameId}")
    public String playGame(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        System.out.println("play-game request");
        System.out.println("GAMEID = " + gameId);
        Game game = gameService.getGame(gameId);
        for(int i = 0; i < game.getPlayers().size(); i++){
            System.out.println(game.getPlayers().get(i).getName());
        }
        //Get adventure deck -> buildDeck -> shuffle Deck
        game.getAdventureDeck().buildStartingDeck().shuffleDeck();
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
        return "Dummy Data";
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

    //PART 1 - BASIC REMOVAL
    @PostMapping("/discard-cards")
    //@MessageMapping("/discard-cards/{gameId}")
    //@SendTo("/topic/discard-pile/{gameId}")
    public ArrayList<Card> discardCards(@DestinationVariable String gameId, @RequestBody ConnectRequest request) throws Exception {
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

    //User Pressed 'Draw Card' Button
    @PostMapping("/draw-card")
    //@MessageMapping("/draw-card/{gameId}")
    public ArrayList<Card> drawCard(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
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

    //This is used to just update the principal names connect players
    @MessageMapping("/update-principal/{gameId}")
    //@SendToUser("/topic/updated-principal-name/{gameId}")
    public void updatePrincipal(@DestinationVariable String gameId, @RequestBody ConnectRequest request, Principal principal) throws Exception {
        System.out.println("update principal request");
        Game game = gameService.getGame(gameId);
        for(int i = 0; i < game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                System.out.println("Successfully Updated");
                game.getPlayers().get(i).setName(principal.getName());
                break;
            }
        }
    }

}