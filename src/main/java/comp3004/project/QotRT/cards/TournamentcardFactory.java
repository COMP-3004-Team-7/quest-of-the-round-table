package comp3004.project.QotRT.cards;

import org.apache.logging.log4j.message.ThreadDumpMessage;

public class TournamentcardFactory extends StoryCardFactory{
    public StoryCard createCard(String item){

        if (item.equals("At Camelot")) {
            return new Camelot();
        } else if (item.equals("At Orkney")) {
            return new Orkney();
        } else if (item.equals("At Tintagel")) {
            return new Tintagel();
        } else if (item.equals("At York")) {
            return new York();
        }else{
            return null;
        }
    }

}
