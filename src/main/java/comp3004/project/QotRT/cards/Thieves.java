package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.cards.Card;

public class Thieves extends Card {

    public Thieves(){
        this.name = "Thieves";
        //realcards.add(this);
        this.MINbattlepoints = 5;
        this.MAXbattlepoints = 5;
        this.bids = 0;
        this.type = "Foe";
        this.specialevent = null;
        //System.out.println("added Thieves to the deck of cards");
    }
}
