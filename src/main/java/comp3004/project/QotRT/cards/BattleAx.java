package comp3004.project.QotRT.cards;

public class BattleAx extends Card{
    public BattleAx() {
        name = "Battle-ax";
        //realcards.add(this);
        //battlepoints = 15;
        this.MINbattlepoints = 15;
        this.MAXbattlepoints = 15;
        bids = 0;
        specialevent = null;
        System.out.println("added battle-ax to the deck of cards");
    }
}
