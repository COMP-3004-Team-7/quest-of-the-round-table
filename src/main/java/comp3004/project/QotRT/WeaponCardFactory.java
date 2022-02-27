package comp3004.project.QotRT;

public class WeaponCardFactory extends AdventureCardFactory{
    Card createCard(String item){
        if (item.equals("Horse")) {
            return new Horse();
        } else if (item.equals("Sword")) {
            return new Sword();
        } else if (item.equals("Lance")) {
            return new Lance();
        } else if (item.equals("Dagger")) {
            return new Dagger();
        } else if(item.equals("Excalibar")) {
            return new Excalibar();
        }else if(item.equals("BattleAx")){
            return new BattleAx();
        } else {
            return null;
        }
    }
}
