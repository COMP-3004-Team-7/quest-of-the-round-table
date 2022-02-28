import * as React from 'react';
import ReactDOM from 'react-dom';
import {JoinDialog} from "./components/dialogs/JoinDialog";
//Game object stuff- move out eventually
import SockJS from 'sockjs-client';
import Stomp from 'stomp-websocket';

class Game extends React.Component {
    constructor(props) {
        super(props);
        let socket = new SockJS("/ws");
        this.state = {
            stompService: Stomp.over(socket)
        }
    }

    render(){
        return <JoinDialog stompService = {this.state.stompService}/>;
    }
}



ReactDOM.render(<Game />, document.getElementById('react'));


