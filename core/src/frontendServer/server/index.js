let app = require('express')();
let server = require('http').Server(app);
let io = require('socket.io')(server);
const shortid = require('shortid');
const engine = require('./physic-loop');
const Agent = require('./models/Agent');
const Pistol = require('./models/Pistol');
const MachineGun = require('./models/MachineGun');
const PistolPickup = require('./models/PistolPickup');
const MachineGunPickup = require('./models/MachineGunPickup');
const constants = require('./settings/constants');

const projectiles = engine.projectiles;
const agents = engine.agents;
const pickups = engine.pickups;

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
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "A":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isAPressed = true;
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "S":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isSPressed = true;
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "D":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isDPressed = true;
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "R":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isRPressed = true;
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
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "A":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isAPressed = false;
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "S":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isSPressed = false;
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "D":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isDPressed = false;
                        if (!agent.isRPressed) setVelocity(agents[i]);
                    }
                }
                break;
            case "R":
                for (let i = 0; i < agents.length; i++) {
                    if (agents[i].id === socket.id) {
                        agents[i].isRPressed = false;
                    }
                }
                break;
        }
    });

    socket.on('restart', () => {
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents[i].currentHealth = constants.PLAYER_MAX_HEALTH;
                agents[i].isDead = false;
                engine.moveAgent(agents[i], 500, 500);
                break;
            }
        }
    });

    socket.on('currentPlayerHealth', (data) => {
        let currentHealth = Object.values(data)[0];
        let id = Object.values(data)[1];
        //console.log(data)
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === id) {
                agents[i].currentHealth = currentHealth;
                //console.log(agents[i].currentHealth)
                //console.log("Player current health: " + agents[i].currentHealth);
            }
        }
    });

    socket.on('mouseStart', (data) => {
        // console.log(Object.keys(data)[0] + " just pressed");
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id && !agents[i].isDead) {
                agents[i].isLMPressed = true;
            }
        }
    });

    socket.on("pickWeapon", data => {
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents[i].pickWeapon = true;
            }
        }
    });

    socket.on('isDead', (data) => {
        let isDead = Object.values(data)[1];
        let id = Object.values(data)[0];
        //console.log("playerId: " + id);
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === id) {
                agents[i].isDead = isDead;
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
        console.log('Player disconnected, id:', socket.id);
        socket.broadcast.emit('playerDisconnected', {id: socket.id});
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents.splice(i, 1)
            }
        }
    });

    console.log("Adding new player, id " + socket.id);
    const agent = new Agent(500, 500, "Rami",  false, constants.PLAYER_MAX_HEALTH, new Pistol(), 0, socket.id);
    agents.push(agent);

    if (!loopAlreadyRunning) {
        loopAlreadyRunning = true;
        pickups.push(new PistolPickup(300, 300, shortid.generate()));
        pickups.push(new MachineGunPickup(400, 400, shortid.generate()));
        engine.lastLoop = new Date().getTime();
        engine.physicLoop(projectile => {

            socket.broadcast.emit("newProjectile", {
                x: projectile.bounds.position.x,
                y: projectile.bounds.position.y,
                id: projectile.id,
                xSpeed: projectile.velocity.x,
                ySpeed: projectile.velocity.y,
                type: projectile.type
            })
        });
        gameDataLoop(socket);
    }
});

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

async function gameDataLoop(socket) {

    while (true) {
        const agentData = [];

          for (agent of agents) {
            //console.log(agent.currentHealth)
              //console.log("x:", agents[i].bounds.position.x, ",y:", agents[i].bounds.position.y)

              agentData.push({
                x: agent.bounds.bounds.min.x,
                y: agent.bounds.bounds.min.y,
                name: agent.name,
                xVelocity: agent.velocity.x,
                yVelocity: agent.velocity.y,
                bulletsLeft: agent.reloadMark === -1 ? agent.weapon.bulletsInChamber : -1,
                isDead: agent.isDead,
                currentHealth: agent.currentHealth,
                id: agent.id,
                weapon: agent.weapon.projectileType,
            })
        }

        const projectileData = [];

        for (projectile of projectiles) {
            projectileData.push({
                x: projectile.bounds.position.x,
                y: projectile.bounds.position.y,
                id: projectile.id,
                xSpeed: projectile.velocity.x,
                ySpeed: projectile.velocity.y,
                type: projectile.type
            })
        }

        const pickupData = [];

        for (pickup of pickups) {
            pickupData.push({
                x: pickup.bounds.bounds.min.x,
                y: pickup.bounds.bounds.min.y,
                type: pickup.type,
                id: pickup.id,
            })
        }

        socket.broadcast.emit("gameData", {agentData, projectileData, pickupData});
        await sleep(1000 / constants.UPDATES_PER_SECOND)
    }

}

function setVelocity(agent) {
    agent.velocity.x = 0;
    agent.velocity.y = 0;

    let movementSpeed = constants.PLAYER_MOVEMENT_SPEED;
    let pressedKeys = 0;

    if (agent.isWPressed) pressedKeys++;
    if (agent.isAPressed) pressedKeys++;
    if (agent.isSPressed) pressedKeys++;
    if (agent.isDPressed) pressedKeys++;

    if (pressedKeys > 1) movementSpeed *= 0.7;

    if (agent.isWPressed) agent.velocity.y += movementSpeed;
    if (agent.isSPressed) agent.velocity.y -= movementSpeed;
    if (agent.isAPressed) agent.velocity.x -= movementSpeed;
    if (agent.isDPressed) agent.velocity.x += movementSpeed;
}

