import React, {useContext} from 'react';
import {AppBar, Fab, Typography} from "@mui/material";
import Icon from '@mui/material/Icon';
import ajax from "can-ajax";

import UserContext from '../index.js'


class GameBottomAppBar extends React.Component{

    drawCard = () =>{
        ajax({
            url: "/game/draw-card?gameId=" + this.context.gameID,
            type: 'POST',
            contentType: "application/json",
            data: JSON.stringify({
                "player": {"username": this.context.name},
                "gameId": this.context.gameID})
        });
    }

    render(){
        return(
      <React.Fragment>
          <AppBar position="fixed" color="primary" sx={{ top: 'auto', bottom: 0, zIndex: 1500, p:2}}>
                <Typography variant="h6">Quest of the Round Table</Typography>
              <Fab sx={{position: 'absolute', zIndex: 1, top: -30, left: 0, right: 0, margin: '0 auto'}} onClick={this.drawCard}>
                  <Icon>queue</Icon>
              </Fab>
          </AppBar>
      </React.Fragment>
    )};

}

export {GameBottomAppBar}