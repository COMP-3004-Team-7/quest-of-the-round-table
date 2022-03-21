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
        this.state = {stage: 0, building: true};
    }


    submit = (gameID, name) => {
        this.addCard(gameID, name);
        ajax({
            url: "/quest/"+(this.state.building?"submit-completed-quest-stage" : "complete-cards-played-against-foe")+"?gameId="+gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": name},
                "gameId": gameID,
                "stage": this.state.stage
            })
        });
    }

    addCard = (gameID, name) => {
        ajax({
            url: "/quest/"+(this.state.building?"select-card-for-sponsored-quest-stage" : "submit-card-against-foe")+"?gameId="+gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": name},
                "gameId": gameID,
                "card": this.props.card,
                "stage": this.state.stage
            })
        });
        this.props.handleClose();
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
                    <TextField
                        autoFocus
                        margin="dense"
                        id="stageInput"
                        label="Stage"
                        type="Number"
                        fullWidth
                        variant="standard"
                        onChange={this.onChangeStage}
                    />
                    <Switch defaultChecked label={"Building"} onChange={e=>this.setState({...this.state, building: e.target.checked})}/>

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