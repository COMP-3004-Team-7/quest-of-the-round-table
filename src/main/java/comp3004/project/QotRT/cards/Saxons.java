package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.cards.Card;

public class Saxons extends Card {

    public Saxons(){
        this.name = "Saxons";
        //realcards.add(this);
        this.MINbattlepoints= 10;
        this.MAXbattlepoints = 20;
        this.type = "Foe";
        this.bids = 0;
        this.specialevent = null;
        //System.out.println("added Saxons to the deck of cards");
    }
}
