package comp3004.project.QotRT.controller;

import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.service.EventService;
import comp3004.project.QotRT.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
public class EventController {

    private EventService eventService;
    private GameService gameService;
    private SimpMessagingTemplate simpMessagingTemplate;

    public EventController(EventService eventService, GameService gameService, SimpMessagingTemplate simpMessagingTemplate){
        this.eventService = eventService;
        this.gameService = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @PostMapping("/discard-weapon")
    public ResponseEntity discardWeapon(@RequestParam String gameId, @RequestBody DiscardRequest request) throws Exception {
        return eventService.discardWeapon(gameId, request, simpMessagingTemplate, gameService);
    }

    @PostMapping("/discard-foe")
    public ResponseEntity discardFoe(@RequestParam String gameId, @RequestBody DiscardRequest request) throws Exception {
        return eventService.discardFoe(gameId, request, simpMessagingTemplate, gameService);
    }

}
