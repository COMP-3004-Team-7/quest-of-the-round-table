package comp3004.project.QotRT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.cards.FoeCardFactory;
import comp3004.project.QotRT.cards.WeaponCardFactory;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.SelectSponsorCardRequest;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;


@SpringBootTest
@AutoConfigureMockMvc
class QotRtQuestTests {

	@Autowired
	private MockMvc mockMvc;
	private SimpMessagingTemplate simpMessagingTemplate;
	private final GameService gameService = new GameService();

	//Test that person who sponsors quest is changed to main player
	@Test
	void acceptSponsorQuestUpdatesMainPlayer() throws Exception {
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		//Converting the Player to JSONString
		String jsonPlayer1 = mapper.writeValueAsString(p1);
		String jsonPlayer2 = mapper.writeValueAsString(p2);

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
		result = mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk()).andReturn();

		//Start the game (p2 starts it)
		result = mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 accepts to sponsor quest card
		result = mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		game = gameService.getGame(gameId);
		System.out.println("Main player after = " + game.getMainPlayer().getUsername());
		Assertions.assertEquals(game.getMainPlayer().getUsername(), "Tim");
	}

	//Test that sponsor returns current story card
	@Test
	void acceptSponsorQuestReturnsStoryCard() throws Exception{
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		//Converting the Player to JSONString
		String jsonPlayer1 = mapper.writeValueAsString(p1);
		String jsonPlayer2 = mapper.writeValueAsString(p2);

		MvcResult result = mockMvc.perform(post("/game/start")
						.content(jsonPlayer1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get Result, convert to JSON and get gameId
		String actualJson = result.getResponse().getContentAsString();
		JSONObject gameJSONobj = new JSONObject(actualJson);
		String gameId = gameJSONobj.getString("gameId");


		//Connect another player to the game
		ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
		String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		result = mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Start the game (p2 starts it)
		result = mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get current story card
		Game game = gameService.getGame(gameId);
		String currentStoryCardName = game.getCurrentStoryCard().getName();

		//p2 accepts to sponsor quest card
		result = mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		Assertions.assertEquals(currentStoryCardName, game.getCurrentStoryCard().getName());
	}

	//Test that server accepts cards for first stage and updates accordingly
	@Test
	void foeSubmittedForFirstStageOfQuest() throws Exception{
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		//Converting the Player to JSONString
		String jsonPlayer1 = mapper.writeValueAsString(p1);
		String jsonPlayer2 = mapper.writeValueAsString(p2);

		MvcResult result = mockMvc.perform(post("/game/start")
						.content(jsonPlayer1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get Result, convert to JSON and get gameId
		String actualJson = result.getResponse().getContentAsString();
		JSONObject gameJSONobj = new JSONObject(actualJson);
		String gameId = gameJSONobj.getString("gameId");


		//Connect another player to the game
		ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
		String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		result = mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Start the game (p2 starts it)
		result = mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get current story card
		Game game = gameService.getGame(gameId);
		String currentStoryCardName = game.getCurrentStoryCard().getName();

		//p2 accepts to sponsor quest card
		result = mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits foe card
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card = foeCardFactory.createCard("Boar");
		game.getMainPlayer().getCards().add(card);
		SelectSponsorCardRequest selectSponsorCardRequest = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card,1);
		String jsonSponsorCardRequest = mapper.writeValueAsString(selectSponsorCardRequest);

		result = mockMvc.perform(post("/quest/select-foe-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		Assertions.assertEquals("Boar", game.getStage(1).get(0).getName());
	}

	//Test that server rejects a card that isnt a Foe for first card submitted in a stage
	@Test
	void foeNotSubmittedForFirstStageOfQuest() throws Exception{
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		//Converting the Player to JSONString
		String jsonPlayer1 = mapper.writeValueAsString(p1);
		String jsonPlayer2 = mapper.writeValueAsString(p2);

		MvcResult result = mockMvc.perform(post("/game/start")
						.content(jsonPlayer1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get Result, convert to JSON and get gameId
		String actualJson = result.getResponse().getContentAsString();
		JSONObject gameJSONobj = new JSONObject(actualJson);
		String gameId = gameJSONobj.getString("gameId");


		//Connect another player to the game
		ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
		String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		result = mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Start the game (p2 starts it)
		result = mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get current story card
		Game game = gameService.getGame(gameId);
		String currentStoryCardName = game.getCurrentStoryCard().getName();

		//p2 accepts to sponsor quest card
		result = mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits weapon card -> should throw error
		WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
		Card card = weaponCardFactory.createCard("Lance");
		game.getMainPlayer().getCards().add(card);
		SelectSponsorCardRequest selectSponsorCardRequest = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card,1);
		String jsonSponsorCardRequest = mapper.writeValueAsString(selectSponsorCardRequest);

		result = mockMvc.perform(post("/quest/select-foe-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Must submit Foe Card first before weapons", actualJson);
	}


	//Test that the submitted weapon cards are correct to first stage of quest
	@Test
	void weaponCardsAddedToFirstStageOfQuest() throws Exception{
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		//Converting the Player to JSONString
		String jsonPlayer1 = mapper.writeValueAsString(p1);
		String jsonPlayer2 = mapper.writeValueAsString(p2);

		MvcResult result = mockMvc.perform(post("/game/start")
						.content(jsonPlayer1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get Result, convert to JSON and get gameId
		String actualJson = result.getResponse().getContentAsString();
		JSONObject gameJSONobj = new JSONObject(actualJson);
		String gameId = gameJSONobj.getString("gameId");


		//Connect another player to the game
		ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
		String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		result = mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Start the game (p2 starts it)
		result = mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get current story card
		Game game = gameService.getGame(gameId);
		String currentStoryCardName = game.getCurrentStoryCard().getName();

		//p2 accepts to sponsor quest card
		result = mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p2 submits foe card
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card1 = foeCardFactory.createCard("Boar");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,1);
		String jsonSponsorCardRequest = mapper.writeValueAsString(selectSponsorCardRequest1);

		result = mockMvc.perform(post("/quest/select-foe-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p2 submits 2 weapon cards
		WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
		Card card2 = weaponCardFactory.createCard("Lance");
		Card card3 = weaponCardFactory.createCard("Horse");
		game.getMainPlayer().getCards().add(card2);
		game.getMainPlayer().getCards().add(card3);
		SelectSponsorCardRequest selectSponsorCardRequest2 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card2,1);
		String jsonSponsorCardRequest2 = mapper.writeValueAsString(selectSponsorCardRequest2);
		SelectSponsorCardRequest selectSponsorCardRequest3 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card3,1);
		String jsonSponsorCardRequest3 = mapper.writeValueAsString(selectSponsorCardRequest3);

		result = mockMvc.perform(post("/quest/add-weapon-to-foe-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest2)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		result = mockMvc.perform(post("/quest/add-weapon-to-foe-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest3)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		ArrayList<String> expectedCards = new ArrayList<>();
		ArrayList<String> actualCards = new ArrayList<>();
		actualCards.add(game.getStage(1).get(0).getName()); actualCards.add(game.getStage(1).get(1).getName());
		actualCards.add(game.getStage(1).get(2).getName());
		expectedCards.add(card1.getName()); expectedCards.add(card2.getName()); expectedCards.add(card3.getName());
		Assertions.assertEquals(expectedCards, actualCards);
	}

	//Test that foe submitted to stage that already has a foe gives back an error
	@Test
	void FoeCardAddedToFoeInFirstStageOfQuest() throws Exception{
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		//Converting the Player to JSONString
		String jsonPlayer1 = mapper.writeValueAsString(p1);
		String jsonPlayer2 = mapper.writeValueAsString(p2);

		MvcResult result = mockMvc.perform(post("/game/start")
						.content(jsonPlayer1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get Result, convert to JSON and get gameId
		String actualJson = result.getResponse().getContentAsString();
		JSONObject gameJSONobj = new JSONObject(actualJson);
		String gameId = gameJSONobj.getString("gameId");


		//Connect another player to the game
		ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
		String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		result = mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Start the game (p2 starts it)
		result = mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get current story card
		Game game = gameService.getGame(gameId);
		String currentStoryCardName = game.getCurrentStoryCard().getName();

		//p2 accepts to sponsor quest card
		result = mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p2 submits foe card
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card1 = foeCardFactory.createCard("Boar");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		result = mockMvc.perform(post("/quest/select-foe-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits foe card
		game.getMainPlayer().getCards().add(card1);

		result = mockMvc.perform(post("/quest/add-weapon-to-foe-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Must submit weapons only to stage that already has a Foe", actualJson);
	}
}

@SpringBootTest
@AutoConfigureMockMvc
class TestingWebApplicationTest {

	@Autowired
	private MockMvc mockMvc;
	private SimpMessagingTemplate simpMessagingTemplate;
	//Test player creating game and test the name is correct
	@Test
	void playerCreatingNewGame() throws Exception {
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating player
		Player p = new Player("John",0);
		//Converting the Player to JSONString
		String jsonPlayer = mapper.writeValueAsString(p);


		MvcResult result = mockMvc.perform(post("/game/start")
				.content(jsonPlayer)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Get Result, convert to JSON and get first players name
		String actualJson = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(actualJson);
		String name = obj.getJSONArray("players").getJSONObject(0).getString("username");
		System.out.println(actualJson);
		Assertions.assertEquals("John", name);
	}

	//@Test

//	void discardingCardTest() throws Exception{
//		simpMessagingTemplate = new SimpMessagingTemplate();
//	}
		//ObjectMapper mapper = new ObjectMapper();

//		//Creating player
//		Player p = new Player("John",0);
//		//Converting the Player to JSONString
//		String jsonPlayer = mapper.writeValueAsString(p);
//
//		MvcResult result = mockMvc.perform(post("/game/start")
//						.content(jsonPlayer)
//						.contentType(MediaType.APPLICATION_JSON)
//						.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk()).andReturn();
//
//		String actualJson = result.getResponse().getContentAsString();
//		JSONObject obj = new JSONObject(actualJson);
//		String id = obj.getString("gameId");
//		System.out.println(id);
//
//		MvcResult result1 = mockMvc.perform(post("/app/play-game/"+id)
//						.content(jsonPlayer)
//						.contentType(MediaType.APPLICATION_JSON)
//						.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk()).andReturn();
//	}



}