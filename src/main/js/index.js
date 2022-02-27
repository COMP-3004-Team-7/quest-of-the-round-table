import * as React from 'react';
import ReactDOM from 'react-dom';
import JoinDialog from "./components/dialogs/JoinDialog.jsx";

class Game extends React.Component {
    render(){return <div><JoinDialog/></div>}

}

ReactDOM.render(
    <Game />,
    document.getElementById('react')
)
