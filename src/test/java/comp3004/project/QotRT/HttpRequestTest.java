package comp3004.project.QotRT;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp3004.project.QotRT.cards.*;
import comp3004.project.QotRT.controller.dto.ConnectRequest;
import comp3004.project.QotRT.controller.dto.SelectSponsorCardRequest;
import comp3004.project.QotRT.controller.dto.SubmitBidRequest;
import comp3004.project.QotRT.controller.dto.SubmitStageRequest;
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
import java.util.Arrays;


@SpringBootTest
@AutoConfigureMockMvc
class QotRtQuestTests {

	@Autowired
	private MockMvc mockMvc;
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

		//Set story card to enchanted forest
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		//Get current story card
		Game game = gameService.getGame(gameId);
		//Set story card to enchanted forest
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);
		String currentStoryCardName = game.getCurrentStoryCard().getName();

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		Assertions.assertEquals("Boar", game.getStage(1).get(0).getName());
	}

	//Test that server rejects a card that isnt a Foe/Test for first card submitted in a stage
	@Test
	void foeNotSubmittedForFirstStageOfQuest() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		result = mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Must submit Foe Card or Test First", actualJson);
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
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

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest2)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits foe card
		game.getMainPlayer().getCards().add(card1);

		result = mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Must supplement Foe card with Weapon Cards Only", actualJson);
	}

	//Test that submitting completed stage updates and returns what the next stage should be (2)
	@Test
	void submittingFirstStageOfQuest() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
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

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,1);
		String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage
		result = mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		System.out.println("Next Stage: "+ actualJson);
		Assertions.assertEquals("2", actualJson);
	}

	//Test that test card submitted to 2nd stage, where 1st stage was a test, returns error
	@Test
	void TestCardAlreadySubmitted() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits test card
		TestCardFactory testCardFactory = new TestCardFactory();
		Card card1 = testCardFactory.createCard("Test of Valor");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,1);
		String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage
		mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p2 submits ANOTHER test card
		game.getMainPlayer().getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,2);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		result = mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Can only submit 1 test card per quest", actualJson);
	}

	//Test that you cannot add card to a test card
	@Test
	void CantAddCardToTestCard() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits test card
		TestCardFactory testCardFactory = new TestCardFactory();
		Card card1 = testCardFactory.createCard("Test of Valor");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Submit another card
		game.getMainPlayer().getCards().add(card1);

		result = mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();


		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Can't add a card to a test card", actualJson);
	}

	//Test that submitting 2 stages where stage2 battlepoints < stage1 battlepoints returns an error
	@Test
	void errorThrownByIncorrectIncreasingBattlePoints() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p2 submits foe card
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card1 = foeCardFactory.createCard("Green Knight");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,1);
		String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage
		mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits another foe card for stage 2
		card1 = foeCardFactory.createCard("Boar");
		game.getMainPlayer().getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,2);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,2);
		jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage (stage 2)
		result = mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();


		actualJson = result.getResponse().getContentAsString();
		System.out.println("Error: "+ actualJson);
		Assertions.assertEquals("Submitted stage battlepoints lower than previous stages", actualJson);
	}

	//Test that submitting 2 stages where stage2 battlepoints < stage1 battlepoints returns an error
	//And also returns the cards back to the player
	@Test
	void errorThrownByIncorrectIncreasingBattlePointsReturnsCards() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p2 submits foe card
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card1 = foeCardFactory.createCard("Green Knight");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,1);
		String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage
		mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p2 submits another foe card for stage 2
		int numCardsBefore = game.getMainPlayer().getCards().size();
		card1 = foeCardFactory.createCard("Boar");
		game.getMainPlayer().getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,2);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,2);
		jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage (stage 2)
		mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();


		System.out.println("Actual Num Cards: "+ game.getMainPlayer().getCards().size());
		System.out.println("Expected Num Cards: " + numCardsBefore + 1);
		Assertions.assertEquals(numCardsBefore + 1, game.getMainPlayer().getCards().size());
	}


	//Test that submitting all stages of quest and having a player join the quest updates the 'questingplayers' array in game
	@Test
	void questingPlayersUpdatesWhenPlayerJoinsQuest() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Thieves");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		System.out.println("Actual Player In Quest: "+ game.getQuestingPlayers().get(0).getUsername());
		System.out.println("Expected Player In Quest: " + "John");
		Assertions.assertEquals("John", game.getQuestingPlayers().get(0).getUsername());
	}

	//Test that player passes first stage of quest
	@Test
	void questingPlayerBattlesFirstFoeAndIsStillInQuest() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Thieves");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits card against foe in stage 1
		WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
		Card card1 = weaponCardFactory.createCard("Lance");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p1 completes their submission against foe in stage 1
		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);

		mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		System.out.println("Actual Player In Quest: "+ game.getQuestingPlayers().get(0).getUsername());
		System.out.println("Expected Player In Quest: " + "John");
		Assertions.assertEquals("John", game.getQuestingPlayers().get(0).getUsername());
	}

	//Test that player plays amour card, gets past first stage, and amour card still remains
	@Test
	void questingPlayerBattlesFirstFoeWithAmour() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Saxons");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits card against foe in stage 1
		AmourCardFactory amourCardFactory = new AmourCardFactory();
		Card card1 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p1 completes their submission against foe in stage 1
		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);

		mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		Assertions.assertEquals(1, game.getPlayers().get(0).getAmours().size());
	}

	//Test that player cannot submit foe card against foe card in stage 1 of quest
	@Test
	void playerCantPlayFoeAgainstFoe() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Thieves");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits foe card against foe in stage 1
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card1 = foeCardFactory.createCard("Thieves");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		result = mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		Assertions.assertEquals("Must submit weapon/ally/amour", actualJson);
	}

	//Test that player cannot submit duplicate weapons against a foe in a quest
	@Test
	void playerCantSubmitDuplicateWeaponsAgainstFoe() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Thieves");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits card against foe in stage 1
		WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
		Card card1 = weaponCardFactory.createCard("Lance");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits duplicate weapon
		game.getPlayers().get(0).getCards().add(card1);
		result = mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();


		actualJson = result.getResponse().getContentAsString();
		Assertions.assertEquals("Cannot submit duplicate weapons", actualJson);
	}

	//Test that player fails to make it past a stage
	@Test
	void playerFailsToMakeItPastAStage() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Giant");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 completes their submission against foe in stage 1
		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);

		mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		Assertions.assertEquals(0, game.getQuestingPlayers().size());
	}


	//Test that player fails to make it past a stage and played weapon cards is empty
	@Test
	void playerFailsToMakeItPastAStageAndWeaponCardsPlayedIsEmpty() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Giant");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits card against foe in stage 1
		WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
		Card card1 = weaponCardFactory.createCard("Lance");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 completes their submission against foe in stage 1
		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);

		System.out.println("Weapon Cards Played = " + game.getPlayers().get(0).getWeaponCardsPlayed().size());

		mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		Assertions.assertEquals(0, game.getPlayers().get(0).getWeaponCardsPlayed().size());
	}

	//Test that player finishes quest and gets shields
	@Test
	void playerGetsShieldsFromCompletedQuest() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Boar");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Submit weapons against foe
		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p1 submits card against foe
			WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
			Card card1 = weaponCardFactory.createCard("Lance");
			game.getPlayers().get(0).getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			//p1 completes their submission against foe
			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);

			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		Assertions.assertEquals(3, game.getPlayers().get(0).getShields());
	}

	//Test that player ranks up after receiving 5 shields
	@Test
	void playerRanksUpAfter5Shields() throws Exception{
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

		//Set story card to enchanted forest -> give it 2 bonus shields
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);
		game.setBonusShield(2);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Boar");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Submit weapons against foe
		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p1 submits card against foe
			WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
			Card card1 = weaponCardFactory.createCard("Lance");
			game.getPlayers().get(0).getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			//p1 completes their submission against foe
			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);

			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		Assertions.assertEquals("Knight", game.getPlayers().get(0).getRank());
	}

	//Test that amour cards disappear after quest is over
	@Test
	void amourCardsDisappearAfterQuest() throws Exception{
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

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Saxons");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits amour card against foe in stage 1
		AmourCardFactory amourCardFactory = new AmourCardFactory();
		Card card1 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 completes their submissions against foes
		for(int i = 0; i < game.getCurrentStoryCard().getStages(); i++){
			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);
			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		Assertions.assertEquals(0, game.getPlayers().get(0).getAmours().size());
	}

	//Test that player submits higher bid and moves on
	@Test
	void submittingBidPlayerMovesOn() throws Exception{
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();

		//Creating players
		Player p1 = new Player("John","19203391912",0);
		Player p2 = new Player("Tim","12930494592",0);
		Player p3 = new Player("Sally","13495859302",0);
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


		//Connect another player to the game (p2)
		ConnectRequest connectRequest = new ConnectRequest(p2,gameId);
		String jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Connect another player to the game (p3)
		connectRequest = new ConnectRequest(p3,gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);
		mockMvc.perform(post("/game/connect")
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Start the game (p3 starts it)
		mockMvc.perform(post("/game/play-game?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//Set story card to enchanted forest
		Game game = gameService.getGame(gameId);
		QuestCardFactory questCardFactory = new QuestCardFactory();
		StoryCard storyCard = questCardFactory.createCard("Journey through the Enchanted Forest");
		game.setCurrentStoryCard(storyCard);

		//p2 accepts to sponsor quest card
		mockMvc.perform(post("/quest/sponsor-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		for(int i = 0; i < game.getCurrentStoryCard().getStages()-1; i++){
			//p2 submits foe card
			FoeCardFactory foeCardFactory = new FoeCardFactory();
			Card card1 = foeCardFactory.createCard("Saxons");
			game.getMainPlayer().getCards().add(card1);
			SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,i+1);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,i+1);
			String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

			//p2 submits completed stage
			mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
							.content(jsonSubmitStageRequest)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p2 submits test card for final stage
		TestCardFactory testCardFactory = new TestCardFactory();
		Card card1 = testCardFactory.createCard("Test of Temptation");
		game.getMainPlayer().getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getMainPlayer(),gameId,card1,game.getCurrentStoryCard().getStages());
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/select-card-for-sponsored-quest-stage?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		SubmitStageRequest submitStageRequest = new SubmitStageRequest(game.getMainPlayer(),gameId,game.getCurrentStoryCard().getStages());
		String jsonSubmitStageRequest = mapper.writeValueAsString(submitStageRequest);

		//p2 submits completed stage
		mockMvc.perform(post("/quest/submit-completed-quest-stage?gameId="+gameId)
						.content(jsonSubmitStageRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p3 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(2),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 joins quest
		connectRequest = new ConnectRequest(game.getPlayers().get(0),gameId);
		jsonConnectRequest = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/quest/join-current-quest?gameId="+gameId)
						.content(jsonConnectRequest)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p3 submits amour card against foe in stage 1
		AmourCardFactory amourCardFactory = new AmourCardFactory();
		card1 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits amour card against foe in stage 1
		game.getPlayers().get(0).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(2),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 and p2 completes their submissions against foes
		for(int i = 0; i < game.getCurrentStoryCard().getStages()-1; i++){
			submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);
			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
			submitStageRequest = new SubmitStageRequest(game.getPlayers().get(2),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);
			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p3 submits bid of 3 against foe in stage 1
		SubmitBidRequest selectBidRequest1 = new SubmitBidRequest(game.getPlayers().get(2),3,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectBidRequest1);

		mockMvc.perform(post("/quest/submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits bid of 5 against foe in stage 1
		selectBidRequest1 = new SubmitBidRequest(game.getPlayers().get(0),5,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectBidRequest1);

		mockMvc.perform(post("/quest/submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 fulfills their bid
		for(int i = 0; i < 5; i++) {
			selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0), gameId, game.getPlayers().get(0).getCards().get(0), game.getCurrentStoryCard().getStages());
			jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

			mockMvc.perform(post("/quest/discard-cards-for-test?gameId=" + gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//Expect p1 to win shields and p3 to not win any shields
		ArrayList<Integer> shieldsOfPlayers = new ArrayList<>();
		ArrayList<Integer> expectedResult = new ArrayList<>(Arrays.asList(3,0));
		shieldsOfPlayers.add(game.getPlayers().get(0).getShields());
		shieldsOfPlayers.add(game.getPlayers().get(2).getShields());

		Assertions.assertEquals(expectedResult, shieldsOfPlayers);
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
}