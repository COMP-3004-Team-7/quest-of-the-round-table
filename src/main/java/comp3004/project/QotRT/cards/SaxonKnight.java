package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.cards.Card;

public class SaxonKnight extends Card {

    public SaxonKnight(){
        this.name = "Saxon Knight";
        //realcards.add(this);
        this.MINbattlepoints = 15;
        this.MAXbattlepoints = 25;
        this.type = "Foe";
        this.bids = 0;
        this.specialevent = null;
       // System.out.println("added Saxon Knight to the deck of cards");
    }
}
