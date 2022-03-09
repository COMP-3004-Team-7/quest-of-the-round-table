package comp3004.project.QotRT.cards;

public class Excalibur extends Card{
    public Excalibur() {
        name = "Excalibur";
        //battlepoints = 30;
        this.MINbattlepoints = 30;
        this.MAXbattlepoints = 30;
        bids = 0;
        specialevent = null;
        //realcards.add(this);
        System.out.println("added excalibar to the deck of cards");
    }
}
