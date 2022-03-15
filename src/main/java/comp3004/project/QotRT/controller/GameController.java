package comp3004.project.QotRT.controller;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.service.CardService;
import comp3004.project.QotRT.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;

@RestController
@RequestMapping("/game")
public class GameController {

    private GameService gameService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private CardService cardService;

    public GameController(GameService gameService, SimpMessagingTemplate simpMessagingTemplate, CardService cardService) {
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.cardService = cardService;
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
    public ArrayList<Card> playGame(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return cardService.startGame(gameService, gameId, simpMessagingTemplate);
    }
    //PART 1 - BASIC REMOVAL
    @PostMapping("/discard-cards")
    //@MessageMapping("/discard-cards/{gameId}")
    //@SendTo("/topic/discard-pile/{gameId}")
    public ArrayList<Card> discardCards(@DestinationVariable String gameId, @RequestBody DiscardRequest request) throws Exception {
        return cardService.discardCards(request, gameService, simpMessagingTemplate);
    }

    //User Pressed 'Draw Card' Button
    @PostMapping("/draw-card")
    //@MessageMapping("/draw-card/{gameId}")
    public ArrayList<Card> drawCard(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return cardService.drawCard(gameId, request, gameService, simpMessagingTemplate);
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