package comp3004.project.QotRT.model;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.cards.StoryDeck;

import java.util.ArrayList;

public class Game {
    private Integer nextPlayerNumber = 1;
    private ArrayList<Player> players;
    private String gameId;
    private GameStatus status;
    private AdventureDeck adventureDeck;
    private StoryDeck storyDeck;
    private ArrayList<Player> questingPlayers;
    private Player mainPlayer;
    private StoryCard currentStoryCard;
    private Card[][] sponsoredQuestCards;

    public Game() {
        adventureDeck = new AdventureDeck();
        players = new ArrayList<>();
        questingPlayers = new ArrayList<>();
        storyDeck = new StoryDeck();
    }

    //GETTER AND SETTERS
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

    public StoryDeck getStoryDeck() {
        return storyDeck;
    }

    public StoryCard getCurrentStoryCard() {
        return currentStoryCard;
    }

    public void setCurrentStoryCard(StoryCard currentStoryCard) {
        this.currentStoryCard = currentStoryCard;
        sponsoredQuestCards = new Card[currentStoryCard.getStages()][];
    }

    public Card[][] getSponsoredQuestCards() {
        return sponsoredQuestCards;
    }

    public ArrayList<Player> getQuestingPlayers() { return questingPlayers; }

    public void setQuestingPlayers(ArrayList<Player> questingPlayers) { this.questingPlayers = questingPlayers; }

    public Player getMainPlayer() { return mainPlayer; }

    public void setMainPlayer(Player mainPlayer) { this.mainPlayer = mainPlayer; }


    //METHODS
    public void addPlayer(Player p){
        players.add(p);
    }

    public void addToSponsoredQuestCards(Card c, Integer stage) {
        int length = sponsoredQuestCards[stage].length;
        sponsoredQuestCards[stage][length] = c;
    }

    public void addQuestingPlayer(Player p){
        questingPlayers.add(p);
    }
}
