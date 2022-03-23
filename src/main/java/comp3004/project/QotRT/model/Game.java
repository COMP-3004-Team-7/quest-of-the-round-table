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
    private ArrayList<Player> tournamentPlayers;
    private Player mainPlayer;
    private StoryCard currentStoryCard;
    private ArrayList<Card> firstStage;
    private ArrayList<Card> secondStage;
    private ArrayList<Card> thirdStage;
    private ArrayList<Card> fourthStage;
    private ArrayList<Card> fifthStage;
    private int bonusShield = 0;
    private int numOfTournamentPlayers;
    private Boolean inTieBreakerTournament;

    public Game() {
        inTieBreakerTournament = false;
        numOfTournamentPlayers = 0;
        adventureDeck = new AdventureDeck();
        players = new ArrayList<>();
        questingPlayers = new ArrayList<>();
        tournamentPlayers = new ArrayList<>();
        storyDeck = new StoryDeck();
        firstStage = new ArrayList<>();
        secondStage = new ArrayList<>();
        thirdStage = new ArrayList<>();
        fourthStage = new ArrayList<>();
        fifthStage = new ArrayList<>();
    }

    //GETTER AND SETTERS
    public Boolean getInTieBreakerTournament() {
        return inTieBreakerTournament;
    }

    public void setInTieBreakerTournament(Boolean inTieBreakerTournament) {
        this.inTieBreakerTournament = inTieBreakerTournament;
    }

    public int getNumOfTournamentPlayers() {
        return numOfTournamentPlayers;
    }

    public void setNumOfTournamentPlayers(int numOfTournamentPlayers) {
        this.numOfTournamentPlayers = numOfTournamentPlayers;
    }

    public ArrayList<Player> getTournamentPlayers() {
        return tournamentPlayers;
    }

    public void setTournamentPlayers(ArrayList<Player> tournamentPlayers) {
        this.tournamentPlayers = tournamentPlayers;
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

    public StoryDeck getStoryDeck() {
        return storyDeck;
    }

    public StoryCard getCurrentStoryCard() {
        return currentStoryCard;
    }

    public void setCurrentStoryCard(StoryCard currentStoryCard) {
        this.currentStoryCard = currentStoryCard;
    }

    public ArrayList<Player> getQuestingPlayers() { return questingPlayers; }

    public void setQuestingPlayers(ArrayList<Player> questingPlayers) { this.questingPlayers = questingPlayers; }

    public Player getMainPlayer() { return mainPlayer; }

    public void setMainPlayer(Player mainPlayer) { this.mainPlayer = mainPlayer; }


    //METHODS
    public void addPlayer(Player p){
        players.add(p);
    }

    public ArrayList<Card> getStage(Integer stage){
        if(stage == 1){
            return firstStage;
        }
        else if(stage == 2){
            return secondStage;
        }
        else if(stage == 3){
            return thirdStage;
        }
        else if(stage == 4){
            return fourthStage;
        }
        else if(stage == 5){
            return fifthStage;
        }
        else{
            return null;
        }
    }

    public void addCardToStage(Card c, Integer stage){
        if(stage == 1){
            firstStage.add(c);
        }
        else if(stage == 2){
            secondStage.add(c);
        }
        else if(stage == 3){
            thirdStage.add(c);
        }
        else if(stage == 4){
            fourthStage.add(c);
        }
        else if(stage == 5){
            fifthStage.add(c);
        }
        else{}
    }

    public int getBonusShield() {
        return bonusShield;
    }
    public void setBonusShield(int value){
        this.bonusShield = value;
    }

    public void addQuestingPlayer(Player p){
        questingPlayers.add(p);
    }

    public void resetStages(){
        firstStage = new ArrayList<>();
        secondStage = new ArrayList<>();
        thirdStage = new ArrayList<>();
        fourthStage = new ArrayList<>();
        fifthStage = new ArrayList<>();
    }
}
