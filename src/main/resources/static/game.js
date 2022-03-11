let stompClient;
let gameId;
let playerType;
let playerName;

$('document').ready(function(){
    //Hide game-board div until players connect
    document.getElementById("game-board").style.display = "none";
    document.getElementById("sponsor-button").style.display = "none"; //hide
    document.getElementById("decline-sponsor-button").style.display = "none"; //hide

    //Set Event Listeners
    document.getElementById("create-game-button").addEventListener("click", createGame);
    document.getElementById("join-game-button").addEventListener("click", connectToSpecificGame);
    document.getElementById("start-game-button").addEventListener("click", startGame);
    document.getElementById("draw-card-button").addEventListener("click", drawCard);
    document.getElementById("discard-button").addEventListener("click", discardCard);
    document.getElementById("sponsor-button").addEventListener("click", sponsorQuest);
        document.getElementById("decline-sponsor-button").addEventListener("click", declineSponsorQuest);
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
        stompClient.subscribe("/user/topic/cards-in-hand/" + gameId, function (response){
            let data = JSON.parse(response.body)
            updateCardsInHand(data);
        });
        stompClient.subscribe("/topic/start-game/" + gameId, function (response){
            document.getElementById("draw-card-button").style.display = "block"; //show
            document.getElementById("start-game-button").style.display = "none"; //hide
        });
        stompClient.subscribe("/user/topic/game-progress/" + gameId, function (response){
            //Do Something With the Data -- Right now this is getting back the 12 initial cards that
            //are dealt to the Client/Player

        });
        stompClient.subscribe("/topic/display-story-card/" + gameId, function (response){
            let data = JSON.parse(response.body)
            console.log(data);
            alert("Story card is drawn: " + data.name)
        });
        stompClient.subscribe("/user/topic/sponsor-quest/" + gameId, function (response){
            //Do Something With the Data -- Right now this is getting back the 12 initial cards that
            //are dealt to the Client/Player
            document.getElementById("sponsor-button").style.display = "block";
            document.getElementById("decline-sponsor-button").style.display = "block";
        });
        stompClient.subscribe("/topic/discard-pile/" + gameId, function (response){
            let data = JSON.parse(response.body)
            console.log(data);
            alert("A card has been removed!")
        });
    });
}

function updateCardsInHand(data){
    console.log(data);
    console.log(data.length)
    if (data.length > 12){
        alert("Please discard down to 12 cards")
        document.getElementById("draw-card-button").style.display = "none";
    }
    else{
        document.getElementById("draw-card-button").style.display = "block";
    }
    showCards(data)
}

function showCards(data){
    let cardList = "<h4>Cards</h4>"

    for(let i = 0; i < data.length; i++){
        cardList = cardList + "<p>" + data[i].name + "</p>";
    }
    document.getElementById("main-player").innerHTML = cardList;
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
                "username": playerName
            }),
            //Receiving a 'Game' Object on Success
            success: function(data){
                gameId = data.gameId;
                playerType = 1;
                connectToSocket(gameId);
                alert("You have created a game. Game Id is: " + data.gameId)
                document.getElementById("game-board").style.display = "block";
                document.getElementById("available-games").style.display = "none";
                document.getElementById("draw-card-button").style.display = "none";
                setTimeout(function() {
                    stompClient.send("/topic/game-progress/" + gameId, {}, JSON.stringify(data));
                    stompClient.send("/app/update-principal/" + gameId, {},
                        JSON.stringify({"player": {"username": playerName},"gameId": gameId}));
                }, 200);
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
        gameId = document.getElementById("join-game").value;
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
                    "username": playerName
                },
                "gameId": gameId
            }),
            //Receiving a 'Game' Object on Success
            success: function(data){
                gameId = data.gameId;
                let numPlayers = data.players.length
                console.log("NUM PLAYERS = " + numPlayers)
                console.log("GAMEID = " + gameId)
                playerType = numPlayers;
                connectToSocket(gameId);
                alert("You have joined a game. Game Id is: " + data.gameId)
                document.getElementById("game-board").style.display = "block";
                document.getElementById("available-games").style.display = "none";
                document.getElementById("draw-card-button").style.display = "none";
                setTimeout(function() {
                    stompClient.send("/topic/game-progress/" + gameId, {}, JSON.stringify(data));
                    stompClient.send("/app/update-principal/" + gameId, {},
                        JSON.stringify({"player": {"username": playerName},"gameId": gameId}));
                }, 500);
            },
            error: function(error){
                console.log(error);
            }
        });
    }
}

function startGame(){
    $.ajax({
        url: "/game/play-game?gameId=" + gameId,
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({
            "player": {"username": playerName},
            "gameId": gameId}),
        //Receiving nothing back -> update DOM elements to start game
        success: function(string){
            stompClient.send("/topic/start-game/" + gameId, {}, JSON.stringify({"player": {"username": playerName},"gameId": gameId}));
        },
        error: function(error){
            console.log(error);
        }
    })
    //stompClient.send("/app/play-game/" + gameId, {}, JSON.stringify({"player": {"username": playerName},"gameId": gameId}));
}

function drawCard(){
    $.ajax({
        url: "/game/draw-card?gameId=" + gameId,
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({
            "player": {"username": playerName},
            "gameId": gameId}),
        //Receiving nothing back -> update DOM elements to start game
        success: function(data){
            console.log(data);
            updateCardsInHand(data);
        },
        error: function(error){
            console.log(error);
        }
    })
    //stompClient.send("/app/draw-card/" + gameId, {}, JSON.stringify({"player": {"username": playerName},"gameId": gameId}));
}

function discardCard(){
    $.ajax({
        url: "/game/discard-cards?gameId=" + gameId,
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({
            "player": {"username": playerName},
            "gameId": gameId}),
        //Receiving nothing back -> update DOM elements to start game
        success: function(data){
            console.log("Cards after discarding "+data);
            updateCardsInHand(data);
        },
        error: function(error){
            console.log(error);
        }
    })
    //stompClient.send("/app/discard-cards/" + gameId, {}, JSON.stringify({"player": {"username": playerName},"gameId": gameId}));
}
function sponsorQuest(){
    $.ajax({
        url: "/quest/sponsor-quest?gameId=" + gameId,
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({
            "player": {"username": playerName},
            "gameId": gameId}),
        //Receiving nothing back -> update DOM elements to start game
        success: function(response){
            console.log(response);
            document.getElementById("sponsor-button").style.display = "none";
            document.getElementById("decline-sponsor-button").style.display = "none";
            alert("You have sponsored " + response.name);
        },
        error: function(error){
            console.log(error);
        }
    })
}
function declineSponsorQuest(){
    $.ajax({
        url: "/quest/decline-sponsor-quest?gameId=" + gameId,
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({
            "player": {"username": playerName},
            "gameId": gameId}),
        //Receiving nothing back -> update DOM elements to start game
        success: function(data){
            if(data !== "Two player game"){ //IF the player size is 2 -> then the last player becomes the main player (race condition)
                document.getElementById("sponsor-button").style.display = "none";
                document.getElementById("decline-sponsor-button").style.display = "none";
            }
        },
        error: function(error){
            console.log(error);
        }
    })
}
//Update player names
function updatePlayerNames(game){
    let players = game.players;
    console.log("PLAYERS = " + players);
    console.log("Player length = " + players.length);
    console.log("Game Id = " + game.gameId);
    //Build string of player names
    let playerList = "<h4>Player Names</h4>"
    for(let i = 0; i < players.length; i++){
        playerList = playerList + "<p>" + players[i].username + "</p>";
    }
    document.getElementById("current-player-names").innerHTML = playerList;
}