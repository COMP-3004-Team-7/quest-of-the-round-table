package comp3004.project.QotRT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp3004.project.QotRT.cards.Card;
import comp3004.project.QotRT.controller.GameController;
import comp3004.project.QotRT.model.Game;
import comp3004.project.QotRT.model.Player;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@SpringBootTest
class QotRtApplicationTests {

	@Autowired
	private GameController controller;
//	@Autowired
//	private TestRestTemplate restTemplate;

	//Test that controller is loaded when server runs
	@Test
	void contextLoads() {
		assertThat(controller).isNotNull();
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
		String name = obj.getJSONArray("players").getJSONObject(0).getString("name");
		System.out.println(actualJson);
		Assertions.assertEquals(name, "John");
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