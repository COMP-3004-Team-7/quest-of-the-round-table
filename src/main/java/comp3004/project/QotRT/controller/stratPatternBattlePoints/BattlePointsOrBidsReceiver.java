package comp3004.project.QotRT.controller.stratPatternBattlePoints;

import comp3004.project.QotRT.controller.stratPatternProceedQuestStage.ProceedQuestStageStrategy;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class BattlePointsOrBidsReceiver {

    private GetBattlePointsOrBidsStrategy getBattlePointsOrBidsStrategy;

    public void setGetBattlePointsOrBidsStrategy(GetBattlePointsOrBidsStrategy getBattlePointsOrBidsStrategy) {
        this.getBattlePointsOrBidsStrategy = getBattlePointsOrBidsStrategy;
    }

    public int receiveBattlePoints(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player){
        return getBattlePointsOrBidsStrategy.getBattlePoints(game, simpMessagingTemplate, player);
    }
    public int receiveBids(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player){
        return getBattlePointsOrBidsStrategy.getBids(game, simpMessagingTemplate, player);
    }
}
