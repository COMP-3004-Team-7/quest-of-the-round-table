import * as React from 'react';
import ReactDOM from 'react-dom';
import {JoinDialog} from "./components/dialogs/JoinDialog";
import {GameBottomAppBar} from "./components/GameBottomAppBar";
import {GameRightDrawer} from "./components/GameRightDrawer";
import {CardDeck} from "./components/playingCards/CardDeck";
import {Stack, StyledEngineProvider} from "@mui/material";

//Game object stuff- move out eventually
import * as Stomp from "@stomp/stompjs";
//const Stomp = StompJs.Stomp;
import ajax from 'can-ajax';
import SockJs from 'sockjs-client'


class Game extends React.Component {
    constructor(props) {
        super(props);
        this.stompService = Stomp.client("ws://"+window.location.host+"/ws");
        //this.stompService = Stomp.over(function(){return new SockJs("/ws");});

        this.state = {
            gameID: "",
            game: {},
            name: "",
            hand: {},
            discardPile: {}
        };
    }

    setGame = (game, name) => {
        this.setState({...this.state, game: game, gameID: game.gameId, name: name});
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
            this.stompService.send("/topic/game-progress/" + this.state.gameID, {}, JSON.stringify(game));
            this.stompService.send("/game/update-principal/" + this.state.gameID, {},
                JSON.stringify({"player": {"username": this.state.name},"gameId": this.state.gameID}));

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




    render(){
        return(<React.Fragment>
            <JoinDialog stompService = {this.stompService} connectToGame = {this.connectToGame}/>
            <GameBottomAppBar gameID={this.state.gameID??""} name={this.state.name}/>
            <GameRightDrawer gameID={this.state.gameID??""} players={this.state.game.players??""} name={this.state.name} startGame={this.startGame}/>
            <Stack sx={{flexGrow: 1, width: "80%", anchor: "left"}}>
                <p>
                    Main window
                </p>
                <Stack>
                    <p>
                        Card Stack 1
                    </p>

                    <CardDeck cards={this.state.discardPile} interactive={false}/>

                    <CardDeck cards={this.state.hand} interactive={true} name={this.state.name} gameID={this.state.gameID}/>
                </Stack>
            </Stack>

        </React.Fragment>);
    }
}



ReactDOM.render((
        <Game/>
), document.getElementById('react'));


