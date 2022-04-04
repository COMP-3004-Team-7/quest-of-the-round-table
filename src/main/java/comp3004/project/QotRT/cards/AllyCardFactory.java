package comp3004.project.QotRT.cards;

public class AllyCardFactory extends AdventureCardFactory{
    public Card createCard(String item){
        if (item.equals("Queen Iseult")) {
            return new QueenIseult();
        }
        else if (item.equals("Merlin")){
            return new Merlin();
        }
        else if (item.equals("Sir Galahad")){
            return new SirGalahad();
        }
        else if (item.equals("Sir Lancelot")){
            return new SirLancelot();
        }
        else if (item.equals("Sir Tristan")){
            return new SirTristan();
        }
        else if (item.equals("Sir Percival")){
            return new SirPercival();
        }
        else if (item.equals("Sir Gawain")){
            return new SirGawain();
        }
        else if (item.equals("Queen Guinevere")){
            return new QueenGuinevere();
        }
        else if (item.equals("King Arthur")){
            return new KingArthur();
        }
        else if (item.equals("King Pellinore")){
            return new KingPellinore();
        }
        else {
            return null;
        }
    }
}
