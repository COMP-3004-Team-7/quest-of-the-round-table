package comp3004.project.QotRT.controller.dto;


import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.model.Player;

public class DiscardRequest {
    private Player player;
    private String gameId;
    private Card card;

    public DiscardRequest(Player player, String gameId, Card card) {
        this.player = player;
        this.gameId = gameId;
        this.card = card;
    }

    public Player getPlayer() {
        return player;
    }

    public String getGameId() {
        return gameId;
    }

    public Card getCard() {
        return card;
    }
}
