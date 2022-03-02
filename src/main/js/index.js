import * as React from 'react';
import ReactDOM from 'react-dom';
import {JoinDialog} from "./components/dialogs/JoinDialog";
import {GameBottomAppBar} from "./components/GameBottomAppBar";
import {Stack} from "@mui/material";
import {GameRightDrawer} from "./components/GameRightDrawer";
import {StyledEngineProvider} from "@mui/material";
//Game object stuff- move out eventually
import SockJS from 'sockjs-client';
import Stomp from 'stomp-websocket';


class Game extends React.Component {
    constructor(props) {
        super(props);
        let socket = new SockJS("/ws");
        this.state = {
            stompService: Stomp.over(socket),
            gameID: "",
            game: {}
        }
    }

    setGame = (game) => {
        this.setState({...this.state, game: game, gameID: game.gameId});

        let players = game.players;
        console.log("PLAYERS = " + players);
        console.log("Player length = " + players.length);
        console.log("Game Id = " + game.gameId);
    }

    render(){
        return(<React.Fragment>
            <JoinDialog stompService = {this.state.stompService} setGame = {this.setGame}/>
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
                    <p>
                        Card Stack 3
                    </p>
                </Stack>
            </Stack>
            <GameBottomAppBar/>
            <GameRightDrawer gameID={this.state.gameID??""} players={this.state.game.players??""}/>
        </React.Fragment>);
    }
}



ReactDOM.render((
    <StyledEngineProvider>
        <Game/>
    </StyledEngineProvider>), document.getElementById('react'));


