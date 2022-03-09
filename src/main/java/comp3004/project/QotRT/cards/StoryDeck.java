package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.model.AdventureDeck;

import java.util.ArrayList;
import java.util.Collections;

public class StoryDeck {
    private ArrayList<StoryCard> deck;
    private ArrayList<StoryCard> discardPile;
    //Quest card
    private static final Integer numHolyGrail = 1;
    private static final Integer numGreenKnight = 1;
    private static final Integer numQuestingBeast = 1;
    private static final Integer numQueensHonor = 1;
    private static final Integer numFairMaiden = 1;
    private static final Integer numEnchantedForest = 1;
    private static final Integer numKingArthurEnemy = 2;
    private static final Integer numSlayDragon = 1;
    private static final Integer numBoarHunt = 2;
    private static final Integer numSaxonRaiders = 2;

    public StoryDeck(){
        deck = new ArrayList<>();
        discardPile = new ArrayList<>();
    }

    //Start of Game Deck
    public StoryDeck buildStoryDeck(){
        //Weapon Cards
        QuestCardFactory questCardFactory = new QuestCardFactory();
        for(int i = 0; i < numHolyGrail; i++){
            deck.add(questCardFactory.createCard("Search for the Holy Grail"));
        }
        for(int i = 0; i < numGreenKnight; i++){
            deck.add(questCardFactory.createCard("Test of the Green Knight"));
        }
        for(int i = 0; i < numQuestingBeast; i++){
            deck.add(questCardFactory.createCard("Search for the Questing Beast"));
        }
        for(int i = 0; i < numQueensHonor; i++){
            deck.add(questCardFactory.createCard("Defend the Queen's Honor"));
        }
        for(int i = 0; i < numFairMaiden; i++){
            deck.add(questCardFactory.createCard("Rescue the Fair Maiden"));
        }
        for(int i = 0; i < numEnchantedForest; i++){
            deck.add(questCardFactory.createCard("Journey through the Enchanted Forest"));
        }
        for(int i = 0; i < numKingArthurEnemy; i++){
            deck.add(questCardFactory.createCard("Vanquish King Arthur's Enemies"));
        }
        for(int i = 0; i < numSlayDragon; i++){
            deck.add(questCardFactory.createCard("Slay the Dragon"));
        }
        for(int i = 0; i < numBoarHunt; i++){
            deck.add(questCardFactory.createCard("Boar Hunt"));
        }
        for(int i = 0; i < numSaxonRaiders; i++){
            deck.add(questCardFactory.createCard("Repel the Saxon Raiders"));
        }
        return this;
    }

    public void shuffle(){
        Collections.shuffle(deck);
    }

    //Draw Top Card of Deck
    public StoryCard drawCard(){
        return deck.remove(deck.size()-1);
    }

    //Discard Card (i.e. add to discard pile)
    public void discardCard(StoryCard c){
        discardPile.add(c);
    }

    public ArrayList<StoryCard> getDeck() {
        return deck;
    }

    public void setDeck(ArrayList<StoryCard> deck) {
        this.deck = deck;
    }

    public ArrayList<StoryCard> getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(ArrayList<StoryCard> discardPile) {
        this.discardPile = discardPile;
    }
}
