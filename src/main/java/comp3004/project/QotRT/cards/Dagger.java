package comp3004.project.QotRT.cards;

public class Dagger extends Card{
    public Dagger() {
        name = "Dagger";
        //battlepoints = 5;
        this.MINbattlepoints = 5;
        this.MAXbattlepoints = 5;
        bids = 0;
        specialevent = null;
       // realcards.add(this);
        System.out.println("added Dagger to the deck of cards");
    }
}
