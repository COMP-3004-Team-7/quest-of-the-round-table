package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class FoeStageStrategy implements ProceedQuestStageStrategy{

    @Override
    public void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player) {
        //TODO remove some code inside 'completeCardsPlayedAgainstFoe' and put it here to deal with proceeding queststage forward
    }
}
