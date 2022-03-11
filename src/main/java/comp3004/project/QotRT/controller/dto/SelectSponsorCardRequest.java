package comp3004.project.QotRT.controller.dto;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.model.Player;

public class SelectSponsorCardRequest {
    private Player player;
    private String gameId;
    private Card card;
    private Integer stage;

    public SelectSponsorCardRequest() {
    }

    public SelectSponsorCardRequest(Player player, String gameId, Card card, Integer stage) {
        this.player = player;
        this.gameId = gameId;
        this.card = card;
        this.stage = stage;
    }

    public Integer getStage() { return stage; }

    public Card getCard() { return card; }

    public Player getPlayer() {
        return player;
    }

    public String getGameId() {
        return gameId;
    }
}
