package comp3004.project.QotRT.cards;

public class Horse extends Card{
    public Horse() {
        name = "Horse";
        //realcards.add(this);
        //battlepoints = 10;
        this.MINbattlepoints = 10;
        this.MAXbattlepoints = 10;
        bids = 0;
        this.type = "Weapon";
        specialevent = null;
        System.out.println("added Horse to the deck of cards");
    }
}
