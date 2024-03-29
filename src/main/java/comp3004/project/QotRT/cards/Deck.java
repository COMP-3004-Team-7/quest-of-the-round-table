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
        //adding test cards to the adventure deck.
        for (int i = 0; i<8 ; i++) { //ADDING WEAPON CARDS
            if (i < 2) {
                cardList.add(new TestofValor());
            } else if (i < 4) {
                cardList.add(new TestofTemptation());
            } else if (i < 6) {
                cardList.add(new TestofMorghanLeFey());
            }else {
                cardList.add(new TestQuestingBeast());
            }
        }
        //adding amour cards to the adventure cards
        for (int i = 0; i<8 ; i++) {
            cardList.add(new Amour());
        }
        //adding ally cards to the adventure card
        for (int i = 0; i<1 ; i++) {
            cardList.add(new KingArthur());
            cardList.add(new KingPellinore());
            cardList.add(new Merlin());
            cardList.add(new QueenGuinevere());
            cardList.add(new QueenIseult());
            cardList.add(new SirPercival());
            cardList.add(new SirTristan());
            cardList.add(new SirGawain());
            cardList.add(new SirGalahad());
            cardList.add(new SirLancelot());
        }


        return cardList;
    }
}
