package comp3004.project.QotRT;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp3004.project.QotRT.cards.*;
import comp3004.project.QotRT.controller.dto.*;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.AllyBattlePointsOrBidsStrategy;
import comp3004.project.QotRT.controller.stratPatternBattlePoints.BattlePointsOrBidsReceiver;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import comp3004.project.QotRT.service.GameService;
import comp3004.project.QotRT.service.TournamentService;
import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//NOTE -> Some tests might fail, but that is because the story cards are automatically drawn at the end, and might effect
//the number of shields players have (ex: an event card that gives or removes shields from players) that is fine.

@SpringBootTest
@AutoConfigureMockMvc
class QotRtQuestTests {

	@Autowired
	private MockMvc mockMvc;
	private final GameService gameService = new GameService();
    private final BattlePointsOrBidsReceiver battlePointsOrBidsReceiver = new BattlePointsOrBidsReceiver();

	//Test that deck gets rigged
	@Test
	void newTestToSeeIfDeckGetsRiggedProperly() throws Exception {
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

		//Expect current story card to be repel saxon raiders, then court called to camelot, then at tintagel
		game = gameService.getGame(gameId);

		ArrayList<String> actual = new ArrayList<>(List.of(game.getCurrentStoryCard().getName(),
				game.getStoryDeck().getDeck().get(game.getStoryDeck().getDeck().size()-1).getName(),
				game.getStoryDeck().getDeck().get(game.getStoryDeck().getDeck().size()-2).getName()));
		ArrayList<String> expected = new ArrayList<>(List.of("Repel the Saxon Raiders", "All Allies in play must be discarded", "At Tintagel"));

		Assertions.assertEquals(expected, actual);
	}

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
		game.setBonusShield(0);


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

    //Test Sir Gawain Battle Points on non special quest
    @Test
    void sirGawainTest() throws Exception{
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

        //p1 submits ally against foe
        AllyCardFactory allyCardFactory = new AllyCardFactory();
        Card card1 = allyCardFactory.createCard("Sir Gawain");
        game.getPlayers().get(0).getCards().add(card1);
        SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
        String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

        mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
                        .content(jsonSponsorCardRequest1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
        int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

        Assertions.assertEquals(10, actualBattlePoints);
    }

    //Test Sir Gawain Battle Points on Green Knight Quest
    @Test
    void sirGawainTestSpecialQuest() throws Exception{
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
        StoryCard storyCard = questCardFactory.createCard("Test of the Green Knight");
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

        //p1 submits ally against foe
        AllyCardFactory allyCardFactory = new AllyCardFactory();
        Card card1 = allyCardFactory.createCard("Sir Gawain");
        game.getPlayers().get(0).getCards().add(card1);
        SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
        String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

        mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
                        .content(jsonSponsorCardRequest1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
        int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

        Assertions.assertEquals(30, actualBattlePoints);
    }

    //Test King Pellinore Battle Points on any quest
    @Test
    void kingPellinoreTest() throws Exception{
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
        StoryCard storyCard = questCardFactory.createCard("Test of the Green Knight");
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

        //p1 submits ally against foe
        AllyCardFactory allyCardFactory = new AllyCardFactory();
        Card card1 = allyCardFactory.createCard("King Pellinore");
        game.getPlayers().get(0).getCards().add(card1);
        SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
        String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

        mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
                        .content(jsonSponsorCardRequest1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
        int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

        Assertions.assertEquals(10, actualBattlePoints);
    }

    //Test King Pellinore bids on questing beast quest
    @Test
    void kingPellinoreTestSpecialQuest() throws Exception{
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
        StoryCard storyCard = questCardFactory.createCard("Search for the Questing Beast");
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

        //p1 submits ally against foe
        AllyCardFactory allyCardFactory = new AllyCardFactory();
        Card card1 = allyCardFactory.createCard("King Pellinore");
        game.getPlayers().get(0).getCards().add(card1);
        SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
        String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

        mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
                        .content(jsonSponsorCardRequest1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
        int actualBids = battlePointsOrBidsReceiver.receiveBids(game, null, game.getPlayers().get(0));

        Assertions.assertEquals(4, actualBids);
    }

	//Test Sir Percival battle points on non special quest
	@Test
	void sirPercivalTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Questing Beast");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Sir Percival");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(5, actualBattlePoints);
	}

	//Test Sir Percival battle points on special quest
	@Test
	void sirPercivalTestSpecialQuest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Holy Grail");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Sir Percival");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(25, actualBattlePoints);
	}

	//Test Sir Lancelot battle points on non special quest
	@Test
	void sirLancelotTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Holy Grail");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Sir Lancelot");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(15, actualBattlePoints);
	}

	//Test Sir Lancelot battle points on special quest
	@Test
	void sirLancelotTestSpecialQuest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Defend the Queen's Honor");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Sir Lancelot");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(40, actualBattlePoints);
	}

	//Test Sir Galahad
	@Test
	void sirGalahadTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Defend the Queen's Honor");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Sir Galahad");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(15, actualBattlePoints);
	}

	//Test Sir Tristan battle points when queen iseult is NOT in play
	@Test
	void sirTristanTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Holy Grail");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Sir Tristan");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(10, actualBattlePoints);
	}

	//Test queen Iseult bids when sir tristan is not in play
	@Test
	void queenIseultTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Holy Grail");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Queen Iseult");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBids = battlePointsOrBidsReceiver.receiveBids(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(2, actualBids);
	}

	//Test king arthur bids and battelpoints
	@Test
	void kingArthurBidsAndBattlePointsTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Holy Grail");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("King Arthur");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBids = battlePointsOrBidsReceiver.receiveBids(game, null, game.getPlayers().get(0));
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(List.of(2,10), List.of(actualBids,actualBattlePoints));
	}

	//Test queen guinever bids and battelpoints
	@Test
	void queenGuinevereBidsAndBattlePointsTest() throws Exception{
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
		StoryCard storyCard = questCardFactory.createCard("Search for the Holy Grail");
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

		//p1 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card1 = allyCardFactory.createCard("Queen Guinevere");
		game.getPlayers().get(0).getCards().add(card1);
		SelectSponsorCardRequest selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBids = battlePointsOrBidsReceiver.receiveBids(game, null, game.getPlayers().get(0));
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(List.of(3,0), List.of(actualBids,actualBattlePoints));
	}

	//Test Sir Tristan when queen isuelt is in play
	@Test
	void sirTristanTestWithQueen() throws Exception{
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
		game.setBonusShield(0);

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

		//p3 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card2 = allyCardFactory.createCard("Queen Iseult");
		game.getPlayers().get(2).getCards().add(card2);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(2),gameId,card2,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits ally against foe
		Card card3 = allyCardFactory.createCard("Sir Tristan");
		game.getPlayers().get(0).getCards().add(card3);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card3,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBattlePoints = battlePointsOrBidsReceiver.receiveBattlePoints(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(30, actualBattlePoints);
	}

	//Test Queen Iseult when Sir Tristan is in play
	@Test
	void queenIseultTestWithTristan() throws Exception{
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
		game.setBonusShield(0);

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

		//p3 submits ally against foe
		AllyCardFactory allyCardFactory = new AllyCardFactory();
		Card card2 = allyCardFactory.createCard("Sir Tristan");
		game.getPlayers().get(2).getCards().add(card2);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(2),gameId,card2,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits ally against foe
		Card card3 = allyCardFactory.createCard("Queen Iseult");
		game.getPlayers().get(0).getCards().add(card3);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card3,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		battlePointsOrBidsReceiver.setGetBattlePointsOrBidsStrategy(new AllyBattlePointsOrBidsStrategy());
		int actualBids = battlePointsOrBidsReceiver.receiveBids(game, null, game.getPlayers().get(0));

		Assertions.assertEquals(4, actualBids);
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
		game.setBonusShield(0);

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
		game.getPlayers().get(2).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(2),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits amour card against foe in stage 1
		Card card2 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card2);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card2,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//p1 and p3 completes their submissions against foes
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

	//Test that player submits low bid and returns an error
	@Test
	void submittingLowBidThrowsError() throws Exception{
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
		card1 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 completes their submission against foes
		for(int i = 0; i < game.getCurrentStoryCard().getStages()-1; i++){
			submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);
			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 submits bid of 1 against foe in stage 1 -> not high enough
		SubmitBidRequest selectBidRequest1 = new SubmitBidRequest(game.getPlayers().get(0),1,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectBidRequest1);

		result = mockMvc.perform(post("/quest/submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		Assertions.assertEquals("You need to bid higher than 2" , actualJson);
	}

	//Test that player submits bid that is too high (not enough cards to discard) throws error
	@Test
	void submittingTooHighBidThrowsError() throws Exception{
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
		card1 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 completes their submission against foes
		for(int i = 0; i < game.getCurrentStoryCard().getStages()-1; i++){
			submitStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,i+1);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(submitStageRequest);
			mockMvc.perform(post("/quest/complete-cards-played-against-foe?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();
		}

		//p1 submits bid of 25 against foe in stage 1 -> too high
		SubmitBidRequest selectBidRequest1 = new SubmitBidRequest(game.getPlayers().get(0),25,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectBidRequest1);

		result = mockMvc.perform(post("/quest/submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		actualJson = result.getResponse().getContentAsString();
		Assertions.assertEquals("You cannot bid over your bid limit (check number of cards in hand)" , actualJson);
	}

	//Test that player declines bid and does not move on
	@Test
	void submittingBidPlayerMovesOnDecliningPlayerDoesNot() throws Exception{
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
		game.getPlayers().get(2).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(2),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits amour card against foe in stage 1
		Card card2 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card2);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card2,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 and p3 completes their submissions against foes
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

		//p3 declines to submit a bid
		SubmitStageRequest selectStageRequest = new SubmitStageRequest(game.getPlayers().get(2),gameId,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectStageRequest);

		mockMvc.perform(post("/quest/decline-to-submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits bid of 5 against foe in stage 1
		SubmitBidRequest selectBidRequest1 = new SubmitBidRequest(game.getPlayers().get(0),5,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectBidRequest1);

		mockMvc.perform(post("/quest/submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//Expect p3 to not be in questing array, and p1 to be in questing array


		Assertions.assertEquals("John", game.getQuestingPlayers().get(0).getUsername());
	}

	//Test that both player declines bid and questing array is empty
	@Test
	void bothDecliningPlayersDoNotMoveOn() throws Exception{
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
		game.getPlayers().get(2).getCards().add(card1);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(2),gameId,card1,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits amour card against foe in stage 1
		Card card2 = amourCardFactory.createCard("Amour");
		game.getPlayers().get(0).getCards().add(card2);
		selectSponsorCardRequest1 = new SelectSponsorCardRequest(game.getPlayers().get(0),gameId,card2,1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectSponsorCardRequest1);

		mockMvc.perform(post("/quest/submit-card-against-foe?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 and p3 completes their submissions against foes
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

		//p3 declines to submit a bid
		SubmitStageRequest selectStageRequest = new SubmitStageRequest(game.getPlayers().get(2),gameId,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectStageRequest);

		mockMvc.perform(post("/quest/decline-to-submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		//p1 submits bid of 5 against foe in stage 1
		selectStageRequest = new SubmitStageRequest(game.getPlayers().get(0),gameId,3);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(selectStageRequest);

		mockMvc.perform(post("/quest/decline-to-submit-bid?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//Expect p3 and p1 to not be in questing array -> should be empty


		Assertions.assertEquals(0, game.getQuestingPlayers().size());
	}




}

@SpringBootTest
@AutoConfigureMockMvc
class QotRtTournamentTests {

	@Autowired
	private MockMvc mockMvc;
	private final GameService gameService = new GameService();
	private final BattlePointsOrBidsReceiver battlePointsOrBidsReceiver = new BattlePointsOrBidsReceiver();

	//Test that players can join a tournament
	@Test
	void playerCanJoinATournament() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);

		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 3){

			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}


		//expect 3 players to be in Tournament

		Assertions.assertEquals(3, game.getNumOfTournamentPlayers());
	}

	//Test that players can decline to join a tournament
	@Test
	void playerCanDeclineATournament() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);


		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 2){

			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		//One player declines
		connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//expect 2 players to be in Tournament

		Assertions.assertEquals(2, game.getNumOfTournamentPlayers());
	}

	//Test that only 1 player joins wins shields
	@Test
	void onePlayerJoinsTournamentAndWinsShields() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);


		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 3){
			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex + i) % game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);
			if(i == 0){
				mockMvc.perform(post("/tournament/join-tournament?gameId=" + gameId)
								.content(jsonSponsorCardRequest1)
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk()).andReturn();
			}
			else{
				mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
								.content(jsonSponsorCardRequest1)
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk()).andReturn();
			}
			i++;
		}


		//expect startIndex player to have 2 shields

		Assertions.assertEquals(2, game.getPlayers().get(startIndex).getShields());
	}

	//Test that players can submit weapons to a tournament
	@Test
	void playerCanSubmitWeaponToATournament() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);


		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 2){

			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		//One player declines
		connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//Player submits Lance Weapon
		int indexForSubmitting = game.getTournamentPlayers().indexOf(game.getMainPlayer());
		WeaponCardFactory weaponCardFactory = new WeaponCardFactory();
		Card card1 = weaponCardFactory.createCard("Lance");
		game.getTournamentPlayers().get(indexForSubmitting).getCards().add(card1);
		DiscardRequest discardRequest = new DiscardRequest(game.getTournamentPlayers().get(indexForSubmitting), gameId, card1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(discardRequest);

		mockMvc.perform(post("/tournament/submit-tournament-card?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();



		Assertions.assertEquals(1, game.getTournamentPlayers().get(indexForSubmitting).getWeaponCardsPlayed().size());
	}

	//Test that if a player submits a foe, it will throw an error
	@Test
	void playerSubmitsFoeToATournamentAndItThrowsError() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);


		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 2){

			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		//One player declines
		connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		//Player submits Lance Weapon
		int indexForSubmitting = game.getTournamentPlayers().indexOf(game.getMainPlayer());
		FoeCardFactory foeCardFactory = new FoeCardFactory();
		Card card1 = foeCardFactory.createCard("Boar");
		game.getTournamentPlayers().get(indexForSubmitting).getCards().add(card1);
		DiscardRequest discardRequest = new DiscardRequest(game.getTournamentPlayers().get(indexForSubmitting), gameId, card1);
		jsonSponsorCardRequest1 = mapper.writeValueAsString(discardRequest);

		result = mockMvc.perform(post("/tournament/submit-tournament-card?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();


		actualJson = result.getResponse().getContentAsString();
		Assertions.assertEquals("Can only play weapon, amour, or ally cards", actualJson);
	}

	//Test that players can submit allies and player with more battle points wins
	@Test
	void playersSubmitAlliesToATournamentAndOnePlayerWins() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);


		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 2){

			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		//One player declines
		connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		int expectedWinnerIndex = 0;
		i = 0;
		int indexForSubmitting = game.getTournamentPlayers().indexOf(game.getMainPlayer());
		while(i < 2){
			AllyCardFactory allyCardFactory = new AllyCardFactory();
			DiscardRequest discardRequest;

			if(i == 0) {
				//Player submits king arthur
				Card card1 = allyCardFactory.createCard("King Arthur");
				game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()).getCards().add(card1);
				discardRequest = new DiscardRequest(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()), gameId, card1);

			}
			else{
				expectedWinnerIndex = game.getPlayers().indexOf(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()));
				//Player submits sir lancelot
				Card card1 = allyCardFactory.createCard("Sir Lancelot");
				game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()).getCards().add(card1);
				discardRequest = new DiscardRequest(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()), gameId, card1);


			}
			jsonSponsorCardRequest1 = mapper.writeValueAsString(discardRequest);
			mockMvc.perform(post("/tournament/submit-tournament-card?gameId=" + gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();


			connectRequest = new ConnectRequest(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()), gameId);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/complete-submitting-tournament-cards?gameId=" + gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		Assertions.assertEquals(3, game.getPlayers().get(expectedWinnerIndex).getShields());
	}

	//Test that players can submit allies and if they tie, they play a tie breaker tournament
	@Test
	void playersSubmitAlliesToATournamentAndTie() throws Exception {
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

		//Set story card to Tintagel
		Game game = gameService.getGame(gameId);
		TournamentcardFactory tournamentcardFactory = new TournamentcardFactory();
		StoryCard storyCard = tournamentcardFactory.createCard("At Tintagel");
		game.setCurrentStoryCard(storyCard);


		//players join tournament
		int i = 0;
		int startIndex = game.getPlayers().indexOf(game.getMainPlayer());
		while(i < 2){

			connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
			String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/join-tournament?gameId="+gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		//One player declines
		connectRequest = new ConnectRequest(game.getPlayers().get((startIndex+i)%game.getPlayers().size()), gameId);
		String jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

		mockMvc.perform(post("/tournament/decline-joining-tournament?gameId="+gameId)
						.content(jsonSponsorCardRequest1)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();


		int expectedWinnerIndex = 0;
		i = 0;
		int indexForSubmitting = game.getTournamentPlayers().indexOf(game.getMainPlayer());
		while(i < 2){
			AllyCardFactory allyCardFactory = new AllyCardFactory();
			DiscardRequest discardRequest;

			if(i == 0) {
				//Player submits king arthur
				Card card1 = allyCardFactory.createCard("King Arthur");
				game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()).getCards().add(card1);
				discardRequest = new DiscardRequest(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()), gameId, card1);

			}
			else{
				expectedWinnerIndex = game.getPlayers().indexOf(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()));
				//Player submits sir lancelot
				Card card1 = allyCardFactory.createCard("King Arthur");
				game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()).getCards().add(card1);
				discardRequest = new DiscardRequest(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()), gameId, card1);


			}
			jsonSponsorCardRequest1 = mapper.writeValueAsString(discardRequest);
			mockMvc.perform(post("/tournament/submit-tournament-card?gameId=" + gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();


			connectRequest = new ConnectRequest(game.getTournamentPlayers().get((indexForSubmitting + i) % game.getTournamentPlayers().size()), gameId);
			jsonSponsorCardRequest1 = mapper.writeValueAsString(connectRequest);

			mockMvc.perform(post("/tournament/complete-submitting-tournament-cards?gameId=" + gameId)
							.content(jsonSponsorCardRequest1)
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn();

			i++;
		}

		Assertions.assertEquals(Lists.list("At Tintagel", true), Lists.list(game.getCurrentStoryCard().getName(), game.getInTieBreakerTournament()));
	}


}