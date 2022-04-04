import * as React from 'react';
import {Box, Menu, MenuItem, Paper, Typography} from "@mui/material";
import ajax from "can-ajax";
import {CardAddDialog} from "../dialogs/CardAddDialog";
import {UserContext} from '../../index.js'



class PlayingCard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {dialogOpen: false, open: false};
        this.cardRef = React.createRef();
    }

    handleClick = () => {
        this.setState({...this.state, open: true});
    }
    handleClose = () => {
        this.setState({...this.state, open:false});
    }
    handleAdd = () => {
        this.setState({...this.state, dialogOpen: true})
    }

    handleDiscard = (e, gameID, name) =>{
            ajax({
            url: "/game/discard-cards?gameId=" + gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": name},
                "gameId": gameID,
                "card": this.props.card
            })
        });
        this.handleClose();
    }

    handleDialogClose = () =>{
        this.setState({...this.state, dialogOpen: false})
    }

    render = () => {
        return (
            <Box sx={{width: 100, height: 140, rotation:this.props.rotation??0, p:2}} ref={this.cardRef} style={this.props.style}>
                <UserContext>
                    {({gameID}) => <CardAddDialog card={this.props.card} open={this.state.dialogOpen} handleClose={this.handleDialogClose} gameID={gameID} stage={this.props.stage}/>}
                </UserContext>
                {this.props.interactive?<Menu open={this.state.open} onClose={this.handleClose} anchorEl={this.cardRef.current} onHover>
                    <UserContext.Consumer>
                        {({gameID, name}) => <MenuItem onClick={e => this.handleDiscard(e, gameID, name)}>Discard Card</MenuItem>}
                    </UserContext.Consumer>
                    <MenuItem onClick={this.handleAdd}>Add card to Event</MenuItem>
                </Menu>:""}
                <Paper sx={{width: "100%", height: "100%"}} onAuxClick={this.handleClick} onClick={this.handleClick}>
                    <Typography variant="h4">{this.props.card.name}</Typography>
                    <Typography variant="subtitle">{this.props.card.minbattlepoints+"/"+this.props.card.maxbattlepoints}</Typography>
                </Paper>
            </Box>
            );
    }
}

export {PlayingCard}