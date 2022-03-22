package comp3004.project.QotRT.controller.stratPatternNewStory;

import comp3004.project.QotRT.cards.StoryCard;
import comp3004.project.QotRT.model.Game;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface NewStoryCardStrategy {
    void dealWithNewStoryCard(Game game, SimpMessagingTemplate simpMessagingTemplate);
}
