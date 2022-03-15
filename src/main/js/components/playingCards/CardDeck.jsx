import * as React from 'react';
import {Box, Paper, Typography} from "@mui/material";
import {PlayingCard} from "./PlayingCard";

const PADDING = 4;

class CardDeck extends React.Component{
    constructor(props) {
        super(props);
        this.area = React.createRef();

    }

    render(){
        return(
            <React.Fragment>
                <div ref = {this.area} style = {{position:"relative", display:"grid"}}>
                    {this.props.cards.values?this.props.cards.map((card, index, arr)=>{
                        let style = {'gridArea': "1/1",
                            'marginLeft': PADDING+index*((this.area.current.offsetWidth-2*PADDING-100)/arr.length),
                            'marginTop': PADDING ,
                            'padding': 0};

                        return(<PlayingCard card = {card} interactive = {this.props.interactive} style = {style} gameID = {this.props.gameID} name = {this.props.name}/>);
                    }):""}
                </div>
            </React.Fragment>
        )
    }
}

export {CardDeck}