package comp3004.project.QotRT;

public class AmourCardFactory extends AdventureCardFactory{
    Card createCard(String item){
            if (item.equals("Amour")) {
                return new Amour();
            }else{
                return null;
            }
    }
}