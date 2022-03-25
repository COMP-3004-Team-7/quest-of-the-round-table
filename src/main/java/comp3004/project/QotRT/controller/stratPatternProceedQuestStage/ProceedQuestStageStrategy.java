package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface ProceedQuestStageStrategy {
    void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player);
}
