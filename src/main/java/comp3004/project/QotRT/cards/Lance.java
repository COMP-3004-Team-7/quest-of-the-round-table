package comp3004.project.QotRT.cards;

public class Lance extends Card{
    public Lance() {
        name = "Lance";
        //realcards.add(this);
        //battlepoints = 20;
        this.MAXbattlepoints = 20;
        this.MINbattlepoints = 20;
        bids = 0;
        specialevent = null;
        System.out.println("added Lance to the deck of cards");
    }
}
