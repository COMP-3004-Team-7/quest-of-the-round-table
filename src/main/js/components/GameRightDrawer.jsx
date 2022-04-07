import * as React from 'react';
import {
    Button,
    Card,
    CardContent,
    Drawer,
    Skeleton,
    Stack,
    Step,
    StepLabel,
    Stepper,
    Typography
} from "@mui/material";
import ajax from 'can-ajax';
import {UserContext} from '../index.js'


class GameRightDrawer extends React.Component{
    render(){
        return(
            <React.Fragment>
                <Drawer variant="permanent" anchor="right" sx={{width:"40%", flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: '20%',
                        boxSizing: 'border-box'
                    }}}>
                    <Stack>
                        <Card sx={{m:2, p:2}}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom component="div">Players</Typography>
                                <Stepper orientation="vertical">
                                    {this.props.players? this.props.players.map((player) =>
                                        (<Step key={player.name}>
                                            <StepLabel>{player.name}</StepLabel>
                                        </Step>)
                                    ) :<Skeleton/>}
                                </Stepper>
                                <br/>
                                <Button variant="contained" onClick={this.props.startGame}>Start Game</Button>
                            </CardContent>
                        </Card>
                        <Card sx={{m:2, p:2}}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom component="div">Game ID</Typography>
                                <UserContext.Consumer>
                                    {({gameID}) => (<Typography variant="h5" gutterBottom component="div">{gameID}</Typography>)}
                                </UserContext.Consumer>
                            </CardContent>
                        </Card>
                        <Card sx={{m:2, p:2}}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom component="div">Quest</Typography>
                                <UserContext.Consumer>
                                    {({gameID, name}) => (
                                        <React.Fragment>
                                            <Button onClick={
                                                () => {ajax({
                                                url: "/quest/sponsor-quest?gameId=" + gameID,
                                                type: 'POST',
                                                dataType: "json",
                                                contentType: "application/json",
                                                data: JSON.stringify({player: {username: name}, gameId: gameID})
                                            })}}>Sponsor Quest</Button>
                                            <Button onClick={ () =>{ajax({
                                                url: "/quest/decline-sponsor-quest?gameId=" + gameID,
                                                type: 'POST',
                                                dataType: "json",
                                                contentType: "application/json",
                                                data: JSON.stringify({player: {username: name}, gameId: gameID})
                                            })}}>Decline Sponsor Quest</Button>
                                            <br/>
                                            <Button onClick={
                                                () => {ajax({
                                                    url: "/quest/join-current-quest?gameId=" + gameID,
                                                    type: 'POST',
                                                    dataType: "json",
                                                    contentType: "application/json",
                                                    data: JSON.stringify({player: {username: name}, gameId: gameID})
                                                })}}>Join Quest</Button>
                                            <Button onClick={ () =>{ajax({
                                                url: "/quest/decline-to-join-current-quest?gameId=" + gameID,
                                                type: 'POST',
                                                dataType: "json",
                                                contentType: "application/json",
                                                data: JSON.stringify({player: {username: name}, gameId: gameID})
                                            })}}>Decline Quest</Button>
                                            <Button onClick={
                                                () => {ajax({
                                                    url: "/tournament/join-tournament?gameId=" + gameID,
                                                    type: 'POST',
                                                    dataType: "json",
                                                    contentType: "application/json",
                                                    data: JSON.stringify({player: {username: name}, gameId: gameID})
                                                })}}>Join Tournament</Button>
                                            <Button onClick={ () =>{ajax({
                                                url: "/tournament/decline-to-join-tournament?gameId=" + gameID,
                                                type: 'POST',
                                                dataType: "json",
                                                contentType: "application/json",
                                                data: JSON.stringify({player: {username: name}, gameId: gameID})
                                            })}}>Decline Tournament</Button>
                                        </React.Fragment>
                                    )}
                                </UserContext.Consumer>
                            </CardContent>
                        </Card>
                    </Stack>
                </Drawer>
            </React.Fragment>
        )};

}

export {GameRightDrawer}