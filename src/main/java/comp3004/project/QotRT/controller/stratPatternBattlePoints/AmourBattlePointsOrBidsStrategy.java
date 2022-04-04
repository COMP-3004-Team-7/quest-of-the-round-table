package comp3004.project.QotRT.controller.stratPatternBattlePoints;

import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class AmourBattlePointsOrBidsStrategy implements GetBattlePointsOrBidsStrategy{
    @Override
    public int getBattlePoints(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player) {
        int amourBattlePoints = 0;
        for(int i = 0; i < player.getAmours().size(); i++){
            amourBattlePoints += player.getAmours().get(i).getMAXbattlepoints();
        }
        return  amourBattlePoints;
    }

    @Override
    public int getBids(Game game, SimpMessagingTemplate simpMessagingTemplate, Player player) {
        int amourBids = 0;
        for(int i = 0; i < player.getAmours().size(); i++){
            amourBids += player.getAmours().get(i).getBids();
        }
        return  amourBids;
    }
}
