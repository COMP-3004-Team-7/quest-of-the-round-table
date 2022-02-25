package comp3004.project.QotRT.model;

import java.security.Principal;
import java.util.ArrayList;

public class Player implements Principal {
    private String username;
    private String principalName; //Used for keeping track of Principal Session
    private Integer playerNumber;

    public Player(String name) {
        this.principalName = name;
        playerNumber = 0;
    }

    public Player(String name, Integer playerNumber) {
        this.principalName = name;
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
        return principalName;
    }

    public void setName(String name) {
        this.principalName = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
