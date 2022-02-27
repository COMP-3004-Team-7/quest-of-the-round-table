package comp3004.project.QotRT;

public class Sword extends Card{
    public Sword() {
        name = "Sword";
        realcards.add(this);
        System.out.println("added Sword to the deck of cards");
    }
}
