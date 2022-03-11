package comp3004.project.QotRT.controller.dto;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.model.Player;

public class SubmitStageRequest {
    private Player player;
    private String gameId;
    private Integer stage;

    public Integer getStage() { return stage; }

    public Player getPlayer() {
        return player;
    }

    public String getGameId() {
        return gameId;
    }
}
