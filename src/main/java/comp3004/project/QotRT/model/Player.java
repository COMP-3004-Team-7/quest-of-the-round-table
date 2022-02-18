package comp3004.project.QotRT.model;

import java.security.Principal;
import java.util.ArrayList;

public class Player implements Principal {
    private String name;
    private Integer playerNumber;

    public Player(String name) {
        this.name = name;
        playerNumber = 0;
    }

    public Player(String name, Integer playerNumber) {
        this.name = name;
        this.playerNumber = playerNumber;
    }

    public Integer getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(Integer playerNumber) {
        this.playerNumber = playerNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
