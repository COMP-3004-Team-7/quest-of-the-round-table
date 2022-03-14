package comp3004.project.QotRT.cards;

public class EventCardFactory extends StoryCardFactory{
    public StoryCard createCard(String item){
        if (item.equals("Chivalrous Deed")) {
            return new ChivalrousDeed();
        } else if (item.equals("Pox")) {
            return new Pox();
        } else if (item.equals("Plague")) {
            return new Plague();
        } else if (item.equals("King's Recognition")) {
            return new KingsRecognition();
        }else if (item.equals("Queen's Favor")) {
            return new Queenhonor();
        } else if (item.equals("Court Called to Camelot")) {
            return new CourtCamelot();
        } else if (item.equals("King's Call to Arms")) {
            return new KingsArms();
        }else if (item.equals("Prosperity Throughout the Realm")) {
            return new ProsperityRealm();
        }else {
            return null;
        }
    }
}
