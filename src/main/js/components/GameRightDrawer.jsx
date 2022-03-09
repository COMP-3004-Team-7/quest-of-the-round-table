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

class GameRightDrawer extends React.Component{
    constructor(props) {
        super(props);

    }


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
                                {this.props.players ? <Button variant="contained" onClick={this.props.startGame}>Start Game</Button>
                                    : <Skeleton/>}
                            </CardContent>
                        </Card>
                        <Card sx={{m:2, p:2}}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom component="div">Game ID</Typography>
                                {this.props.gameID? <Typography variant="h5" gutterBottom component="div">{this.props.gameID}</Typography> : <Skeleton/>}
                            </CardContent>
                        </Card>
                    </Stack>
                </Drawer>
            </React.Fragment>

        )};

}

export {GameRightDrawer}