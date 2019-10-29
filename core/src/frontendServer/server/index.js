let app = require('express')();
let server = require('http').Server(app);
let io = require('socket.io')(server);

server.listen(8080, () =>
    console.log("Server is running.."));

io.on('connection', (socket) => {
    console.log("Player connected")

    socket.emit('socketID', { id: socket.id})
    socket.broadcast.emit('newPlayer', { id: socket.id})
    socket.on('disconnect', () =>
        console.log("Player disconnected"))
    });


