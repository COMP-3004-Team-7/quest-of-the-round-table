package comp3004.project.QotRT.controller.stratPatternNewStory;

import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.model.Game;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class NewStoryCardDealer {
    private NewStoryCardStrategy newStoryCardStrategy;

    public void setNewStoryCardStrategy(NewStoryCardStrategy newStoryCardStrategy) {
        this.newStoryCardStrategy = newStoryCardStrategy;
    }

    public void dealWithNewStoryCard(Game game, SimpMessagingTemplate simpMessagingTemplate){
        //Draw new story card, and set it to current story card -> implement appropriate strategy if its Event or Tournament or Quest
        StoryCard storyCard = game.getStoryDeck().drawCard();
        game.setCurrentStoryCard(storyCard);
        if(game.getCurrentStoryCard().getType().equals("Quest")){
            setNewStoryCardStrategy(new QuestCardStrategy());
        }
        else if(game.getCurrentStoryCard().getType().equals("Tournament")){
            setNewStoryCardStrategy(new TournamentCardStrategy());
        }
        else{
            setNewStoryCardStrategy(new EventCardStrategy());
        }
        newStoryCardStrategy.dealWithNewStoryCard(game, simpMessagingTemplate);
    }
}
