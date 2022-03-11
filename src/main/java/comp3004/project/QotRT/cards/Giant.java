package comp3004.project.QotRT.cards;

public class Giant extends Card{

    public Giant(){
        this.name = "Giant";
        //realcards.add(this);
        this.MAXbattlepoints = 40;
        this.MINbattlepoints = 40;
        this.bids = 0;
        this.type = "Foe";
        this.specialevent = null;
        //System.out.println("added Giant to the deck of cards");
    }
}
