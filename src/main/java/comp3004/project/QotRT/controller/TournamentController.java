package comp3004.project.QotRT.controller;

import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.service.CardService;
import comp3004.project.QotRT.service.GameService;
import comp3004.project.QotRT.service.QuestService;
import comp3004.project.QotRT.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournament")
public class TournamentController {
    private GameService gameService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private CardService cardService;
    private TournamentService tournamentService;

    public TournamentController(GameService gameService, TournamentService tournamentService, SimpMessagingTemplate simpMessagingTemplate, CardService cardService){
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.cardService = cardService;
        this.tournamentService = tournamentService;
    }

    //Decline to join tournament
    @PostMapping("/decline-joining-tournament")
    public ResponseEntity declineJoiningTournament(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return tournamentService.declineJoiningTournament(gameId, request, simpMessagingTemplate, gameService);
    }

    //Accept to join the tournament
    @PostMapping("/join-tournament")
    public ResponseEntity joinTournament(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return tournamentService.joinTournament(gameId, request, simpMessagingTemplate, gameService);
    }

    //Submit card to play in tournament
    @PostMapping("/submit-tournament-card")
    public ResponseEntity submitTournamentCard(@RequestParam String gameId, @RequestBody DiscardRequest request) throws Exception {
        return tournamentService.submitTournamentCard(gameId, request, simpMessagingTemplate, gameService);
    }

    //Submit card to play in tournament
    @PostMapping("/complete-submitting-tournament-cards")
    public ResponseEntity submitTournamentCard(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return tournamentService.completeCardsPlayedInTournament(gameId, request, simpMessagingTemplate, gameService);
    }

}
