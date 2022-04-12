package comp3004.project.QotRT.service;

import comp3004.project.QotRT.cards.*;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.controller.stratPatternNewStory.NewStoryCardDealer;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service

public class EventService {
    private final NewStoryCardDealer newStoryCardDealer = new NewStoryCardDealer();

    public void doEvent(Game game, SimpMessagingTemplate simpMessagingTemplate) throws InterruptedException {
        if(game.getCurrentStoryCard() instanceof ChivalrousDeed) {
            String lowRank = "";
            int leastNumShields = 0;
            ArrayList<Player> playerArrayList = new ArrayList<>();
            HashMap<String, Integer> ranks = new HashMap<>();
            ranks.put("Squire", 1);
            ranks.put("Knight", 2);
            ranks.put("Champion Knight", 3);
            ranks.put("Knight of the Round Table", 4);

            //GETTING THE LOW RANK PLAYERS AND STORING IT IN A LIST
            for (int i = 0; i < game.getPlayers().size(); i++) {
                String currRank = game.getPlayers().get(i).getRank();
                if (i == 0) {
                    lowRank = currRank;
                    playerArrayList.add(game.getPlayers().get(i));
                } else {
                    if (ranks.get(currRank) == ranks.get(lowRank)) {
                        playerArrayList.add(game.getPlayers().get(i));
                    } else if (ranks.get(currRank) < ranks.get(lowRank)) {
                        playerArrayList = new ArrayList<>();
                        playerArrayList.add(game.getPlayers().get(i));
                        lowRank = currRank;
                    }
                }
            }
            leastNumShields = playerArrayList.get(0).getShields();
            for (int i=1; i < playerArrayList.size(); i++){
                if(leastNumShields>playerArrayList.get(i).getShields()){
                   playerArrayList.remove(i);
                   i--;
                }
            }

            //Possible game winners array list
            ArrayList<Player> gameWinners = new ArrayList<>();
            //Update shields and ranks
            for (int i=0 ; i<playerArrayList.size();i++){
                playerArrayList.get(i).setShields(playerArrayList.get(i).getShields() + 3);
                playerArrayList.get(i).setRank();
                //Todo SEND MESSAGE TO UPDATE PLAYER RANK (IF CHANGED)
                if(playerArrayList.get(i).getRank().equals("Knight")){
                    gameWinners.add(playerArrayList.get(i));
                }
            }
            //Check for winners and send them message, otherwise draw new story card
            if(gameWinners.size() > 0) {
                String winnerString = "";
                for (int i = 0; i < gameWinners.size(); i++) {
                    winnerString += gameWinners.get(i).getUsername();
                    if (i == gameWinners.size() - 1) {
                        winnerString += " ";
                    } else {
                        winnerString += " and ";
                    }
                }
                simpMessagingTemplate.convertAndSend("/topic/game-winner/" + game.getGameId(), winnerString + "won the game!");
            }
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
        //All players except player drawing this card lose 1 shield
        else if(game.getCurrentStoryCard() instanceof Pox){
            for(int i = 0; i < game.getPlayers().size(); i++){
                if(!game.getMainPlayer().getUsername().equals(game.getPlayers().get(i).getUsername())){
                    if(game.getPlayers().get(i).getShields() >= 1){
                        game.getPlayers().get(i).setShields(game.getPlayers().get(i).getShields()-1);
                        //Send simp message to player
                    }
                }
            }
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
        //Drawer loses 2 shields if possible
        else if(game.getCurrentStoryCard() instanceof Plague){
            if(game.getMainPlayer().getShields() >= 2){
                game.getMainPlayer().setShields(game.getMainPlayer().getShields()-2);
                //TODO Send simp message to player
            }
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
        else if(game.getCurrentStoryCard() instanceof KingsArms){
            //FIND THE HIGHEST RANK PLAYER
            //THEN SEND MESSAGE TO THAT PLAYER TO DISCARD ONE OF THEIR WEAPON CARDS
            //IF THAT CANNOT BE DONE - THEN DISCARD 2 FOE CARD

            //QUESTION ?? WHAT HAPPENS IF THE PLAYER DOES NOT HAVE 2 FOE CARDS?
            //ASSUMPTION - IF THE PLAYER HAS LESS THAN 2 CARDS, DISCARD WHATEVER YOU HAVE I.E., THE ONE FOE CARD

            ArrayList<Player> highRankingPlayers = new ArrayList<>();
            HashMap<String, Integer> ranks = new HashMap<>();
            ranks.put("Squire",1);
            ranks.put("Knight",2);
            ranks.put("Champion Knight",3);
            ranks.put("Knight of the Round Table",4);

            int highestCurrRank =  1;

            for (int i =0; i<game.getPlayers().size(); i++){
                if(highestCurrRank == ranks.get(game.getPlayers().get(i).getRank())){
                    highRankingPlayers.add(game.getPlayers().get(i));
                }
                else if(highestCurrRank > ranks.get(game.getPlayers().get(i).getRank())){
                    highRankingPlayers = new ArrayList<>();
                    highestCurrRank = ranks.get(game.getPlayers().get(i).getRank());
                    highRankingPlayers.add(game.getPlayers().get(i));
                }
            }

            int numFoes = 0;
            HashMap<Player,Integer> numFoeCardsForHighPlayers = new HashMap<>();
            boolean flag = false;
            for (int i=0 ; i<highRankingPlayers.size(); i++){
                numFoeCardsForHighPlayers.put(highRankingPlayers.get(i),numFoes);
                for (int j=0; j<highRankingPlayers.get(i).getCards().size(); j++){
                    if (highRankingPlayers.get(i).getCards().get(j).getType().equals("Weapon")){
                        //TODO
                        //SEND MESSAGE TO USER TO DISCARD A WEAPON CARD
                        //simpMessagingTemplate.convertAndSendToUser();
                        flag = true;
                        break;
                    }
                    else if (highRankingPlayers.get(i).getCards().get(j).getType().equals("Foe")){
                        numFoes ++;
                        numFoeCardsForHighPlayers.replace(highRankingPlayers.get(i),numFoes);
                    }
                }
                if (!flag){
                    if (numFoeCardsForHighPlayers.get(highRankingPlayers.get(i)) > 2) {
                        //TODO
                        //DISCARD 2 FOE CARDS
                        //SEND MESSAGE TO USER TO DISCARD 2 FOE CARDS
                    } else {
                        //SEND MESSAGE TO USER THAT WE DISCARDED YOUR FOE CARD!
                        for (int j = 0; j < highRankingPlayers.get(i).getCards().size(); j++) {
                            if (highRankingPlayers.get(i).getCards().get(j).getType().equals("Foe")) {
                                highRankingPlayers.get(i).getCards().remove(j);
                                j--;
                            }
                        }
                        simpMessagingTemplate.convertAndSendToUser(highRankingPlayers.get(i).getName(),
                                "/topic/cards-in-hand/" + game.getGameId(), highRankingPlayers.get(i).getCards());
                    }
                }
                numFoes = 0;
            }
        }
        //Lowest ranked player(s) draw 2 adventure cards
        else if(game.getCurrentStoryCard() instanceof QueensFavor){
            //Get lowest ranked player(s)
            ArrayList<Player> lowestRankedPlayers = new ArrayList<>();
            int lowestRank = 99999;
            for(int i = 0; i < game.getPlayers().size(); i++){
                if(game.getPlayers().get(i).getBattlePoints() < lowestRank){
                    lowestRankedPlayers.clear();
                    lowestRankedPlayers.add(game.getPlayers().get(i));
                    lowestRank = game.getPlayers().get(i).getBattlePoints();
                }
                else if(game.getPlayers().get(i).getBattlePoints().equals(lowestRank)){
                    lowestRankedPlayers.add(game.getPlayers().get(i));
                }
            }
            //Give them 2 cards each
            for(int i = 0; i < lowestRankedPlayers.size(); i++){
                Card c1 = game.getAdventureDeck().drawCard();
                Card c2 = game.getAdventureDeck().drawCard();
                lowestRankedPlayers.get(i).getCards().add(c1); lowestRankedPlayers.get(i).getCards().add(c2);
                simpMessagingTemplate.convertAndSendToUser(lowestRankedPlayers.get(i).getName(),
                        "/topic/cards-in-hand/"+game.getGameId(), lowestRankedPlayers.get(i).getCards());
            }
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
        //All allies in play must be discarded
        else if(game.getCurrentStoryCard() instanceof CourtCamelot){
            for(int i =0; i < game.getPlayers().size(); i++){
                game.getPlayers().get(i).setAllies(new ArrayList<>());
                //Send Simp Message to the player with updated allies (i.e. none)
            }
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
        else if(game.getCurrentStoryCard() instanceof KingsRecognition){
            game.setBonusShield(2);
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
        //All players draw 2 adventure cards
        else if(game.getCurrentStoryCard() instanceof ProsperityRealm){
            for(int i = 0; i < game.getPlayers().size(); i++){
                Card c1 = game.getAdventureDeck().drawCard();
                Card c2 = game.getAdventureDeck().drawCard();
                game.getPlayers().get(i).getCards().add(c1); game.getPlayers().get(i).getCards().add(c2);
                simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(i).getName(),
                        "/topic/cards-in-hand/"+game.getGameId(), game.getPlayers().get(i).getCards());
            }
            newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        }
    }

    public ResponseEntity discardWeapon(String gameId, DiscardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) throws InterruptedException {
        System.out.println("discard weapon card request");
        System.out.println("PLAYER: " + request.getPlayer());
        System.out.println("GAMEID: " + request.getGameId());
        Game game = gameService.getGame(request.getGameId());
        Card discarded;
        if (!request.getCard().getType().equals("Weapon")){
            return ResponseEntity.badRequest().body("ERROR:THE CARD IS NOT A WEAPON");
        }
        //Remove discarded cards from players hand and move to adventure deck discard pile
        for (int i = 0 ; i < game.getPlayers().size(); i++){
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                // Card discardedCard = game.getPlayers().get(i).getCards().remove(//INDEX)
                for (int j = 0; j<game.getPlayers().get(i).getCards().size(); j++){
                    if (game.getPlayers().get(i).getCards().get(j).getName().equals(request.getCard().getName())){
                        discarded = game.getPlayers().get(i).getCards().remove(j);
                        game.getAdventureDeck().discardCard(discarded);
                        break;
                    }
                }
                //Send card back to player
                simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+request.getGameId()+"/"+
                        game.getPlayers().get(i).getUsername(), game.getPlayers().get(i).getCards());
                break;
            }
        }
        simpMessagingTemplate.convertAndSend("/topic/discard-pile/" + request.getGameId(), game.getAdventureDeck().getDiscardPile());

        newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        return ResponseEntity.ok().body("");
    }

    public ResponseEntity discardFoe(String gameId, DiscardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) throws InterruptedException {
        System.out.println("discard weapon card request");
        System.out.println("PLAYER: " + request.getPlayer());
        System.out.println("GAMEID: " + request.getGameId());
        Game game = gameService.getGame(request.getGameId());
        Card discarded;
        if (!request.getCard().getType().equals("Foe")){
            return ResponseEntity.badRequest().body("ERROR:THE CARD IS NOT A FOE");
        }
        //Remove discarded cards from players hand and move to adventure deck discard pile
        for (int i = 0 ; i < game.getPlayers().size(); i++){
            if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                // Card discardedCard = game.getPlayers().get(i).getCards().remove(//INDEX)
                for (int j = 0; j<game.getPlayers().get(i).getCards().size(); j++){
                    if (game.getPlayers().get(i).getCards().get(j).getName().equals(request.getCard().getName())){
                        discarded = game.getPlayers().get(i).getCards().remove(j);
                        game.getAdventureDeck().discardCard(discarded);
                        break;
                    }
                }
                //Send card back to player
                simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+request.getGameId()+"/"+
                        game.getPlayers().get(i).getUsername(), game.getPlayers().get(i).getCards());
                break;
            }
        }
        simpMessagingTemplate.convertAndSend("/topic/discard-pile/" + request.getGameId(), game.getAdventureDeck().getDiscardPile());

        newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
        return ResponseEntity.ok().body("");
    }


    //ATTEMPT AT CONSOLIDATING THE 2 DISCARD METHODS ABOVE (DISCARDFOE & DISCARDWEAPON)

//    public ResponseEntity discardCard(String gameId, DiscardRequest request, SimpMessagingTemplate simpMessagingTemplate, GameService gameService) {
//        Game game = gameService.getGame(request.getGameId());
//        Card discarded;
//        //Check if they have a weapon -> if they do, then their request should be trying to discard a weapon. otherwise a foe
//        for(int i = 0; i < game.getPlayers().size(); i++){
//            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
//                for(int j = 0; j < game.getPlayers().get(i).getCards().size(); j++){
//                    if(game.getPlayers().get(i).getCards().get(j).getType().equals("Weapon") && !request.getCard().getType().equals("Weapon")){
//                        return ResponseEntity.badRequest().body("You have a weapon.. You must discard 1 weapon");
//                    }
//                }
//            }
//        }
//        //Check if discarding weapon -> send back all is good, no more discarding
//        if(request.getCard().getType().equals("Weapon") || request.getCard().getType().equals("Foe")){
//            //Remove discarded cards from players hand and move to adventure deck discard pile
//            for (int i = 0 ; i < game.getPlayers().size(); i++){
//                if (game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
//                    for (int j = 0; j<game.getPlayers().get(i).getCards().size(); j++){
//                        if (game.getPlayers().get(i).getCards().get(j).getName().equals(request.getCard().getName())){
//                            discarded = game.getPlayers().get(i).getCards().remove(j);
//                            game.getAdventureDeck().discardCard(discarded);
//                            break;
//                        }
//                    }
//                    //Send card back to player
//                    simpMessagingTemplate.convertAndSend("/topic/cards-in-hand/"+request.getGameId()+"/"+
//                            game.getPlayers().get(i).getUsername(), game.getPlayers().get(i).getCards());
//                    break;
//                }
//            }
//            simpMessagingTemplate.convertAndSend("/topic/discard-pile/" + request.getGameId(), game.getAdventureDeck().getDiscardPile());
//            if(request.getCard().getType().equals("Foe")){
//                //Todo need some way to tell if this is first discarded foe or 2nd
//                return ResponseEntity.ok().body("Successfully discarded your weapon");
//            }
//            else{
//                newStoryCardDealer.dealWithNewStoryCard(game,simpMessagingTemplate);
//                return ResponseEntity.ok().body("Successfully discarded your weapon");
//            }
//        }
//        //Have to discard foe or weapon
//        else{
//            return ResponseEntity.badRequest().body("Can only discard weapon or 2 foes (if you have them)");
//        }
//    }
}
