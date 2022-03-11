package comp3004.project.QotRT.model;

import comp3004.project.QotRT.cards.Card;

import java.security.Principal;
import java.util.ArrayList;

public class Player implements Principal {
    private String username;
    private String principalName; //Used for keeping track of Principal Session
    private Integer playerNumber;
    private ArrayList<Card> cards;
    private String status;
    private ArrayList<Card> weaponCardsPlayed;

    public Player() {
        cards = new ArrayList<>();
        weaponCardsPlayed = new ArrayList<>();
        playerNumber = 0;
        status = "current";
    }

    public Player(String principalName) {
        this.principalName = principalName;
        cards = new ArrayList<>();
        weaponCardsPlayed = new ArrayList<>();
        playerNumber = 0;
        status = "current";
    }

    public Player(String username, Integer playerNumber) {
        this.username = username;
        this.playerNumber = playerNumber;
    }

    public Player(String username, String principalName, Integer playerNumber) {
        this.username = username;
        this.principalName = principalName;
        this.playerNumber = playerNumber;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public ArrayList<Card> getCards() { return cards; }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public ArrayList<Card> getWeaponCardsPlayed() {
        return weaponCardsPlayed;
    }

    public void setWeaponCardsPlayed(ArrayList<Card> weaponCardsPlayed) {
        this.weaponCardsPlayed = weaponCardsPlayed;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }
}
