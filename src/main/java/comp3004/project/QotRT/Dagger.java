package comp3004.project.QotRT;

public class Dagger extends Card{
    public Dagger() {
        name = "Dagger";
        realcards.add(this);
        System.out.println("added Dagger to the deck of cards");
    }
}
