import * as React from 'react';
import ReactDOM from 'react-dom';
import {JoinDialog} from "./components/dialogs/JoinDialog";
import {GameBottomAppBar} from "./components/GameBottomAppBar";
import {GameRightDrawer} from "./components/GameRightDrawer";
import {CardDeck} from "./components/playingCards/CardDeck";
import {Alert, Card, Stack, StyledEngineProvider, Typography} from "@mui/material";

//Game object stuff- move out eventually
import * as Stomp from "@stomp/stompjs";
//const Stomp = StompJs.Stomp;
import ajax from 'can-ajax';
import SockJs from 'sockjs-client'
import {PlayingCard} from "./components/playingCards/PlayingCard";
import {StoryCard} from "./components/StoryCard";

export const UserContext = React.createContext(undefined);

class Game extends React.Component {
    constructor(props) {
        super(props);
        this.stompService = Stomp.client("ws://"+window.location.host+"/ws");
        //this.stompService = Stomp.over(function(){return new SockJs("/ws");});

        this.state = {
            game: {},
            hand: {},
            discardPile: {},
            storyCard: {},
            phase: "",
            status: "",
            stage: {stage: 0, building:null}
        };
    }

    setGame = (game, name) => {
        this.setState({...this.state, game: game, gameID: game.gameId, name: name, status: game.players.find(p=>p.name===name).status});
    }

    connectToGame = (game, name) => {
        this.setGame(game, name);

        this.stompService.connect({}, (frame) => {
            this.stompService.subscribe("/topic/game-progress/" + game.gameId, (response) => {

                console.log(JSON.parse(response.body));
                this.setGame(JSON.parse(response.body), name);
            });
            this.stompService.subscribe("/topic/discard-pile/" + game.gameId, (response) => {
                this.setState({...this.state, discardPile: JSON.parse(response.body)});
            });
            this.stompService.subscribe("/topic/cards-in-hand/" + game.gameId+"/"+this.state.name, (response) => {
                this.setState({...this.state, hand: JSON.parse(response.body)});
            });
            this.stompService.subscribe("/topic/display-story-card/" + game.gameId, (response) => {
                this.setState({...this.state, storyCard: JSON.parse(response.body)});
            });
            this.stompService.subscribe("/topic/sponsor-quest/"+game.gameId+"/"+this.state.name, response => {
                this.setState({...this.state, stage: {stage: 1, building: true}});
            });
            this.stompService.subscribe("/topic/build-quest-stage/"+game.gameId+"/"+this.state.name, response => {
                this.setState({...this.state, stage: {stage: response.body, building: true}});
            });
            this.stompService.subscribe("/topic/quest-build-complete/"+game.gameId, response => alert("Quest build completed!"));


            this.stompService.subscribe("/topic/play-against-quest-stage/"+game.gameId, response => {
                alert(response.body);
                //this.setState({...this.state, stage: {stage: 1, building: false}});
            });
            this.stompService.subscribe("/topic/fight-quest-stage/"+game.gameId+"/"+this.state.name, response => {
                //alert(response.body);
                this.setState({...this.state, stage: {stage: response.body, building: false}});
            });

            this.stompService.subscribe("/topic/quest-winner/"+game.gameId+"/"+this.state.name, response => alert(response.body));
            this.stompService.subscribe("/topic/quest-eliminated/"+game.gameId+"/"+this.state.name, response => alert(response.body));


            this.stompService.send("/topic/game-progress/" + this.state.gameID, {}, JSON.stringify(game));
            this.stompService.send("/game/update-principal/" + this.state.gameID, {},
                JSON.stringify({"player": {"username": name},"gameId": this.state.gameID}));

        }, (error) => {
            console.log(error);
        });

    }

    startGame = () => {
        ajax({
            url: "/game/play-game?gameId=" + this.state.gameID,
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({player: {username: this.state.name}, gameId: this.state.gameID})
        });
    }

    updateHand = (data) =>{
        //this.stompService.send("/topic/start-game/" + this.state.gameID, {}, JSON.stringify({"player": {"username": this.state.name},"gameId": this.state.gameID}));
        console.log("received")
        console.log(JSON.parse(data));
        this.setState({...this.state, hand:JSON.parse(data)});
    }

    setPhase = (phase) =>{
        this.setState({...this.state, phase: phase});
    }


    render(){
        return(<React.Fragment>
            <UserContext.Provider value={{gameID: this.state.gameID, name: this.state.name, phase: this.state.phase, setPhase: this.setPhase}}>
                <JoinDialog stompService = {this.stompService} connectToGame = {this.connectToGame}/>
                <GameBottomAppBar/>
                <GameRightDrawer players={this.state.game.players??""} status={this.state.game.status} startGame={this.startGame}/>
                <Stack sx={{flexGrow: 1, width: "80%", anchor: "left"}}>
                        <Card>
                            {this.state.storyCard.name?<StoryCard card={this.state.storyCard}/>:""}
                        </Card>
                    <Stack>
                        <CardDeck cards={this.state.discardPile} interactive={false}/>

                        <CardDeck cards={this.state.hand} interactive={true} stage={this.state.stage} />
                    </Stack>
                </Stack>
            </UserContext.Provider>
        </React.Fragment>);
    }
}



ReactDOM.render(<Game/>, document.getElementById('react'));

