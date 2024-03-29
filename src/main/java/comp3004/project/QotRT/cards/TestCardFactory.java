package comp3004.project.QotRT.cards;

public class TestCardFactory extends AdventureCardFactory {
    public Card createCard(String item){
        if (item.equals("Test of Morghan Le Fey")) {
            return new TestofMorghanLeFey();
        } else if (item.equals("Test of Temptation")) {
            return new TestofTemptation();
        } else if (item.equals("Test of Valor")) {
            return new TestofValor();
        } else if (item.equals("Test of Questing Beast")) {
            return new TestQuestingBeast();
        }else {
            return null;
        }
    }
}
