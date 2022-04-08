package comp3004.project.QotRT;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp3004.project.QotRT.cards.*;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.DiscardRequest;
import comp3004.project.QotRT.controller.dto.SelectSponsorCardRequest;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.BattlePointsOrBidsReceiver;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.service.GameService;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

public class QotRtTourneyTest {
    @Autowired
    private MockMvc mockMvc;
    private final GameService gameService = new GameService();
    private final BattlePointsOrBidsReceiver battlePointsOrBidsReceiver = new BattlePointsOrBidsReceiver();

    @Test
    void acceptTournament() throws Exception {
        //Creating the ObjectMapper object
        ObjectMapper mapper = new ObjectMapper();

        //Creating players
        Player p1 = new Player("John","19203391912",0);
        Player p2 = new Player("Tim","12930494592",0);
        //Converting the Player to JSONString
        String jsonPlayer1 = mapper.writeValueAsString(p1);


        MvcResult result = mockMvc.perform(post("/game/start")
                        .content(jsonPlayer1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Get Result, convert to JSON and get gameId
        String actualJson = result.getResponse().getContentAsString();
        JSONObject gameJSONobj = new JSONObject(actualJson);
        String gameId = gameJSONobj.getString("gameId");
        String mainPlayer = gameJSONobj.getJSONObject("mainPlayer").getString("username");
        //Print current main player
        Game game = gameService.getGame(gameId);
        System.out.println("Main player at start = " + game.getMainPlayer());
        System.out.println("Main player at start = " + mainPlayer);

        //Connect another player to the game
        ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
        String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
        mockMvc.perform(post("/game/connect")
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Start the game (p2 starts it)
        mockMvc.perform(post("/game/play-game?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Set story card to At Camelot
        TournamentcardFactory tournamentCardFactory = new TournamentcardFactory();
        StoryCard storyCard = tournamentCardFactory.createCard("At Camelot");
        game.setCurrentStoryCard(storyCard);

        //p2 accepts to join tournament
        mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        actualJson = result.getResponse().getContentAsString();
        game = gameService.getGame(gameId);
        System.out.println("Number of players in the tournament: " + game.getTournamentPlayers().size());
        Assertions.assertEquals(game.getTournamentPlayers().size(), 1);
    }
    @Test
    void declineTournament() throws Exception {
        //Creating the ObjectMapper object
        ObjectMapper mapper = new ObjectMapper();

        //Creating players
        Player p1 = new Player("John","19203391912",0);
        Player p2 = new Player("Tim","12930494592",0);
        //Converting the Player to JSONString
        String jsonPlayer1 = mapper.writeValueAsString(p1);


        MvcResult result = mockMvc.perform(post("/game/start")
                        .content(jsonPlayer1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Get Result, convert to JSON and get gameId
        String actualJson = result.getResponse().getContentAsString();
        JSONObject gameJSONobj = new JSONObject(actualJson);
        String gameId = gameJSONobj.getString("gameId");
        String mainPlayer = gameJSONobj.getJSONObject("mainPlayer").getString("username");
        //Print current main player
        Game game = gameService.getGame(gameId);
        System.out.println("Main player at start = " + game.getMainPlayer());
        System.out.println("Main player at start = " + mainPlayer);

        //Connect another player to the game
        ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
        String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
        mockMvc.perform(post("/game/connect")
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Start the game (p2 starts it)
        mockMvc.perform(post("/game/play-game?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Set story card to At Camelot
        TournamentcardFactory tournamentCardFactory = new TournamentcardFactory();
        StoryCard storyCard = tournamentCardFactory.createCard("At Camelot");
        game.setCurrentStoryCard(storyCard);

        //p2 declines to join tournament
        mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        game = gameService.getGame(gameId);
        System.out.println("Number of players in the tournament: " + game.getTournamentPlayers().size());
        Assertions.assertEquals(game.getTournamentPlayers().size(), 0);
    }

    @Test
    void submitTourney() throws Exception {
        //Creating the ObjectMapper object
        ObjectMapper mapper = new ObjectMapper();

        //Creating players
        Player p1 = new Player("John","19203391912",0);
        Player p2 = new Player("Tim","12930494592",0);
        //Converting the Player to JSONString
        String jsonPlayer1 = mapper.writeValueAsString(p1);


        MvcResult result = mockMvc.perform(post("/game/start")
                        .content(jsonPlayer1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Get Result, convert to JSON and get gameId
        String actualJson = result.getResponse().getContentAsString();
        JSONObject gameJSONobj = new JSONObject(actualJson);
        String gameId = gameJSONobj.getString("gameId");
        String mainPlayer = gameJSONobj.getJSONObject("mainPlayer").getString("username");
        //Print current main player
        Game game = gameService.getGame(gameId);
        System.out.println("Main player at start = " + game.getMainPlayer());
        System.out.println("Main player at start = " + mainPlayer);

        //Connect another player to the game
        ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
        String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
        mockMvc.perform(post("/game/connect")
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Start the game (p2 starts it)
        mockMvc.perform(post("/game/play-game?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Set story card to At Camelot
        TournamentcardFactory tournamentCardFactory = new TournamentcardFactory();
        StoryCard storyCard = tournamentCardFactory.createCard("At Camelot");
        game.setCurrentStoryCard(storyCard);

        //p2 joins the tournament
        mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //p2 submits a weapon card
        WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
        Card card1 = weaponCardFactory.createCard("Lance");
        Card card2 = weaponCardFactory.createCard("Horse");
        game.getMainPlayer().getCards().add(card1);
        game.getMainPlayer().getCards().add(card2);

        game = gameService.getGame(gameId);

        DiscardRequest discardRequest1 = new DiscardRequest(p2,gameId,card1);
        String jsonSponsorCardRequest1 = mapper.writeValueAsString(discardRequest1);
        DiscardRequest discardRequest2 = new DiscardRequest(p2,gameId,card2);
        String jsonSponsorCardRequest2 = mapper.writeValueAsString(discardRequest2);

        mockMvc.perform(post("/tournament/submit-tournament-card?gameId="+gameId)
                        .content(jsonSponsorCardRequest1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        mockMvc.perform(post("/tournament/submit-tournament-card?gameId="+gameId)
                        .content(jsonSponsorCardRequest2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        ArrayList<String> expectedCards = new ArrayList<>();
        ArrayList<String> actualCards = new ArrayList<>();
        actualCards.add(game.getTournamentPlayers().get(0).getWeaponCardsPlayed().get(0).getName());
        actualCards.add(game.getTournamentPlayers().get(0).getWeaponCardsPlayed().get(1).getName());
        expectedCards.add(card1.getName()); expectedCards.add(card2.getName());
        Assertions.assertEquals(expectedCards, actualCards);
    }

    //SUBMITTING DUPLICATE WEAPON CARDS
    //CHECKING IF THE ERROR MESSAGE OCCURS
    @Test
    void submitSameWeaponCardTourney() throws Exception {
        //Creating the ObjectMapper object
        ObjectMapper mapper = new ObjectMapper();

        //Creating players
        Player p1 = new Player("John","19203391912",0);
        Player p2 = new Player("Tim","12930494592",0);
        //Converting the Player to JSONString
        String jsonPlayer1 = mapper.writeValueAsString(p1);


        MvcResult result = mockMvc.perform(post("/game/start")
                        .content(jsonPlayer1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Get Result, convert to JSON and get gameId
        String actualJson = result.getResponse().getContentAsString();
        JSONObject gameJSONobj = new JSONObject(actualJson);
        String gameId = gameJSONobj.getString("gameId");
        String mainPlayer = gameJSONobj.getJSONObject("mainPlayer").getString("username");
        //Print current main player
        Game game = gameService.getGame(gameId);
        System.out.println("Main player at start = " + game.getMainPlayer());
        System.out.println("Main player at start = " + mainPlayer);

        //Connect another player to the game
        ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
        String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
        mockMvc.perform(post("/game/connect")
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Start the game (p2 starts it)
        mockMvc.perform(post("/game/play-game?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //Set story card to At Camelot
        TournamentcardFactory tournamentCardFactory = new TournamentcardFactory();
        StoryCard storyCard = tournamentCardFactory.createCard("At Camelot");
        game.setCurrentStoryCard(storyCard);

        //p2 joins the tournament
        mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
                        .content(jsonConnectRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        //p2 submits a weapon card
        WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
        Card card1 = weaponCardFactory.createCard("Lance");
        //Card card2 = weaponCardFactory.createCard("Lance");
        game.getMainPlayer().getCards().add(card1);
        //game.getMainPlayer().getCards().add(card2);

        game = gameService.getGame(gameId);

        DiscardRequest discardRequest1 = new DiscardRequest(p2,gameId,card1);
        String jsonSponsorCardRequest1 = mapper.writeValueAsString(discardRequest1);
        DiscardRequest discardRequest2 = new DiscardRequest(p2,gameId,card1);
        String jsonSponsorCardRequest2 = mapper.writeValueAsString(discardRequest2);

        mockMvc.perform(post("/tournament/submit-tournament-card?gameId="+gameId)
                        .content(jsonSponsorCardRequest1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        result = mockMvc.perform(post("/tournament/submit-tournament-card?gameId="+gameId)
                        .content(jsonSponsorCardRequest2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        actualJson = result.getResponse().getContentAsString();
        System.out.println("Error: "+ actualJson);
        Assertions.assertEquals("Cannot play duplicate weapon cards", actualJson);
    }



}


