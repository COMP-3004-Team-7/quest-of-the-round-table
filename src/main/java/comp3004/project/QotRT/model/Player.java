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
    private ArrayList<Card> allies;
    private Integer battlePoints;
    private Integer shields;
    private String rank;

    public Player() {
        cards = new ArrayList<>();
        weaponCardsPlayed = new ArrayList<>();
        allies = new ArrayList<>();
        playerNumber = 0;
        status = "current";
        battlePoints = 5;
        shields = 0;
        rank = "Squire";
    }

    public Player(String principalName) {
        this.principalName = principalName;
        cards = new ArrayList<>();
        weaponCardsPlayed = new ArrayList<>();
        allies = new ArrayList<>();
        playerNumber = 0;
        status = "current";
        battlePoints = 5;
        shields = 0;
        rank = "Squire";
    }

    public Player(String username, Integer playerNumber) {
        cards = new ArrayList<>();
        weaponCardsPlayed = new ArrayList<>();
        allies = new ArrayList<>();
        this.username = username;
        this.playerNumber = playerNumber;
        battlePoints = 5;
        shields = 0;
        rank = "Squire";
    }

    public Player(String username, String principalName, Integer playerNumber) {
        cards = new ArrayList<>();
        weaponCardsPlayed = new ArrayList<>();
        allies = new ArrayList<>();
        this.username = username;
        this.principalName = principalName;
        this.playerNumber = playerNumber;
        battlePoints = 5;
        shields = 0;
        rank = "Squire";
    }

    public ArrayList<Card> getAllies() {
        return allies;
    }

    public void setAllies(ArrayList<Card> allies) {
        this.allies = allies;
    }

    public Integer getShields() {
        return shields;
    }

    public void setShields(Integer shields) {
        this.shields = shields; this.setRank();
    }

    public Integer getBattlePoints() {
        return battlePoints;
    }

    public void setBattlePoints(Integer battlePoints) {
        this.battlePoints = battlePoints;
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

    public String getRank (){
        return rank;
    }

    public void setRank (){
        if(this.shields>=5 && this.rank.equals("Squire")){
            this.rank = "Knight";
            this.shields = this.shields - 5;
            this.battlePoints = 10;
        }
        else if (this.shields>=7 && this.rank.equals("Knight")){
            this.rank = "Champion Knight";
            this.shields = this.shields - 7;
            this.battlePoints = 20;
        }
        else if (this.shields>=10 && this.rank.equals("Champion Knight")) {
            this.rank = "Knight of the Round Table";

        }
    }
}
