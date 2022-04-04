package comp3004.project.QotRT.controller.stratPatternBattlePoints;

import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface GetBattlePointsOrBidsStrategy {
    int getBattlePoints(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player);
    int getBids(Game game, SimpMessagingTemplate simpMessagingTemplate,  Player player);
}
