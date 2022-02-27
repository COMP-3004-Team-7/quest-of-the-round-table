package comp3004.project.QotRT;

public class Horse extends Card{
    public Horse() {
        name = "Horse";
        realcards.add(this);
        System.out.println("added Horse to the deck of cards");
    }
}
