package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.cards.Card;

public class RobberKnight extends Card {
    public RobberKnight(){
        this.name = "Robber Knight";
        //realcards.add(this);
        this.MAXbattlepoints = 15;
        this.MINbattlepoints = 15;
        this.bids = 0;
        this.type = "Foe";
        this.specialevent = null;
        //System.out.println("added Robber Knight to the deck of cards");
    }

}
