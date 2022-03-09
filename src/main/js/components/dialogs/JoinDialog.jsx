import * as React from 'react';
import ajax from 'can-ajax';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import TrapFocus from '@mui/base/Unstable_TrapFocus';

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
        this.setState({...this.state, gameID: e.target.value});
    }

    //API consumer

    connectToSpecificGame = () => {
        if(this.state.name == null || this.state.name === ""){
            alert("please enter name")
        }
        else{
            if(this.state.gameID == null || this.state.gameID === ""){
                alert("please enter game ID")
            }
            ajax({
                url: "/game/connect",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "player": {
                        "name": this.state.name,
                        "username": this.state.name
                    },
                    "gameId": this.state.gameID
                }),
                //Receiving a 'Game' Object on Success
                success: (data) => {
                    this.setState({...this.state, gameID: data.gameId});
                    this.props.connectToGame(data, this.state.name);

                    this.handleClose();
                },
                error: function(error){
                    console.log(error);
                }
            });
        }
    }

    createGame = () => {
        if(this.state.name == null || this.state.name === ""){
            alert("please enter name")
        }
        else{
            ajax({
                url: "/game/start",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "name": this.state.name,
                    "username": this.state.name
                }),
                //Receiving a 'Game' Object on Success
                success: (data) => {
                    this.setState({...this.state, gameID: data.gameId});
                    //playerType = 1;
                    this.props.connectToGame(data, this.state.name);

                    this.handleClose();
                },
                error: function(error){
                    console.log(error);
                }
            });
        }
    }

    render() {
        return (
            <TrapFocus open={this.state.open}>
                <Dialog open={this.state.open}>
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
            </TrapFocus>);
    }
}

export{JoinDialog}
