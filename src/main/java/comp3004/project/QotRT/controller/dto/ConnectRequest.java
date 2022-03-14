package comp3004.project.QotRT.controller.dto;


import comp3004.project.QotRT.model.Player;

public class ConnectRequest {
    private Player player;
    private String gameId;

    public ConnectRequest(Player player, String gameId) {
        this.player = player;
        this.gameId = gameId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getGameId() {
        return gameId;
    }
}
