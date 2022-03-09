package comp3004.project.QotRT.model;

import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.FoeCardFactory;
import comp3004.project.QotRT.cards.WeaponCardFactory;

import java.util.ArrayList;
import java.util.Collections;

public class AdventureDeck {
    private ArrayList<Card> deck;
    private ArrayList<Card> discardPile;
    //Weapon Numbers
    private static final Integer numHorse = 11;
    private static final Integer numSword = 16;
    private static final Integer numDagger = 6;
    private static final Integer numExcalibur = 2;
    private static final Integer numLance = 6;
    private static final Integer numBattleAx = 8;
    //Foe Numbers
    private static final Integer numDragon = 1;
    private static final Integer numGiant = 2;
    private static final Integer numMordred = 4;
    private static final Integer numGreenKnight = 2;
    private static final Integer numBlackKnight = 3;
    private static final Integer numEvilKnight = 6;
    private static final Integer numSaxonKnight = 8;
    private static final Integer numRobberKnight = 7;
    private static final Integer numSaxons = 5;
    private static final Integer numBoar = 4;
    private static final Integer numThieves = 8;

    public AdventureDeck(){
        deck = new ArrayList<>();
        discardPile = new ArrayList<>();
    }

    //Start of Game Deck
    public AdventureDeck buildStartingDeck(){
        //Weapon Cards
        WeaponCardFactory weaponFactory = new WeaponCardFactory();
        for(int i = 0; i < numHorse; i++){
            deck.add(weaponFactory.createCard("Horse"));
        }
        for(int i = 0; i < numSword; i++){
            deck.add(weaponFactory.createCard("Sword"));
        }
        for(int i = 0; i < numDagger; i++){
            deck.add(weaponFactory.createCard("Dagger"));
        }
        for(int i = 0; i < numExcalibur; i++){
            deck.add(weaponFactory.createCard("Excalibur"));
        }
        for(int i = 0; i < numLance; i++){
            deck.add(weaponFactory.createCard("Lance"));
        }
        for(int i = 0; i < numBattleAx; i++){
            deck.add(weaponFactory.createCard("BattleAx"));
        }

        //Foe Cards
        FoeCardFactory foeFactory = new FoeCardFactory();
        for(int i = 0; i < numDragon; i++){
            deck.add(foeFactory.createCard("Dragon"));
        }
        for(int i = 0; i < numGiant; i++){
            deck.add(foeFactory.createCard("Giant"));
        }
        for(int i = 0; i < numMordred; i++){
            deck.add(foeFactory.createCard("Mordred"));
        }
        for(int i = 0; i < numGreenKnight; i++){
            deck.add(foeFactory.createCard("Green Knight"));
        }
        for(int i = 0; i < numBlackKnight; i++){
            deck.add(foeFactory.createCard("Black Knight"));
        }
        for(int i = 0; i < numEvilKnight; i++){
            deck.add(foeFactory.createCard("Evil Knight"));
        }
        for(int i = 0; i < numSaxonKnight; i++){
            deck.add(foeFactory.createCard("Saxon Knight"));
        }
        for(int i = 0; i < numRobberKnight; i++){
            deck.add(foeFactory.createCard("Robber Knight"));
        }
        for(int i = 0; i < numSaxons; i++){
            deck.add(foeFactory.createCard("Saxons"));
        }
        for(int i = 0; i < numBoar; i++){
            deck.add(foeFactory.createCard("Boar"));
        }
        for(int i = 0; i < numThieves; i++){
            deck.add(foeFactory.createCard("Thieves"));
        }
        return this;
    }

    //Shuffle Deck
    public void shuffleDeck(){
        Collections.shuffle(deck);
    }

    //Move discardPile to Deck and shuffle
    public void discardToDeck(){

    }

    //Draw Top Card of Deck
    public Card drawCard(){
        return deck.remove(deck.size()-1);
    }

    //Discard Card (i.e. add to discard pile)
    public void discardCard(Card c){
        discardPile.add(c);
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    public void setDeck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    public ArrayList<Card> getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(ArrayList<Card> discardPile) {
        this.discardPile = discardPile;
    }
}
