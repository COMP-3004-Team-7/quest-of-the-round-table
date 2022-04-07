import * as React from 'react'
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import TextField from "@mui/material/TextField";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import ajax from "can-ajax";
import {UserContext} from '../../index.js'
import {Switch} from "@mui/material";


class CardAddDialog extends React.Component{
    constructor(props) {
        super(props);
    }


    submit = (gameID, name) => {
        let key ={
            build: "quest/submit-completed-quest-stage",
            quest: "quest/complete-cards-played-against-foe",
            tournament: "tournament/complete-submitting-tournament-cards",
            null: ""
        }

        this.addCard(gameID, name).then(() => ajax({
            url: "/"+key[this.props.stage.buildFor]+"?gameId="+gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": name},
                "gameId": gameID,
                "stage": this.props.stage.stage
            }),
            success: e => alert("Success"),
            error: e=> alert(e.responseText )
        }));

    }

    addCard = (gameID, name) => {
        let key = {
            build: "quest/select-card-for-sponsored-quest-stage",
            quest: "quest/submit-card-against-foe",
            tournament: "tournament/submit-tournament-card",
            null: ""
        }

        return ajax({
            url: "/"+key[this.props.stage.buildFor]+"?gameId="+gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": name},
                "gameId": gameID,
                "card": this.props.card,
                "stage": this.props.stage.stage
            }),
            success: () => this.props.handleClose(),
            error: () => this.props.handleClose()
        });

    }

    onChangeStage = (e) =>{
        this.setState({...this.state, stage: e.target.value});
    }


    render() {
        return (
            <Dialog open={this.props.open} onClose={this.props.handleClose}>
                <DialogTitle>Game Setup</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Add a card to the current quest stage
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <UserContext.Consumer>
                        {({gameID, name}) => (<React.Fragment>
                            <Button onClick={this.props.handleClose}>Cancel</Button>
                            <Button onClick={()=>this.addCard(gameID, name)}>Add Weapon</Button>
                            <Button onClick={()=>this.submit(gameID, name)}>Continue</Button>
                        </React.Fragment>)}
                    </UserContext.Consumer>
                </DialogActions>
            </Dialog>);
    }
}

export {CardAddDialog}