import * as React from 'react'
import {Card, Stack, Typography} from "@mui/material";

class StoryCard extends React.Component{

    constructor(props) {
        super(props);
        this.state = {stages: []};
    }

    renderStages = () => {
        let cards = [];
        for (let i = 0; i < this.props.card.stages; i++) {
            cards.push(<Card sx={{height: 140, width: 100}}><Typography variant="h2">?</Typography></Card>);
        }

        return cards;
    }
    componentDidUpdate(prevProps, prevState, snapshot) {
        if(prevProps.card !== this.props.card && this.props.card.type === "Event") alert(this.props.card.name);
    }

    render(){
        return(<Card direction ={"row"} sx={{m:2, p:2}}>
                <Typography variant="h2">{"Story Card: "+this.props.card.name}</Typography>
                <Stack spacing={2} direction="row">
                    {
                        this.renderStages()
                    }
                </Stack>

            </Card>
        );
    }
}

export {StoryCard}