package comp3004.project.QotRT.controller;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.SelectSponsorCardRequest;
import comp3004.project.QotRT.controller.dto.SubmitBidRequest;
import comp3004.project.QotRT.controller.dto.SubmitStageRequest;
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

    //Decline to sponsor the quest
    @PostMapping("/decline-sponsor-quest")
    public String declineSponsorQuest(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return questService.declineSponsorQuest(gameId, request, simpMessagingTemplate, gameService);
    }

    //Accept to sponsor the quest
    @PostMapping("/sponsor-quest")
    public StoryCard sponsorQuest(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return questService.sponsorQuest(gameId, request, simpMessagingTemplate, gameService);
    }


    //Select Card for sponsor quest stages
    @PostMapping("/select-card-for-sponsored-quest-stage")
    public ResponseEntity selectCardForSponsorStage(@RequestParam String gameId, @RequestBody SelectSponsorCardRequest selectSponsorCardRequest) throws Exception {
        return questService.selectCardForSponsorStage(gameId, selectSponsorCardRequest, simpMessagingTemplate, gameService);
    }

//    //Select Foe for sponsor quest stages
//    @PostMapping("/select-foe-for-sponsored-quest-stage")
//    public ResponseEntity selectFoeCardForSponsorStage(@RequestParam String gameId, @RequestBody SelectSponsorCardRequest selectSponsorCardRequest) throws Exception {
//        return questService.selectFoeCardForSponsorStage(gameId, selectSponsorCardRequest, simpMessagingTemplate, gameService);
//    }
//
//    //Add weapons to Foe sponsor stage
//    @PostMapping("/add-weapon-to-foe-quest-stage")
//    public ResponseEntity addWeaponToSponsorStage(@RequestParam String gameId, @RequestBody SelectSponsorCardRequest selectSponsorCardRequest) throws Exception {
//        return questService.addWeaponToSponsorStage(gameId, selectSponsorCardRequest, simpMessagingTemplate, gameService);
//    }

    //Submit completed stage for quest
    @PostMapping("/submit-completed-quest-stage")
    public ResponseEntity submitSponsorStage(@RequestParam String gameId, @RequestBody SubmitStageRequest request) throws Exception {
        return questService.submitSponsorStage(gameId, request, simpMessagingTemplate, gameService);
    }

    //Player joins Quest
    @PostMapping("/join-current-quest")
    public ResponseEntity joinCurrentQuest(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return questService.joinCurrentQuest(gameId, request, simpMessagingTemplate, gameService);
    }

    //Player declines to join Quest
    @PostMapping("/decline-to-join-current-quest")
    public ResponseEntity declineToJoinCurrentQuest(@RequestParam String gameId, @RequestBody ConnectRequest request) throws Exception {
        return questService.declineToJoinCurrentQuest(gameId, request, simpMessagingTemplate, gameService);
    }

    //Player plays card to battle Foe of Quest
    @PostMapping("/submit-card-against-foe")
    public ResponseEntity submitCardAgainstFoe(@RequestParam String gameId, @RequestBody SelectSponsorCardRequest request) throws Exception {
        return questService.submitCardAgainstFoe(gameId, request, simpMessagingTemplate, gameService);
    }

    //Player completes submission against Foe of Quest
    @PostMapping("/complete-cards-played-against-foe")
    public ResponseEntity completeCardsPlayedAgainstFoe(@RequestParam String gameId, @RequestBody SubmitStageRequest request) throws Exception {
        return questService.completeCardsPlayedAgainstFoe(gameId, request, simpMessagingTemplate, gameService);
    }

    //player submits bid for the test
    @PostMapping("/submit-bid")
    public ResponseEntity submitBid(@RequestParam String gameId, @RequestBody SubmitBidRequest request) throws Exception {
        return questService.submitBid(gameId, request, simpMessagingTemplate, gameService);
    }

    //player declines to submit bid for the test
    @PostMapping("/decline-to-submit-bid")
    public ResponseEntity declineToSubmitBid(@RequestParam String gameId, @RequestBody SubmitStageRequest request) throws Exception {
        return questService.declineToSubmitBid(gameId, request, simpMessagingTemplate, gameService);
    }

    //player declines to submit bid for the test
    @PostMapping("/discard-cards-for-test")
    public ResponseEntity discardCardsForTest(@RequestParam String gameId, @RequestBody SelectSponsorCardRequest request) throws Exception {
        return questService.discardForTestCard(gameId, request, simpMessagingTemplate, gameService);
    }
}
