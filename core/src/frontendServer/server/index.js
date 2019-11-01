let app = require('express')();
let server = require('http').Server(app);
let io = require('socket.io')(server);
const engine = require('./physic-loop');
const Agent = require('./models/Agent');
const Pistol = require('./models/Pistol');
const constants = require('./settings/constants');

const agents = engine.agents;
const projectiles = engine.projectiles;
let players = [];

let loopAlreadyRunning = false;

server.listen(8080, () =>
    console.log("Server is running.."));

io.on('connection', (socket) => {
    console.log("Player connected");


    socket.emit('socketID', {id: socket.id});

    // socket.emit('getPlayers', players);

    // socket.broadcast.emit('newPlayer', {id: socket.id});

    // socket.on('playerMoved', (data) => {
    //     data.id = socket.id;
    //     socket.broadcast.emit('playerMoved', data);
    //     for (let i = 0; i < players.length; i++) {
    //         if (players[i].id === data.id) {
    //             players[i].x = data.x;
    //             players[i].y = data.y;
    //         }
    //     }
    // });

    socket.on('startKey', (data) => {
        switch (Object.keys(data)[0]) {
            case "W":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isWPressed = true;
                    }
                }
                break;
            case "A":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isAPressed = true;
                    }
                }
                break;
            case "S":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isSPressed = true;
                    }
                }
                break;
            case "D":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isDPressed = true;
                    }
                }
                break;
        }
        // console.log(Object.keys(data)[0] + " key is pressed down")
    });

    socket.on('stopKey', (data) => {
        // console.log(Object.keys(data)[0] + " key is released")

        switch (Object.keys(data)[0]) {
            case "W":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isWPressed = false;
                    }
                }
                break;
            case "A":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isAPressed = false;
                    }
                }
                break;
            case "S":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isSPressed = false;
                    }
                }
                break;
            case "D":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isDPressed = false;
                    }
                }
                break;
        }
    });

    socket.on('mouseStart', (data) => {
        // console.log(Object.keys(data)[0] + " just pressed");
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents[i].isLMPressed = true;
            }
        }
    });

    socket.on('mouseStop', (data) => {
        // console.log(Object.keys(data)[0] + " just released")
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents[i].isLMPressed = false;
            }
        }
    });

    socket.on('playerRotation', (data) => {
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents[i].facingDirectionAngle = Object.values(data)[0]
            }
        }
        // console.log("playerId:", socket.id, ",,,  ", Object.keys(data)[0] + " is", Object.values(data)[0])
    });

    socket.on('disconnect', () => {
        // console.log('Player disconnected with id:', socket.id);
        socket.broadcast.emit('playerDisconnected', {id: socket.id});
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents.splice(i, 1)
            }
        }
    });

    console.log("Adding new player with id " + socket.id);
    const agent = new Agent(500, 500, new Pistol(), 0, socket.id);
    agents.push(agent);

    if (!loopAlreadyRunning) {
        gameDataLoop(socket);
        loopAlreadyRunning = true;
    }
});

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

engine.lastLoop = new Date().getTime();
engine.physicLoop();

async function gameDataLoop(socket) {

    while (true) {
        const agentData = [];

        for (agent of agents) {
            agentData.push({
                x: agent.bounds.position.x,
                y: agent.bounds.position.y,
                id: agent.id
            })
        }

        const projectileData = [];

        for (projectile of projectiles) {
            projectileData.push({
                x: projectile.bounds.position.x,
                y: projectile.bounds.position.y,
                id: projectile.id,
                xSpeed: projectile.velocity.x,
                ySpeed: projectile.velocity.y
            })
        }

        socket.broadcast.emit("gameData", {agentData, projectileData});
        await sleep(100)
    }

}

