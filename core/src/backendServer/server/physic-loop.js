const Matter = require('matter-js');
const shortid = require('shortid');

const Agent = require('../models/Agent');
const Pistol = require('../models/Pistol');
const MachineGun = require('../models/MachineGun');
const Shotgun = require('../models/Shotgun');
const PistolProjectile = require('../models/PistolProjectile');
const MachineGunProjectile = require('../models/MachineGunProjectile');
const ShotgunProjectile = require('../models/ShotgunProjectile');
const PistolPickup = require('../models/PistolPickup');
const MachineGunPickup = require('../models/MachineGunPickup');
const ShotgunPickup = require('../models/ShotgunPickup');
const ProjectileType = require('../models/ProjectileType');
const constants = require('../settings/constants');
const Wall = require('../models/Wall');

const util = require('../util/util');

const agents = [];
const projectiles = [];
const pickups = [];
const walls = [];

const {getZonesForObject, getZonesMatrix} = require('../util/util');

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

let lastLoop;

const matrix = getZonesMatrix();

async function physicLoop(broadcastNewProjectile) {
    weaponRespawnLoop();
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

const weaponRespawnLoop = async () => {
    while (true) {
        console.log("Spawning weapons");
        clearAllWeaponPickups();

        for (let i = 0; i < constants.MACHINE_GUNS_ON_MAP; i++) {
            spawnMachineGunPickupAtRandomPlace();
        }
        for (let i = 0; i < constants.PISTOLS_ON_MAP; i++) {
            spawnPistolPickupAtRandomPlace()
        }
        for (let i = 0; i < constants.SHOTGUNS_ON_MAP; i++) {
            spawnShotgunPickupAtRandomPlace()
        }
        await sleep(constants.WEAPON_RESPAWN_RATE * 1000)
    }
};

const clearAllWeaponPickups = () => {
    pickups.forEach(pickup => {
        pickup.zones.forEach(zone => matrix.pickups[zone] = [])
    });
    pickups.splice(0, pickups.length)
};

const spawnShotgunPickupAtRandomPlace = () => {
    const minX = constants.WALL_SPRITE_WIDTH + 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const minY = constants.WALL_SPRITE_HEIGHT + 0.5 * constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - 0.5 * constants.PLAYER_SPRITE_HEIGHT;

    const weapon = new ShotgunPickup(50, 50, shortid.generate());

    weapon.zones = getZonesForObject(weapon.bounds);

    while (true) {
        collided = false;
        const x = util.getRandomArbitrary(minX, maxX);
        const y = util.getRandomArbitrary(minY, maxY);

        const oldZones = weapon.zones;
        Matter.Body.setPosition(weapon.bounds, {x, y});
        weapon.zones = getZonesForObject(weapon.bounds);

        weapon.zones.forEach(zone => {
            matrix.walls[zone].forEach(wall => {
                if (Matter.SAT.collides(wall.bounds, weapon.bounds).collided) collided = true
            })
        });

        oldZones.filter(zone => !weapon.zones.includes(zone)).forEach(zone => {
            matrix.pickups[zone].splice(matrix.pickups[zone].indexOf(weapon), 1)
        });
        weapon.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
            matrix.pickups[zone].push(weapon)
        });

        if (!collided) break
    }

    pickups.push(weapon)
};

const spawnMachineGunPickupAtRandomPlace = () => {
    const minX = constants.WALL_SPRITE_WIDTH + 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const minY = constants.WALL_SPRITE_HEIGHT + 0.5 * constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - 0.5 * constants.PLAYER_SPRITE_HEIGHT;

    const weapon = new MachineGunPickup(50, 50, shortid.generate());

    weapon.zones = getZonesForObject(weapon.bounds);

    while (true) {
        collided = false;
        const x = util.getRandomArbitrary(minX, maxX);
        const y = util.getRandomArbitrary(minY, maxY);

        const oldZones = weapon.zones;
        Matter.Body.setPosition(weapon.bounds, {x, y});
        weapon.zones = getZonesForObject(weapon.bounds);

        weapon.zones.forEach(zone => {
            matrix.walls[zone].forEach(wall => {
                if (Matter.SAT.collides(wall.bounds, weapon.bounds).collided) collided = true
            })
        });

        oldZones.filter(zone => !weapon.zones.includes(zone)).forEach(zone => {
            matrix.pickups[zone].splice(matrix.pickups[zone].indexOf(weapon), 1)
        });
        weapon.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
            matrix.pickups[zone].push(weapon)
        });

        if (!collided) break
    }

    pickups.push(weapon)
};

const spawnPistolPickupAtRandomPlace = () => {
    const minX = constants.WALL_SPRITE_WIDTH + 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const minY = constants.WALL_SPRITE_HEIGHT + 0.5 * constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - 0.5 * constants.PLAYER_SPRITE_HEIGHT;

    const weapon = new PistolPickup(50, 50, shortid.generate());

    weapon.zones = getZonesForObject(weapon.bounds);

    while (true) {
        collided = false;
        const x = util.getRandomArbitrary(minX, maxX);
        const y = util.getRandomArbitrary(minY, maxY);

        const oldZones = weapon.zones;
        Matter.Body.setPosition(weapon.bounds, {x, y});
        weapon.zones = getZonesForObject(weapon.bounds);

        weapon.zones.forEach(zone => {
            matrix.walls[zone].forEach(wall => {
                if (Matter.SAT.collides(wall.bounds, weapon.bounds).collided) collided = true
            })
        });

        oldZones.filter(zone => !weapon.zones.includes(zone)).forEach(zone => {
            matrix.pickups[zone].splice(matrix.pickups[zone].indexOf(weapon), 1)
        });
        weapon.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
            matrix.pickups[zone].push(weapon)
        });

        if (!collided) break
    }

    pickups.push(weapon)
};

function calculateProjectilePositions(delta) {
    for (projectile of projectiles) {
        const x = projectile.bounds.position.x + projectile.velocity.x * delta * projectile.speed;
        const y = projectile.bounds.position.y + projectile.velocity.y * delta * projectile.speed;

        moveProjectile(projectile, x, y);

        if (projectile.bounds.position.x < 0 || projectile.bounds.position.x > constants.MAP_WIDTH ||
            projectile.bounds.position.y < 0 || projectile.bounds.position.y > constants.MAP_HEIGHT) {
            removeProjectile(projectile.id);
            continue
        }

        let removed = false;
        for (zone of projectile.zones) {
            if (matrix.agents[zone] != null) for (agent of matrix.agents[zone]) {
                if (Matter.SAT.collides(agent.bounds, projectile.bounds).collided && !agent.isDead) {
                    agent.takeDamage();
                    removeProjectile(projectile.id);
                    removed = true;
                    break;
                }
            }
            if (matrix.walls[zone] != null) for (wall of matrix.walls[zone]) {
                if (Matter.SAT.collides(wall.bounds, projectile.bounds).collided) {
                    removeProjectile(projectile.id);
                    removed = true;
                    break;
                }
            }

            if (removed) break
        }
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

function moveAgent(agent, x, y, oldX, oldY) {
    x = Matter.Common.clamp(x, constants.WALL_SPRITE_WIDTH + constants.PLAYER_SPRITE_WIDTH / 2,
        constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - constants.PLAYER_SPRITE_WIDTH / 2);

    y = Matter.Common.clamp(y, constants.WALL_SPRITE_HEIGHT + constants.PLAYER_SPRITE_HEIGHT / 2,
        constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - constants.PLAYER_SPRITE_HEIGHT / 2);

    Matter.Body.setPosition(agent.bounds, {x, y});

    oldZones = agent.zones;

    agent.zones = getZonesForObject(agent.bounds);

    let collided = false;

    agent.zones.forEach(zone => {
        if (matrix.walls[zone] != null) matrix.walls[zone].forEach(wall => {
            if (Matter.SAT.collides(wall.bounds, agent.bounds).collided) {
                Matter.Body.setPosition(agent.bounds, {x: oldX, y: oldY});
                collided = true
            }
        })
    });

    if (collided) return;

    oldZones.filter(zone => !agent.zones.includes(zone)).forEach(zone => {
        matrix.agents[zone].splice(matrix.agents[zone].indexOf(agent), 1)
    });
    agent.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
        matrix.agents[zone].push(agent)
    });
}

function movePickup(pickup, x, y) {
    Matter.Body.setPosition(pickup.bounds, {x, y});

    oldZones = pickup.zones;
    pickup.zones = getZonesForObject(pickup.bounds);

    oldZones.filter(zone => !pickup.zones.includes(zone)).forEach(zone => {
        matrix.pickups[zone].splice(matrix.pickups[zone].indexOf(pickup), 1)
    });
    pickup.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
        matrix.pickups[zone].push(pickup)
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

function spawnShotgunProjectiles(x, y, broadcastNewProjectile) {
    for (let i = 0; i < 15; i++) {
        const angle = agent.facingDirectionAngle + util.getRandomArbitrary(-10, 10);
        const xSpeed = Math.cos(Math.PI / 180 * angle);
        const ySpeed = Math.sin(Math.PI / 180 * angle);
        const projectile = new ShotgunProjectile(x,y, xSpeed, ySpeed, shortid.generate());
        broadcastNewProjectile(projectile)
    }
}

function addProjectileToMatrix(projectile) {
    projectile.zones = getZonesForObject(projectile.bounds);
    projectiles.push(projectile);
    projectile.zones.forEach(zone => {
        matrix.projectiles[zone].push(projectile)
    })
}

function pickWeapon(agent) {
    let shouldBreak = false;
    for (zone of agent.zones) {
        if (shouldBreak) break;
        for (pickup of matrix.pickups[zone]) {
            if (Matter.SAT.collides(agent.bounds, pickup.bounds).collided) {
                switch (agent.weapon.projectileType) {
                    case ProjectileType.PISTOL:
                        addPickup(new PistolPickup(0, 0, shortid.generate(),
                            agent.weapon.bulletsInChamber), pickup.bounds.position.x, pickup.bounds.position.y);
                        break;
                    case ProjectileType.MACHINE_GUN:
                        addPickup(new MachineGunPickup(0, 0, shortid.generate(),
                            agent.weapon.bulletsInChamber), pickup.bounds.position.x, pickup.bounds.position.y);
                        break;
                    case ProjectileType.SHOTGUN:
                        addPickup(new ShotgunPickup(0, 0, shortid.generate(),
                            agent.weapon.bulletsInChamber), pickup.bounds.position.x, pickup.bounds.position.y);
                        break;
                }

                switch (pickup.type) {
                    case ProjectileType.PISTOL:
                        agent.weapon = new Pistol();
                        break;
                    case ProjectileType.MACHINE_GUN:
                        agent.weapon = new MachineGun();
                        break;
                    case ProjectileType.SHOTGUN:
                        agent.weapon = new Shotgun();
                        break;
                }

                agent.weapon.bulletsInChamber = pickup.ammunition;
                removePickup(pickup.id);
                shouldBreak = true;
                break;
            }
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

        switch (agent.weapon.projectileType) {
            case ProjectileType.PISTOL:
                xSpeed = Math.cos(Math.PI / 180 * agent.facingDirectionAngle);
                ySpeed = Math.sin(Math.PI / 180 * agent.facingDirectionAngle);
                spawnPistolProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile);
                break;
            case ProjectileType.MACHINE_GUN:
                xSpeed = Math.cos(Math.PI / 180 * agent.facingDirectionAngle);
                ySpeed = Math.sin(Math.PI / 180 * agent.facingDirectionAngle);
                spawnMachineGunProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile);
                break;
            case ProjectileType.SHOTGUN:
                spawnShotgunProjectiles(edgePoint.x, edgePoint.y, broadcastNewProjectile);
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

        if (agent.isWPressed) moveAgent(
            agent, agent.bounds.position.x, agent.bounds.position.y + movementSpeed * delta,
            agent.bounds.position.x, agent.bounds.position.y);

        if (agent.isSPressed) moveAgent(
            agent, agent.bounds.position.x, agent.bounds.position.y - movementSpeed * delta,
            agent.bounds.position.x, agent.bounds.position.y);

        if (agent.isAPressed) moveAgent(
            agent, agent.bounds.position.x - movementSpeed * delta, agent.bounds.position.y,
            agent.bounds.position.x, agent.bounds.position.y);

        if (agent.isDPressed) moveAgent(
            agent, agent.bounds.position.x + movementSpeed * delta, agent.bounds.position.y,
            agent.bounds.position.x, agent.bounds.position.y)
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

const addAgent = (agent, x, y) => {
    agents.push(agent);
    agent.zones = getZonesForObject(agent.bounds);
    agent.zones.forEach(zone => {
        matrix.agents[zone].push(agent)
    });
    moveAgent(agent, x, y, x, y);
};

const moveAgentToRandomPlace = (agent) => {
    const minX = constants.WALL_SPRITE_WIDTH + 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const minY = constants.WALL_SPRITE_HEIGHT + 0.5 * constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - 0.5 * constants.PLAYER_SPRITE_HEIGHT;

    agent.zones = getZonesForObject(agent.bounds);

    while (true) {
        collided = false;
        const x = util.getRandomArbitrary(minX, maxX);
        const y = util.getRandomArbitrary(minY, maxY);

        const oldZones = agent.zones;
        Matter.Body.setPosition(agent.bounds, {x, y});
        agent.zones = getZonesForObject(agent.bounds);

        agent.zones.forEach(zone => {
            matrix.walls[zone].forEach(wall => {
                if (Matter.SAT.collides(wall.bounds, agent.bounds).collided) collided = true
            })
        });

        oldZones.filter(zone => !agent.zones.includes(zone)).forEach(zone => {
            matrix.agents[zone].splice(matrix.agents[zone].indexOf(agent), 1)
        });
        agent.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
            matrix.agents[zone].push(agent)
        });

        if (!collided) break
    }

};

const removeAgent = id => {
    for (let i = 0; i < agents.length; i++) {
        if (agents[i].id === id) {
            zones = agents[i].zones;
            zones.forEach(zone => matrix.agents[zone].splice(matrix.agents[zone].indexOf(agents[i]), 1));
            agents.splice(i, 1);
            break;
        }
    }
};

const removePickup = id => {
    for (let i = 0; i < pickups.length; i++) {
        if (pickups[i].id === id) {
            zones = pickups[i].zones;
            zones.forEach(zone => matrix.pickups[zone].splice(matrix.pickups[zone].indexOf(pickups[i]), 1));
            pickups.splice(i, 1);
            break;
        }
    }
};

const addPickup = (pickup, x, y) => {
    pickups.push(pickup);
    movePickup(pickup, x, y)
};

const addWall = (x, y) => {
    const wall = new Wall(x, y, shortid.generate());
    wall.zones = getZonesForObject(wall.bounds);
    walls.push(wall);
    for (zone of wall.zones) {
        matrix.walls[zone].push(wall)
    }
};

module.exports = {
    physicLoop,
    agents,
    projectiles,
    moveAgent,
    lastLoop,
    pickups,
    matrix,
    addAgent,
    removeAgent,
    addPickup,
    addWall,
    walls,
    moveAgentToRandomPlace
};

