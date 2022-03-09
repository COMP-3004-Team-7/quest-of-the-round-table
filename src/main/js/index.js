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
        //this.stompService = Stomp.client("ws://"+window.location.host+"/ws");
        this.stompService = Stomp.over(function(){return new SockJs("/ws");});

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
                console.log(JSON.parse(response.body));
            });
            this.stompService.subscribe("/user/topic/cards-in-hand/" + game.gameId, (response) => {
                console.log(JSON.parse(response.body));
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
            data: JSON.stringify({player: {username: this.state.name}, gameId: this.state.gameID}),
            success: this.updateHand
        });
    }

    updateHand = (data) =>{
        this.stompService.send("/topic/start-game/" + this.state.gameID, {}, JSON.stringify({"player": {"username": this.state.name},"gameId": this.state.gameID}));
        console.log(data)
    }



    render(){
        return(<React.Fragment>
            <JoinDialog stompService = {this.stompService} connectToGame = {this.connectToGame}/>
            <Stack>
                <p>
                    Main window
                </p>
                <Stack>
                    <p>
                        Card Stack 1
                    </p>
                    <p>
                        Card Stack 2
                    </p>
                    <CardDeck />
                </Stack>
            </Stack>
            <GameBottomAppBar/>
            <GameRightDrawer gameID={this.state.gameID??""} players={this.state.game.players??""} name={this.state.name} startGame={this.startGame}/>
        </React.Fragment>);
    }
}



ReactDOM.render((
        <Game/>
), document.getElementById('react'));


