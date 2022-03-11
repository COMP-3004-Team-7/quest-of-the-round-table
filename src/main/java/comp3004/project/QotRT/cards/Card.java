package comp3004.project.QotRT.cards;

import comp3004.project.QotRT.model.Player;

import java.util.ArrayList;
import java.util.Random;

public class Card {
    String name;
    int MAXbattlepoints;
    int MINbattlepoints;
    int bids;
    String specialevent;
    String type;
    public String getName() {
        return name;

    }


    public int getMAXbattlepoints() {
        return MAXbattlepoints;
    }

    public int getMINbattlepoints() {
        return MINbattlepoints;
    }

    public int getBids() {
        return bids;
    }

    public String getSpecialevent() {
        return specialevent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMAXbattlepoints(int MAXbattlepoints) {
        this.MAXbattlepoints = MAXbattlepoints;
    }

    public void setMINbattlepoints(int MINbattlepoints) {
        this.MINbattlepoints = MINbattlepoints;
    }

    public void setBids(int bids) {
        this.bids = bids;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public void setSpecialevent(String specialevent) {
        this.specialevent = specialevent;
    }

    public String toString() {
        StringBuffer display = new StringBuffer();
        display.append("---- " + name + " ----\n");
        /*
        for (Card card : realcards) {
            display.append(card + "\n");
        }

         */
        return display.toString();
    }
}



/*
    public Card() {
        for (int i = 0; i < 32; i++){
            cards.add(arr[i]);
        }
        Random rand = new Random();//instance of random class
        int upperbound = 32;
        //players.add(new Player());
        for(int i = 0; i<players.size(); i++) {
            for(int j = 0; j<12; j++) {
                //generate random vales from 0-32
                int random = rand.nextInt(upperbound);
                players.get(0).getPlayercards().add(arr[random]);
            }
        }

    }
ArrayList<String> cards = new ArrayList<String>();
    ///ArrayList<Card> realcards = new ArrayList<Card>();
   // ArrayList<Player> players = new ArrayList<Player>();
    String arr[] = {"Horse+10","Sword+10","Dagger+5","Excalibar+30","Lance+20",
                    "Battle-ax+15","Test of the Questing Beast","Test of temptation",
                    "Test of Valor","Test of Morghan Le Fey","Robber Knight +15", "Saxons +10/20",
                    "Boar+5/15","Thieves+5","Green Knight +25/40","Black Knight+25/35","Evil Knight +20/30",
                    "Saxons Knight+15/25","Dragon+50/70","Giant+40","Mordered+30","Sir Gawain+10",
                    "King Pellinore+10","Sir Percival+5","Amour+10(1 bid)","Sir Tristan+10",
                    "King Arthur+10","Queen Guinevere","Merlin","Queen Iseult","Sir Lancelot+15","Sir Galahad+15"
 */