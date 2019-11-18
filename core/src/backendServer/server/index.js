let app = require('express')();
let server = require('http').Server(app);
let io = require('socket.io')(server);
const shortid = require('shortid');
const engine = require('./physic-loop');
const Agent = require('../models/Agent');
const Pistol = require('../models/Pistol');
const MachineGun = require('../models/MachineGun');
const PistolPickup = require('../models/PistolPickup');
const MachineGunPickup = require('../models/MachineGunPickup');
const constants = require('../settings/constants');

const projectiles = engine.projectiles;
const agents = engine.agents;
const pickups = engine.pickups;
const walls = engine.walls;

const worldGenerator = require('../util/worldGenerator');

const getZonesForObject = require('../util/util').getZonesForObject

let loopAlreadyRunning = false;

server.listen(8080, () =>
    console.log("Server is running.."));

worldGenerator.generateWalls().forEach(wall => engine.addWall(wall.x, wall.y));

function wallData() {
    const wallData = [];
    walls.forEach(wall => {
        wallData.push({
            x: wall.bounds.bounds.min.x,
            y: wall.bounds.bounds.min.y
        })
    });
    return wallData;
}

io.on('connection', (socket) => {
    console.log("Player connected");

    socket.emit('socketID', {id: socket.id});
    socket.emit("wallData", wallData());

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
                agents[i].weapon = new Pistol();
                engine.moveAgentToRandomPlace(agents[i]);
                break;
            }
        }
    });

    socket.on('playerName', (data) => {
        let name = Object.values(data)[0];
        for (let i = 0; i < agents.length; i++) {
            if (agents[i].id === socket.id) {
                agents[i].name = name;
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
    });

    socket.on('disconnect', () => {
        console.log('Player disconnected, id:', socket.id);
        socket.broadcast.emit('playerDisconnected', {id: socket.id});
        engine.removeAgent(socket.id);
    });

    console.log("Adding new player, id " + socket.id);
    const ag = new Agent(200, 200, "", 0, 0, false, constants.PLAYER_MAX_HEALTH, new Pistol(), 0, socket.id);
    engine.addAgent(ag, 500, 500);
    //engine.moveAgentToRandomPlace(ag);

    if (!loopAlreadyRunning) {
        loopAlreadyRunning = true;
        engine.addPickup(new MachineGunPickup(0, 0, shortid.generate()), 300, 300);
        engine.addPickup(new MachineGunPickup(0, 0, shortid.generate()), 400, 400);
        engine.lastLoop = new Date().getTime();
        engine.physicLoop(projectile => {

                zones = getZonesForObject(projectile.bounds);

                for (agent of agents) {

                    if (projectile.bounds.position.x > agent.bounds.position.x - constants.WINDOW_WIDTH &&
                        projectile.bounds.position.x < agent.bounds.position.x + constants.WINDOW_WIDTH &&
                        projectile.bounds.position.y > agent.bounds.position.y - constants.WINDOW_HEIGHT &&
                        projectile.bounds.position.y < agent.bounds.position.y + constants.WINDOW_HEIGHT) {
                        io.to(agent.id).emit("newProjectile", {
                            x: projectile.bounds.position.x,
                            y: projectile.bounds.position.y,
                            id: projectile.id,
                            xSpeed: projectile.velocity.x,
                            ySpeed: projectile.velocity.y,
                            type: projectile.type
                        });
                    }
                }
            },
            explosion => {
                console.log("Broadcasting new explosion")
                for (agent of agents) {
                    if (explosion.x > agent.bounds.position.x - constants.WINDOW_WIDTH &&
                        explosion.x < agent.bounds.position.x + constants.WINDOW_WIDTH &&
                        explosion.y > agent.bounds.position.y - constants.WINDOW_HEIGHT &&
                        explosion.y < agent.bounds.position.y + constants.WINDOW_HEIGHT) {
                        io.to(agent.id).emit("newExplosion", {
                            x: explosion.x,
                            y: explosion.y,
                        });
                    }
                }
            });
        agentDataLoop();
        projectileDataLoop();
        pickupDataLoop()
    }
});

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

async function projectileDataLoop() {
    while (true) {
        for (agent of agents) {
            const projectileData = [];

            ids = [];
            for (zone of agent.viewportZones) {
                if (engine.matrix.projectiles[zone] != null)
                    for (projectile of engine.matrix.projectiles[zone]) {
                        if (ids.includes(projectile.id)) continue;
                        ids.push(projectile.id);
                        projectileData.push({
                            x: projectile.bounds.position.x,
                            y: projectile.bounds.position.y,
                            id: projectile.id,
                            xSpeed: projectile.velocity.x,
                            ySpeed: projectile.velocity.y,
                            type: projectile.type
                        })
                    }
            }
            io.to(agent.id).emit("projectileData", {projectileData});
        }
        await sleep(1000 / constants.PROJECTILE_UPDATES_PER_SECOND)
    }
}

async function pickupDataLoop() {
    while (true) {
        for (agent of agents) {
            const pickupData = [];
            ids = [];
            for (zone of agent.viewportZones) {
                if (engine.matrix.pickups[zone] != null)
                    for (pickup of engine.matrix.pickups[zone]) {
                        if (ids.includes(pickup.id)) continue;
                        ids.push(pickup.id);
                        pickupData.push({
                            x: pickup.bounds.bounds.min.x,
                            y: pickup.bounds.bounds.min.y,
                            type: pickup.type,
                            id: pickup.id,
                        })
                    }
            }
            io.to(agent.id).emit("pickupData", {pickupData});
        }
        await sleep(1000 / constants.PICKUP_UPDATES_PER_SECOND)
    }
}

async function agentDataLoop() {
    while (true) {
        for (agent of agents) {
            minX = agent.bounds.position.x - constants.WINDOW_WIDTH;
            minY = agent.bounds.position.y - constants.WINDOW_HEIGHT;
            maxX = agent.bounds.position.x + constants.WINDOW_WIDTH;
            maxY = agent.bounds.position.y + constants.WINDOW_HEIGHT;

            if (minX < 0) {
                maxX -= minX;
                minX = 0
            } else if (maxX > constants.MAP_WIDTH) {
                minX -= maxX;
                maxX = constants.MAP_WIDTH
            }

            if (minY < 0) {
                maxY -= minY;
                minY = 0
            } else if (maxY > constants.MAP_HEIGHT) {
                minY -= maxY;
                maxY = constants.MAP_HEIGHT
            }

            agent.viewportZones = getZonesForObject({
                bounds: {
                    min: {x: minX, y: minY},
                    max: {x: maxX, y: maxY}
                }
            });

            const agentData = [];

            ids = [];
            for (zone of agent.viewportZones) {
                if (engine.matrix.agents[zone] != null)
                    for (ag of engine.matrix.agents[zone]) {
                        if (ids.includes(ag.id)) continue;
                        ids.push(ag.id);
                        agentData.push({
                            x: ag.bounds.bounds.min.x,
                            y: ag.bounds.bounds.min.y,
                            name: ag.name,
                            xVelocity: ag.velocity.x,
                            yVelocity: ag.velocity.y,
                            bulletsLeft: ag.reloadMark === -1 ? ag.weapon.bulletsInChamber : -1,
                            isDead: ag.isDead,
                            currentHealth: ag.currentHealth,
                            id: ag.id,
                            weapon: ag.weapon.projectileType,
                            angle: ag.facingDirectionAngle,
                            kills: ag.kills,
                            deaths: ag.deaths

                        })
                    }
            }

            io.to(agent.id).emit("agentData", {agentData});
        }
        await sleep(1000 / constants.AGENT_UPDATES_PER_SECOND)
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

