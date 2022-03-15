import * as React from 'react';
import {Box, Menu, MenuItem, Paper, Typography} from "@mui/material";
import ajax from "can-ajax";


class PlayingCard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {selected: false, open: false};
        this.cardRef = React.createRef();
    }

    handleClick = () => {
        this.setState({...this.state, open: true});
    }
    handleClose = () => {
        this.setState({...this.state, open:false});
    }

    handleDiscard = () =>{
        ajax({
            url: "/game/discard-cards?gameId=" + this.props.gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": this.props.name},
                "gameId": this.props.gameID,
                "card": this.props.card
            })
        });
        this.handleClose();
    }

    render = () => {
        return (
            <Box sx={{width: 100, height: 140, rotation:this.props.rotation??0, p:2}} ref={this.cardRef} style={this.props.style}>
                {this.props.interactive?<Menu open={this.state.open} onClose={this.handleClose} anchorEl={this.cardRef.current} onHover>
                    <MenuItem onClick={this.handleDiscard}>Discard Card</MenuItem>
                    <MenuItem onClick={this.handleClose}>Add card to Event</MenuItem>
                </Menu>:""}
                <Paper sx={{width: "100%", height: "100%"}}  onClick={this.handleClick}>
                    <Typography variant="h4">{this.props.card.name}</Typography>
                </Paper>
            </Box>
            );
    }
}

export {PlayingCard}