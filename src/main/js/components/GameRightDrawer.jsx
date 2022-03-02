import * as React from 'react';
import {
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

class GameRightDrawer extends React.Component{
    constructor(props) {
        super(props);

    }

    render(){
        return(
            <React.Fragment>
                <Drawer variant="permanent" anchor="right" allign- sx={{width:"40%", flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: '20%',
                        boxSizing: 'border-box',
                    }}}>
                    <Stack>
                        <Card sx={{m:2, p:2}}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom component="div">Players</Typography>
                                <Stepper orientation="vertical">
                                    {this.props.players? this.props.players.map((player) =>
                                        (<Step key={player.username}>
                                            <StepLabel>{player.username}</StepLabel>
                                        </Step>)
                                    ) :<Skeleton/>}
                                </Stepper>
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