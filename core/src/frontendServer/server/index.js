let app = require('express')();
let server = require('http').Server(app);
let io = require('socket.io')(server);

let players = []

server.listen(8080, () =>
    console.log("Server is running.."));

io.on('connection', (socket) => {
    console.log("Player connected")

    socket.emit('socketID', { id: socket.id})
    socket.emit('getPlayers', players)
    socket.broadcast.emit('newPlayer', { id: socket.id})
    socket.on('playerMoved', (data) =>{
        data.id = socket.id
        socket.broadcast.emit('playerMoved', data)
        for (let i=0; i < players.length; i++){
            if(players[i].id === data.id){
                players[i].x = data.x
                players[i].y = data.y
            }
        }
    })
    socket.on('disconnect', () => {
        console.log('Player disconnected');
        socket.broadcast.emit('playerDisconnected', {id: socket.id})
        for (let i=0; i < players.length; i++){
            if(players[i].id === socket.id){
                players.splice(i,1)
            }
        }
    });

    players.push(new player(socket.id, (1280 / 2 - 16), (720 / 2 - 32) ))
    for (let i=0; i < players.length; i++){
        console.log(players[i])
    }
    });

    function player(id, x, y) {
        this.id = id
        this.x = x
        this.y = y
    }



