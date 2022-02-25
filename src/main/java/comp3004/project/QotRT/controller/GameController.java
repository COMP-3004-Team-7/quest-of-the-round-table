package comp3004.project.QotRT.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
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
        return ResponseEntity.ok(gameService.createGame(player));
    }

    //User Connected to Specific Game
    @PostMapping("/connect")
    @SendTo("/game-progress/{gameId}")
    public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws Exception {
        System.out.println("connect game request");
        return ResponseEntity.ok(gameService.connectToGame(request.getPlayer(), request.getGameId()));
    }

    //User Pressed 'Start Game' Button
    @MessageMapping("/play-game/{gameId}")
    public void playGame(@DestinationVariable String gameId, @RequestBody ConnectRequest request, Principal principal) throws Exception {
        System.out.println("play-game request");
        System.out.println("PLAYER: " + principal.getName());
        System.out.println("GAMEID: " + gameId);
        Game game = gameService.getGame(gameId);
        for(int i = 0; i < game.getPlayers().size(); i++){
            System.out.println(game.getPlayers().get(i).getName());
        }
        //Deal 12 Random Cards to Each Player
        //....
        //Send to Cards to Each User (Right now it just sends principal name back to them)
        for(int i = 0; i < game.getPlayers().size(); i++){
            simpMessagingTemplate.convertAndSendToUser(
                    game.getPlayers().get(i).getName(),"/topic/game-progress/"+gameId, game.getPlayers().get(i).getName());
        }
    }

    //User Pressed 'Start Game' Button
    @MessageMapping("/draw-card/{gameId}")
    public void drawCard(@DestinationVariable String gameId, @RequestBody ConnectRequest request, Principal principal) throws Exception {
        System.out.println("play-game request");
        System.out.println("PLAYER: " + principal.getName());
        System.out.println("GAMEID: " + gameId);
        Game game = gameService.getGame(gameId);
        for(int i = 0; i < game.getPlayers().size(); i++){
            System.out.println(game.getPlayers().get(i).getName());
        }
        //Deal Random Card to Player
        //...
        //Send card back to player
        simpMessagingTemplate.convertAndSendToUser(principal.getName(),
                "/topic/game-progress/" + gameId, principal.getName());
    }

    //This is used to just update the principal names connect players
    @MessageMapping("/update-principal/{gameId}")
    public void updatePrincipal(@DestinationVariable String gameId, @RequestBody ConnectRequest request, Principal principal) throws Exception {
        System.out.println("update principal request");
        System.out.println("GAMEID: " + gameId);
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