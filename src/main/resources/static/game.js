
let stompClient;
let gameId;
let playerType;
let playerName;


$('document').ready(function(){
    //Hide game-board div until players connect
    document.getElementById("game-board").style.display = "none";

    //Set Event Listeners
    document.getElementById("create-game-button").addEventListener("click", createGame);
    document.getElementById("join-game-button").addEventListener("click", connectToSpecificGame);
});

//connect to socket function (when user creates game or joins game)
function connectToSocket(gameId){
    console.log('connecting to game')
    let socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame){
        console.log("connected to the frame: " + frame);
        stompClient.subscribe("/topic/game-progress/" + gameId, function (response){
            let data = JSON.parse(response.body)
            console.log(data);
            updatePlayerNames(data);
        });
    });
}

//Create new game
function createGame(){
    playerName = document.getElementById("create-name").value;
    if(playerName == null || playerName === ""){
        alert("please enter name")
    }
    else{
        $.ajax({
            url: "/game/start",
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "name": playerName
            }),
            success: function(data){
                gameId = data.gameId;
                playerType = 1;
                connectToSocket(gameId);
                alert("You have created a game. Game Id is: " + data.gameId)
                document.getElementById("game-board").style.display = "block";
                document.getElementById("available-games").style.display = "none";
                setTimeout(function() {
                    stompClient.send("/topic/game-progress/" + gameId, {}, JSON.stringify(data));
                }, 500);
            },
            error: function(error){
                console.log(error);
            }
        })
    }
}

//Connect to existing game (max 4 players)
function connectToSpecificGame(){
        playerName = document.getElementById("create-name").value;
        if(playerName == null || playerName === ""){
            alert("please enter name")
        }
        else{
            let gameId = document.getElementById("join-game").value;
            if(gameId == null || gameId === ""){
                        alert("please enter game ID")
            }
            $.ajax({
                url: "/game/connect",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "player": {
                        "name": playerName
                    },
                    "gameId": gameId
                }),
                success: function(data){
                    gameId = data.gameId;
                    let numPlayers = data.players.length
                    console.log("NUM PLAYERS = " + numPlayers)
                    playerType = numPlayers;
                    connectToSocket(gameId);
                    alert("You have joined a game. Game Id is: " + data.gameId)
                    document.getElementById("game-board").style.display = "block";
                    document.getElementById("available-games").style.display = "none";
                    setTimeout(function() {
                        stompClient.send("/topic/game-progress/" + gameId, {}, JSON.stringify(data));
                    }, 500);
                },
                error: function(error){
                    console.log(error);
                }
            });
        }
}

//Update player names
function updatePlayerNames(game){
    let players = game.players;
    console.log("PLAYERS = " + players);
    console.log("Player length = " + players.length)
    //Build string of player names
    let playerList = "<h4>Player Names</h4>"
    for(let i = 0; i < players.length; i++){
        console.log(players[i].name);
        playerList = playerList + "<p>" + players[i].name + "</p>";
    }
    console.log(playerList);
    document.getElementById("current-player-names").innerHTML = playerList;
}
