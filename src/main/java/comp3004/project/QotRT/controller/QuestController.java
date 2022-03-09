package comp3004.project.QotRT.controller;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.service.CardService;
import comp3004.project.QotRT.service.GameService;
import comp3004.project.QotRT.service.QuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/quest")
public class QuestController {

    private GameService gameService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private CardService cardService;
    private QuestService questService;

    public QuestController(GameService gameService, QuestService questService, SimpMessagingTemplate simpMessagingTemplate, CardService cardService){
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.cardService = cardService;
        this.questService = questService;
    }

    @PostMapping("/decline-sponsor-quest")
    //@MessageMapping("/play-game/{gameId}")
    public String playGame(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return questService.declineQuest(gameId, request, simpMessagingTemplate, gameService);
    }

}
