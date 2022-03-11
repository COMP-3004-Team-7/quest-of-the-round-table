package comp3004.project.QotRT.cards;

public class Boar extends Card{

    public Boar(){
        this.name = "Boar";
        //realcards.add(this);
        this.MINbattlepoints = 5;
        this.MAXbattlepoints = 15;
        this.bids = 0;
        this.type = "Foe";
        this.specialevent = null;
        //System.out.println("added Boar to the deck of cards");
    }
}
