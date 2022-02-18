package comp3004.project.QotRT.service;


import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.storage.GameStorage;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static comp3004.project.QotRT.model.GameStatus.*;


@Service
public class GameService {
    public GameService() {
    }

    public Game createGame(Player player){
        Game game = new Game();
        game.setGameId(UUID.randomUUID().toString());
        player.setPlayerNumber(game.getNextPlayerNumber());
        game.addPlayer(player);
        game.setStatus(NEW);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game connectToGame(Player player, String gameId) throws Exception {
        if(!GameStorage.getInstance().getGames().containsKey(gameId)){
            throw new Exception("game Id does not exist");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);

        //Only allow four player games
        if(game.getPlayers().size() >= 4){
            throw new Exception("game has at least 4 players -> can't join");
        }
        player.setPlayerNumber(game.getNextPlayerNumber());
        game.addPlayer(player);
        game.setStatus(IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        return game;
    }


    public Game getGame(String gameId){
        return GameStorage.getInstance().getGames().get(gameId);
    }
}
