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
    socket.on('disconnect', () => {
        console.log('Player disconnected');
        socket.broadcast.emit('playerDisconnected', {id: socket.id})
        players.forEach(s => {
            if(s.id === socket.id){
                players.splice(s.index, 1)
            }
        })
    })

    players.push(player(socket.id, (1280 / 2 - 16), (720 / 2 - 32) ))
    });

function player(id, x, y) {
    this.id = id
    this.x = x
    this.y = y
}



