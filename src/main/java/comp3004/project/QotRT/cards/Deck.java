package comp3004.project.QotRT.cards;

import java.util.ArrayList;

public class Deck {


    public ArrayList<Card> createDeck () {
        ArrayList <Card> cardList = new ArrayList<>();

        for (int i = 0; i<49 ; i++){ //ADDING WEAPON CARDS
            if (i<2){
                cardList.add(new Excalibur());
            }
            else if (i<8){
                cardList.add(new Lance());
            }
            else if (i<16){
                cardList.add(new BattleAx());
            }
            else if (i<32){
                cardList.add(new Sword());
            }
            else if (i<43){
                cardList.add(new Horse());
            }
            else{
                cardList.add(new Dagger());
            }
        }

        for (int i = 0; i<50 ; i++){ //ADDING FOE CARDS
            if (i<1){
                cardList.add(new Dragon());
            }
            else if (i<3){
                cardList.add(new Giant());
            }
            else if (i<7){
                cardList.add(new Mordred());
            }
            else if (i<9){
                cardList.add(new GreenKnight());
            }
            else if (i<12){
                cardList.add(new BlackKnight());
            }
            else if (i<18){
                cardList.add(new EvilKnight());
            }
            else if (i<26){
                cardList.add(new SaxonKnight());
            }
            else if (i<33){
                cardList.add(new RobberKnight());
            }
            else if (i<38){
                cardList.add(new Saxons());
            }
            else if (i<42){
                cardList.add(new Boar());
            }
            else{
                cardList.add(new Thieves());
            }
        }
        return cardList;
    }
}
