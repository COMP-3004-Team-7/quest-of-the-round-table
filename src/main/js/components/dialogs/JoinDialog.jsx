import * as React from 'react';
import $ from 'jquery';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

class JoinDialog extends React.Component {
    constructor(props) {
        super(props);
        this.state = {gameID: "", name: "", open: true};
    }

    //TODO we will want to put a handler in here to re-open this dialog when a game is ended- this will also probably need a STOMP client passed it
    setOpen(bool) {
        this.setState({gameID: this.state.gameID, open: bool});
    }

    handleClose = () =>{
        this.setOpen(false);
    }

    onChangeName = (e) => {
        this.setState({...this.state, name: e.target.value});
    }

    onChangeGameID = (e) => {
        this.setState({...this.state, GameID: e.target.value});
    }

    //API consumer
    connectToSocket = (gameId) => {
        this.props.stompService.connect({}, (frame) => {
            console.log("connected to the frame: " + frame);
            this.props.stompService.subscribe("/topic/game-progress/" + gameId, (response) => {
                let data = JSON.parse(response.body)
                console.log(data);
                this.updatePlayerNames(data);
            });
            this.props.stompService.subscribe("/user/topic/game-progress/" + gameId, (response) => {
                //Do Something With the Data -- Right now this is getting back the 12 initial cards that
                //are dealt to the Client/Player
            });
        });
    }

    connectToSpecificGame = () => {
        if(this.state.name == null || this.state.name === ""){
            alert("please enter name")
        }
        else{
            if(this.state.gameID == null || this.state.gameID === ""){
                alert("please enter game ID")
            }
            $.ajax({
                url: "/game/connect",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "player": {
                        "username": this.state.name
                    },
                    "gameId": this.state.gameID
                }),
                //Receiving a 'Game' Object on Success
                success: (data) => {
                    this.setState({...this.state, gameID: data.gameId});
                    let numPlayers = data.players.length
                    console.log("NUM PLAYERS = " + numPlayers)
                    console.log("GAMEID = " + this.state.gameID)
                    //playerType = numPlayers;
                    this.connectToSocket(this.state.gameID);
                    alert("You have joined a game. Game Id is: " + data.gameId)

                    setTimeout(() => {
                        this.props.stompService.send("/topic/game-progress/" + this.state.gameID, {}, JSON.stringify(data));
                        this.props.stompService.send("/app/update-principal/" + this.state.gameID, {},
                            JSON.stringify({"player": {"username": this.state.name},"gameId": this.state.gameID}));
                    }, 500);
                },
                error: function(error){
                    console.log(error);
                }
            });
            this.handleClose();
        }
    }

    createGame = () => {
        if(this.state.name == null || this.state.name === ""){
            alert("please enter name")
        }
        else{
            $.ajax({
                url: "/game/start",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "username": this.state.name
                }),
                //Receiving a 'Game' Object on Success
                success: (data) => {
                    this.setState({...this.state, gameID: data.gameId});
                    //playerType = 1;
                    this.connectToSocket(this.state.gameID);
                    alert("You have created a game. Game Id is: " + this.state.gameID);

                    setTimeout(() => {
                        this.props.stompService.send("/topic/game-progress/" + this.state.gameID, {}, JSON.stringify(data));
                        this.props.stompService.send("/app/update-principal/" + this.state.gameID, {},
                            JSON.stringify({"player": {"username": this.state.name},"gameId": this.state.gameID}));
                    }, 500);
                },
                error: function(error){
                    console.log(error);
                }
            });
            this.handleClose();
        }
    }


    startGame(){
        console.log("STARTING GAME... GAMEID = " + this.state.gameID);
        this.props.stompService.send("/app/play-game/" + this.state.gameID, {}, JSON.stringify({"player": {"username": this.state.name}, gameId: this.state.gameID}));
    }



    //Update player names
    updatePlayerNames = (game) => {
        let players = game.players;
        console.log("PLAYERS = " + players);
        console.log("Player length = " + players.length);
        console.log("Game Id = " + game.gameId);
    }

    render() {
        return (<div>
            <Dialog open={this.state.open} onClose={this.handleClose}>
                <DialogTitle>Game Setup</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Join a game by ID or create a new game
                    </DialogContentText>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="gameIDText"
                        label="Game ID"
                        type="text"
                        fullWidth
                        variant="standard"
                        onChange={this.onChangeGameID}
                    />
                    <TextField
                        autoFocus
                        margin="dense"
                        id="playerNameText"
                        label="Player Name"
                        type="text"
                        fullWidth
                        variant="standard"
                        onChange={this.onChangeName}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={this.createGame}>Create Game</Button>
                    <Button onClick={this.connectToSpecificGame}>Join Game</Button>
                </DialogActions>
            </Dialog>
        </div>);
    }
}

export{JoinDialog}
