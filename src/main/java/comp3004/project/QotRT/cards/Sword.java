package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.cards.Card;

public class Sword extends Card {
    public Sword() {
        name = "Sword";
        //realcards.add(this);
        //battlepoints = 10;
        this.MAXbattlepoints = 10;
        this.MINbattlepoints = 10;
        this.type = "Weapon";
        bids = 0;
        specialevent = null;
        System.out.println("added Sword to the deck of cards");
    }
}
