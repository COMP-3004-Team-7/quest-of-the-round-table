package comp3004.project.QotRT.model;

import comp3004.project.QotRT.cards.Card;

import java.util.ArrayList;

public class Game {
    private Integer nextPlayerNumber = 1;
    private ArrayList<Player> players;
    private String gameId;
    private GameStatus status;
    private AdventureDeck adventureDeck;
    private ArrayList<Player> questingPlayers;
    private Player mainPlayer;

    public Game() {
        adventureDeck = new AdventureDeck();
        players = new ArrayList<>();
        questingPlayers = new ArrayList<>();
    }

    public void addPlayer(Player p){
        players.add(p);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Integer getNextPlayerNumber() {
        return nextPlayerNumber++;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public AdventureDeck getAdventureDeck() {
        return adventureDeck;
    }

    public ArrayList<Player> getQuestingPlayers() { return questingPlayers; }

    public void setQuestingPlayers(ArrayList<Player> questingPlayers) { this.questingPlayers = questingPlayers; }

    public Player getMainPlayer() { return mainPlayer; }

    public void setMainPlayer(Player mainPlayer) { this.mainPlayer = mainPlayer; }
}
