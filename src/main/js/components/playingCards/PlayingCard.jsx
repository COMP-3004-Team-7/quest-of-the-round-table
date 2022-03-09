import * as React from 'react';
import {Box, Paper, Typography} from "@mui/material";


class PlayingCard extends React.Component {
    constructor(props) {
        super(props);
        this.setState({selected: false});

    }

    render() {
        return (
            <React.Fragment>
                <Box sx={{width: 100, height: 140, rotation:this.props.rotation??0, p:2}}>
                    <Paper sx={{width: "100%", height: "100%"}}>
                        <Typography variant="h4">{this.props.cardID}</Typography>
                    </Paper>
                </Box>
            </React.Fragment>);
    }
}

export {PlayingCard}