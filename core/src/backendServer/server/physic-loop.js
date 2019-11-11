const Matter = require('matter-js');
const shortid = require('shortid');

const Agent = require('../models/Agent');
const Pistol = require('../models/Pistol');
const MachineGun = require('../models/MachineGun');
const PistolProjectile = require('../models/PistolProjectile');
const MachineGunProjectile = require('../models/MachineGunProjectile');
const PistolPickup = require('../models/PistolPickup');
const MachineGunPickup = require('../models/MachineGunPickup');
const ProjectileType = require('../models/ProjectileType');
const constants = require('../settings/constants');

const agents = [];
const projectiles = [];
const pickups = [];

const {getZonesForObject, getZonesMatrix} = require('../util/util');

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

let lastLoop;

const matrix = getZonesMatrix();

async function physicLoop(broadcastNewProjectile) {
    while (true) {
        const currentTime = new Date().getTime();
        delta = (currentTime - lastLoop) / 1000;
        lastLoop = currentTime;

        for (agent of agents) {
            checkControls(agent, delta, broadcastNewProjectile)
        }
        calculateProjectilePositions(delta);

        await sleep(1000 / 60)
    }
}

function calculateProjectilePositions(delta) {
    for (projectile of projectiles) {
        const x = projectile.bounds.position.x + projectile.velocity.x * delta * projectile.speed;
        const y = projectile.bounds.position.y + projectile.velocity.y * delta * projectile.speed;

        moveProjectile(projectile, x, y);

        if (projectile.bounds.position.x < 0 || projectile.bounds.position.x > constants.MAP_WIDTH ||
            projectile.bounds.position.y < 0 || projectile.bounds.position.y > constants.MAP_HEIGHT) {
            removeProjectile(projectile.id);
            break
        }

        let removed = false;
        for (zone of projectile.zones) {
            if (matrix.agents[zone] != null)
                for (agent of matrix.agents[zone]) {
                    if (Matter.SAT.collides(agent.bounds, projectile.bounds).collided && !agent.isDead) {
                        agent.takeDamage();
                        removeProjectile(projectile.id);
                        removed = true;
                        break;
                    }
                }
            if (removed) break
        }

        // for (let i = 0; i < agents.length; i++) {
        //     const agent = agents[i];
        //     if (Matter.SAT.collides(agent.bounds, projectile.bounds).collided && !agent.isDead) {
        //         agent.takeDamage();
        //         projectiles.splice(projectiles.indexOf(projectile), 1);
        //         break;
        //     }
        // }
    }
}

function moveProjectile(projectile, x, y) {
    Matter.Body.setPosition(projectile.bounds, {x, y});

    if (x < 0 || x > constants.MAP_WIDTH || y < 0 || y > constants.MAP_HEIGHT) return;

    oldZones = projectile.zones;

    projectile.zones = getZonesForObject(projectile.bounds);

    oldZones.filter(zone => !projectile.zones.includes(zone)).forEach(zone => {
        if (matrix.projectiles[zone] != null)
            matrix.projectiles[zone].splice(matrix.projectiles[zone].indexOf(projectile), 1)
    });
    projectile.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
        if (matrix.projectiles[zone] != null) matrix.projectiles[zone].push(projectile)
    });
}

function removeProjectile(id) {
    for (let i = 0; i < projectiles.length; i++) {
        if (projectiles[i].id === id) {
            zones = projectiles[i].zones;
            zones.forEach(zone => {
                if (matrix.projectiles[zone] != null)
                    matrix.projectiles[zone].splice(matrix.projectiles[zone].indexOf(projectiles[i]), 1)
            });
            projectiles.splice(i, 1);
            break;
        }
    }
}

function moveAgent(agent, x, y) {
    x = Matter.Common.clamp(x, constants.WALL_SPRITE_WIDTH + constants.PLAYER_SPRITE_WIDTH / 2,
        constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - constants.PLAYER_SPRITE_WIDTH / 2);

    y = Matter.Common.clamp(y, constants.WALL_SPRITE_HEIGHT + constants.PLAYER_SPRITE_HEIGHT / 2,
        constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - constants.PLAYER_SPRITE_HEIGHT / 2);

    Matter.Body.setPosition(agent.bounds, {x, y});

    oldZones = agent.zones;

    agent.zones = getZonesForObject(agent.bounds);

    oldZones.filter(zone => !agent.zones.includes(zone)).forEach(zone => {
        matrix.agents[zone].splice(matrix.agents[zone].indexOf(agent), 1)
    });
    agent.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
        matrix.agents[zone].push(agent)
    });
}

function spawnPistolProjectile(x, y, xSpeed, ySpeed, broadcastNewProjectile) {
    const projectile = new PistolProjectile(x, y, xSpeed, ySpeed, shortid.generate());
    addProjectileToMatrix(projectile);
    broadcastNewProjectile(projectile)
}

function spawnMachineGunProjectile(x, y, xSpeed, ySpeed, broadcastNewProjectile) {
    const projectile = new MachineGunProjectile(x, y, xSpeed, ySpeed, shortid.generate());
    addProjectileToMatrix(projectile);
    broadcastNewProjectile(projectile)
}

function addProjectileToMatrix(projectile) {
    projectile.zones = getZonesForObject(projectile.bounds);
    projectiles.push(projectile);
    projectile.zones.forEach(zone => {
        matrix.projectiles[zone].push(projectile)
    })
}

function pickWeapon(agent) {
    for (pickup of pickups) {
        if (Matter.SAT.collides(agent.bounds, pickup.bounds).collided) {
            switch (agent.weapon.projectileType) {
                case ProjectileType.PISTOL:
                    pickups.push(new PistolPickup(pickup.bounds.position.x, pickup.bounds.position.y,
                        shortid.generate(), agent.weapon.bulletsInChamber));
                    break;
                case ProjectileType.MACHINE_GUN:
                    pickups.push(new MachineGunPickup(pickup.bounds.position.x, pickup.bounds.position.y,
                        shortid.generate(), agent.weapon.bulletsInChamber));
                    break;
            }

            switch (pickup.type) {
                case ProjectileType.PISTOL:
                    agent.weapon = new Pistol();
                    break;
                case ProjectileType.MACHINE_GUN:
                    agent.weapon = new MachineGun();
                    break;
            }

            agent.weapon.bulletsInChamber = pickup.ammunition;
            pickups.splice(pickups.indexOf(pickup), 1);
        }
    }
}

function checkControls(agent, delta, broadcastNewProjectile) {
    if (agent.isRPressed && agent.reloadMark === -1) {
        if (agent.weapon.bulletsInChamber !== agent.weapon.maxBulletsInChamber) {
            agent.reloadMark = new Date().getTime();
            agent.weapon.bulletsInChamber = 0
        }
    }

    if (agent.reloadMark !== -1 && new Date().getTime() - agent.reloadMark > agent.weapon.magazineRefillTime) {
        agent.weapon.reload();
        agent.reloadMark = -1;
    }

    if (agent.pickWeapon) {
        agent.pickWeapon = false;
        pickWeapon(agent);
        return
    }

    if (agent.isLMPressed && agent.canShoot() && !agent.isDead) {
        agent.shoot();
        const xCentre = agent.bounds.position.x;
        const yCentre = agent.bounds.position.y;

        const edgePoint = projectToRectEdge(agent.facingDirectionAngle, agent);

        edgePoint.x += xCentre - constants.PLAYER_SPRITE_WIDTH / 2;
        edgePoint.y += yCentre - constants.PLAYER_SPRITE_HEIGHT / 2;

        xSpeed = Math.cos(Math.PI / 180 * agent.facingDirectionAngle);
        ySpeed = Math.sin(Math.PI / 180 * agent.facingDirectionAngle);

        switch (agent.weapon.projectileType) {
            case ProjectileType.PISTOL:
                spawnPistolProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile);
                break;
            case ProjectileType.MACHINE_GUN:
                spawnMachineGunProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile);
                break;
        }
    }

    let movementSpeed = constants.PLAYER_MOVEMENT_SPEED;
    let pressedKeys = 0;

    if (!agent.isDead) {
        if (agent.isWPressed) pressedKeys++;
        if (agent.isAPressed) pressedKeys++;
        if (agent.isSPressed) pressedKeys++;
        if (agent.isDPressed) pressedKeys++;

        if (pressedKeys > 1) movementSpeed *= 0.7;

        if (agent.isWPressed) moveAgent(agent, agent.bounds.position.x, agent.bounds.position.y + movementSpeed * delta);

        if (agent.isSPressed) moveAgent(agent, agent.bounds.position.x, agent.bounds.position.y - movementSpeed * delta);

        if (agent.isAPressed) moveAgent(agent, agent.bounds.position.x - movementSpeed * delta, agent.bounds.position.y);

        if (agent.isDPressed) moveAgent(agent, agent.bounds.position.x + movementSpeed * delta, agent.bounds.position.y)
    }
}

function projectToRectEdge(angle, agent) {
    const twoPI = Math.PI * 2;
    let theta = angle * Math.PI / 180;

    while (theta < -Math.PI) theta += twoPI;
    while (theta > Math.PI) theta -= twoPI;

    const rectAtan = Math.atan2(constants.PLAYER_SPRITE_HEIGHT, constants.PLAYER_SPRITE_WIDTH);
    const tanTheta = Math.tan(theta);

    let region;

    if ((theta > -rectAtan) && (theta <= rectAtan)) region = 1;
    else if ((theta > rectAtan) && (theta <= (Math.PI - rectAtan))) region = 2;
    else if ((theta > (Math.PI - rectAtan)) || (theta <= -(Math.PI - rectAtan))) region = 3;
    else region = 4;

    const edgePoint = {
        x: constants.PLAYER_SPRITE_WIDTH / 2,
        y: constants.PLAYER_SPRITE_HEIGHT / 2
    };

    let xFactor = 1;
    let yFactor = 1;

    if (region === 3 || region === 4) {
        xFactor = -1;
        yFactor = -1;
    }

    if (region === 1 || region === 3) {
        edgePoint.x += xFactor * (constants.PLAYER_SPRITE_WIDTH / 2);
        edgePoint.y += yFactor * (constants.PLAYER_SPRITE_WIDTH / 2) * tanTheta;
    } else {
        edgePoint.x += xFactor * (constants.PLAYER_SPRITE_HEIGHT / (2 * tanTheta));
        edgePoint.y += yFactor * (constants.PLAYER_SPRITE_HEIGHT / 2);
    }

    return edgePoint;
}

addAgent = (agent, x, y) => {
    agents.push(agent);
    agent.zones = getZonesForObject(agent.bounds);
    agent.zones.forEach(zone => {
        matrix.agents[zone].push(agent)
    });
    moveAgent(agent, x, y);
};

removeAgent = id => {
    for (let i = 0; i < agents.length; i++) {
        if (agents[i].id === id) {
            zones = agents[i].zones;
            zones.forEach(zone => matrix.agents[zone].splice(matrix.agents[zone].indexOf(agents[i]), 1));
            agents.splice(i, 1);
            break;
        }
    }
};

module.exports = {physicLoop, agents, projectiles, moveAgent, lastLoop, pickups, matrix, addAgent, removeAgent};

