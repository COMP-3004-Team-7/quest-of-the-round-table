package comp3004.project.QotRT.cards;

public class FoeCardFactory extends AdventureCardFactory{
    public Card createCard(String item){
        if (item.equals("Green Knight")) {
            return new GreenKnight();
        }
        else if (item.equals("Mordred")){
            return new Mordred();
        }
        else if (item.equals("Robber Knight")){
            return new RobberKnight();
        }
        else if (item.equals("Evil Knight")){
            return new EvilKnight();
        }
        else if (item.equals("Giant")){
            return new Giant();
        }
        else if (item.equals("Saxon Knight")){
            return new SaxonKnight();
        }
        else if (item.equals("Saxons")){
            return new Saxons();
        }
        else if (item.equals("Thieves")){
            return new Thieves();
        }
        else if (item.equals("Black Knight")){
            return new BlackKnight();
        }
        else if (item.equals("Boar")){
            return new Boar();
        }
        else if (item.equals("Dragon")){
            return new Dragon();
        }
        return null;
        }
}

