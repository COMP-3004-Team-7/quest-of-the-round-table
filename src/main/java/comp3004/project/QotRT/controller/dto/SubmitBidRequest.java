package comp3004.project.QotRT.controller.dto;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.model.Player;

public class SubmitBidRequest {
    private Player player;
    private Integer bid;
    private Integer stage;

    public SubmitBidRequest() {
    }

    public Integer getStage() {
        return stage;
    }

    public SubmitBidRequest(Player player, int bid, int stage) {
        this.player = player;
        this.bid = bid;
        this.stage = stage;
    }

    public Integer getBid() {
        return bid;
    }

    public Player getPlayer() {
        return player;
    }

}
