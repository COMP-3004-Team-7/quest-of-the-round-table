package comp3004.project.QotRT.cards;

public class QuestCardFactory extends StoryCardFactory{
    public StoryCard createCard(String item){
        if (item.equals("Vanquish King Arthur's Enemies")) {
            return new ArthurEnemies();
        } else if (item.equals("Journey through the Enchanted Forest")) {
            return new EnchantedForest();
        } else if (item.equals("Rapel the Saxon Raiders")) {
            return new SaxonRaiders();
        } else if (item.equals("Boar Hunt")) {
            return new BoarHunt();
        }else if (item.equals("Search for the Questing Beast")) {
            return new QuestingBeast();
        } else if (item.equals("Defend the Queen's Honor")) {
            return new Queenhonor();
        } else if (item.equals("Slay the Dragon")) {
            return new SlayDragon();
        }else if(item.equals("Rescue the Fair Maiden")) {
            return new RescueFairMaiden();
        }else if (item.equals("Search for the Holy Grail")) {
            return new HolyGrail();
        }else if(item.equals("Test of the Green Knight")) {
            return new GreenKnightQuest();
        }else {
            return null;
        }
    }
}

