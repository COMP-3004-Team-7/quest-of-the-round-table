package comp3004.project.QotRT.service;

import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.model.Game;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class QuestService {


    public String declineQuest(String gameId , ConnectRequest request , SimpMessagingTemplate simpMessagingTemplate, GameService gameService){
        Game game = gameService.getGame(gameId);
        int index = 0;
        int sizeOfPlayersList = game.getPlayers().size();
        for (int i=0; i<game.getPlayers().size(); i++){
            if(game.getPlayers().get(i).getUsername().equals(request.getPlayer().getUsername())){
                index = i;
                break;
            }
        }
        // Checking if everyone declines Quest
        // Mod 4 for each index -> circular array
        if ((index+1) %sizeOfPlayersList == game.getPlayers().indexOf(game.getMainPlayer())){
            game.getStoryDeck().discardCard(game.getCurrentStoryCard()); //Discarded card
            game.setCurrentStoryCard(game.getStoryDeck().drawCard());

            game.setMainPlayer(game.getPlayers().get((game.getPlayers().indexOf(game.getMainPlayer())+1)%sizeOfPlayersList));
            System.out.println(game.getMainPlayer().getUsername());

            simpMessagingTemplate.convertAndSend("/topic/display-story-card/"+gameId, game.getCurrentStoryCard());

            simpMessagingTemplate.convertAndSendToUser(game.getMainPlayer().getName(),"/topic/sponsor-quest/"+gameId
                    ,game.getCurrentStoryCard()); // In the future we will have to check if the drawn card is  Quest
                                                // card. This would be done in the Quest Service.

            if(sizeOfPlayersList==2){ //IF the player size is 2 -> then the last player becomes the main player (race condition)
                return "Two player game";
            }
        }
        else{
            simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get((index+1)%sizeOfPlayersList).getName(),"/topic/sponsor-quest/"+gameId
                    ,game.getCurrentStoryCard());
        }
        return "";
    }
}
