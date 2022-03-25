package comp3004.project.QotRT.controller.stratPatternProceedQuestStage;

import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardStrategy;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class QuestStageProceeder {

    private ProceedQuestStageStrategy proceedQuestStageStrategy;

    public void setProceedQuestStageStrategy(ProceedQuestStageStrategy proceedQuestStageStrategy) {
        this.proceedQuestStageStrategy = proceedQuestStageStrategy;
    }

    public void proceedQuestStage(Game game, SimpMessagingTemplate simpMessagingTemplate, int stage, Player player){
        proceedQuestStageStrategy.proceedQuestStage(game, simpMessagingTemplate, stage, player);
    }
}
