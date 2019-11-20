const Matter = require('matter-js');
const shortid = require('shortid');

const Pistol = require('../models/weapons/Pistol');
const MachineGun = require('../models/weapons/MachineGun');
const Shotgun = require('../models/weapons/Shotgun');
const Bazooka = require('../models/weapons/Bazooka');

const PistolProjectile = require('../models/projectiles/PistolProjectile');
const MachineGunProjectile = require('../models/projectiles/MachineGunProjectile');
const ShotgunProjectile = require('../models/projectiles/ShotgunProjectile');
const BazookaProjectile = require('../models/projectiles/BazookaProjectile');

const PistolPickup = require('../models/pickups/PistolPickup');
const MachineGunPickup = require('../models/pickups/MachineGunPickup');
const ShotgunPickup = require('../models/pickups/ShotgunPickup');
const BazookaPickup = require("../models/pickups/BazookaPickup");
const ProjectileType = require('../models/projectiles/ProjectileType');
const constants = require('../settings/constants');
const Wall = require('../models/obstacles/Wall');

const util = require('../util/util');

const agents = [];
const projectiles = [];
const pickups = [];
const walls = [];

const {getZonesForObject, getZonesMatrix} = require('../util/util');

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

let lastLoop;

let continueLooping = true;

const matrix = getZonesMatrix();

const agentsToRemove = [];

async function physicLoop(broadcastNewProjectile, broadcastNewExplosion, broadcastKillConfirm, broadcastDisconnect) {
    weaponRespawnLoop().catch(e => console.log(e));
    while (continueLooping) {
        for (let id of agentsToRemove) {
            removeAgent(id);
            console.log("removing " + id)
            broadcastDisconnect(id);
        }
        agentsToRemove.splice(0, agentsToRemove.length);
        const currentTime = new Date().getTime();
        let delta = (currentTime - lastLoop) / 1000;
        lastLoop = currentTime;

        for (let agent of agents) {
            if (agent.invisible && agent.lastRespawn < new Date().getTime() - constants.INVISIBILITY_DURATION * 1000)
                agent.invisible = false;
            checkControls(agent, delta, broadcastNewProjectile)
        }
        calculateProjectilePositions(delta, broadcastNewExplosion, broadcastKillConfirm);

        await sleep(1000 / 60)
    }
}

const weaponRespawnLoop = async () => {
    while (continueLooping) {
        console.log("Spawning weapons");
        clearAllWeaponPickups();

        for (let i = 0; i < constants.MACHINE_GUNS_ON_MAP; i++) spawnMachineGunPickupAtRandomPlace();
        for (let i = 0; i < constants.PISTOLS_ON_MAP; i++) spawnPistolPickupAtRandomPlace();
        for (let i = 0; i < constants.SHOTGUNS_ON_MAP; i++) spawnShotgunPickupAtRandomPlace();
        for (let i = 0; i < constants.BAZOOKAS_ON_MAP; i++) spawnBazookaAtRandomPlace();

        await sleep(constants.WEAPON_RESPAWN_RATE * 1000)
    }
};

const clearAllWeaponPickups = () => {
    pickups.forEach(pickup => {
        pickup.zones.forEach(zone => matrix.pickups[zone] = [])
    });
    pickups.splice(0, pickups.length)
};

const spawnBazookaAtRandomPlace = () => {
    const minX = constants.WALL_SPRITE_WIDTH + 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const minY = constants.WALL_SPRITE_HEIGHT + 0.5 * constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - 0.5 * constants.PLAYER_SPRITE_HEIGHT;

    const weapon = new BazookaPickup(50, 50, shortid.generate());

    weapon.zones = getZonesForObject(weapon.bounds);

    while (true) {
        let collided = false;
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

const spawnShotgunPickupAtRandomPlace = () => {
    const minX = constants.WALL_SPRITE_WIDTH + 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH - 0.5 * constants.PLAYER_SPRITE_WIDTH;
    const minY = constants.WALL_SPRITE_HEIGHT + 0.5 * constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - 0.5 * constants.PLAYER_SPRITE_HEIGHT;

    const weapon = new ShotgunPickup(50, 50, shortid.generate());

    weapon.zones = getZonesForObject(weapon.bounds);

    while (true) {
        let collided = false;
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
        let collided = false;
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
        let collided = false;
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

function calculateProjectilePositions(delta, broadcastNewExplosion, broadcastKillConfirm) {
    for (let projectile of projectiles) {
        const x = projectile.bounds.position.x + projectile.velocity.x * delta * projectile.speed;
        const y = projectile.bounds.position.y + projectile.velocity.y * delta * projectile.speed;

        moveProjectile(projectile, x, y);

        if (projectile.bounds.position.x < constants.WALL_SPRITE_WIDTH ||
            projectile.bounds.position.x > constants.MAP_WIDTH - constants.WALL_SPRITE_WIDTH ||
            projectile.bounds.position.y < constants.WALL_SPRITE_HEIGHT ||
            projectile.bounds.position.y > constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT) {
            removeProjectile(projectile.id);
            if (projectile.type === ProjectileType.BAZOOKA)
                spawnBazookaExplosion(projectile.bounds.position.x, projectile.bounds.position.y,
                    broadcastNewExplosion, broadcastKillConfirm, projectile.agentId);
            continue
        }

        let removed = false;
        for (let zone of projectile.zones) {
            if (matrix.agents[zone] != null) for (let agent of matrix.agents[zone]) {
                if (!agent.isDead && !agent.invisible && projectile.agentId !== agent.id &&
                    Matter.SAT.collides(agent.bounds, projectile.bounds).collided) {

                    agent.takeDamage(projectile.damage);
                    if (agent.isDead) {
                        broadcastKillConfirm(projectile.agentId);
                        agent.deaths++;
                        for (let i = 0; i < agents.length; i++) {
                            if (agents[i].id === projectile.agentId) {
                                agents[i].kills++
                            }
                        }
                    }
                    if (projectile.type === ProjectileType.BAZOOKA)
                        spawnBazookaExplosion(projectile.bounds.position.x, projectile.bounds.position.y,
                            broadcastNewExplosion, broadcastKillConfirm, projectile.agentId);

                    removeProjectile(projectile.id);
                    removed = true;
                    break;
                }
            }
            if (matrix.walls[zone] != null) for (let wall of matrix.walls[zone]) {
                if (Matter.SAT.collides(wall.bounds, projectile.bounds).collided) {

                    if (projectile.type === ProjectileType.BAZOOKA)
                        spawnBazookaExplosion(projectile.bounds.position.x, projectile.bounds.position.y,
                            broadcastNewExplosion, broadcastKillConfirm, projectile.agentId);

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

    let oldZones = projectile.zones;

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
            let zones = projectiles[i].zones;
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

    let oldZones = agent.zones;

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

    let oldZones = pickup.zones;
    pickup.zones = getZonesForObject(pickup.bounds);

    oldZones.filter(zone => !pickup.zones.includes(zone)).forEach(zone => {
        matrix.pickups[zone].splice(matrix.pickups[zone].indexOf(pickup), 1)
    });
    pickup.zones.filter(zone => !oldZones.includes(zone)).forEach(zone => {
        matrix.pickups[zone].push(pickup)
    });
}

function spawnBazookaExplosion(x, y, broadcastBazookaExplosion, broadcastKillConfirm, agentId) {
    const explosion = Matter.Bodies.circle(x, y, constants.BAZOOKA_EXPLOSION_SIZE / 2);
    const zones = getZonesForObject(explosion);
    for (let zone of zones) {
        if (matrix.agents[zone] != null)
            matrix.agents[zone].forEach(agent => {
                if (Matter.SAT.collides(agent.bounds, explosion).collided && !agent.invisible && !agent.isDead) {
                    agent.takeDamage(constants.BAZOOKA_EXPLOSION_DAMAGE);
                    if (agent.isDead) {
                        broadcastKillConfirm(agentId);
                        agent.deaths++;
                        for (let i = 0; i < agents.length; i++) {
                            if (agents[i].id === agentId) {
                                agents[i].kills++
                            }
                        }

                    }
                }
            });
    }
    broadcastBazookaExplosion({x, y})
}

function spawnPistolProjectile(x, y, xSpeed, ySpeed, broadcastNewProjectile, agentId) {
    const projectile = new PistolProjectile(x, y, xSpeed, ySpeed, shortid.generate(), agentId);
    addProjectileToMatrix(projectile);
    broadcastNewProjectile(projectile)
}

function spawnMachineGunProjectile(x, y, xSpeed, ySpeed, broadcastNewProjectile, agentId) {
    const projectile = new MachineGunProjectile(x, y, xSpeed, ySpeed, shortid.generate(), agentId);
    addProjectileToMatrix(projectile);
    broadcastNewProjectile(projectile)
}

function spawnBazookaProjectile(x, y, xSpeed, ySpeed, broadcastNewProjectile, agentId) {
    const projectile = new BazookaProjectile(x, y, xSpeed, ySpeed, shortid.generate(), agentId);
    addProjectileToMatrix(projectile);
    broadcastNewProjectile(projectile);
}

function spawnShotgunProjectiles(agent, x, y, broadcastNewProjectile, agentId) {
    for (let i = 0; i < 10; i++) {
        const angle = agent.facingDirectionAngle + util.getRandomArbitrary(-15, 15);
        const xSpeed = Math.cos(Math.PI / 180 * angle);
        const ySpeed = Math.sin(Math.PI / 180 * angle);
        const projectile = new ShotgunProjectile(x, y, xSpeed, ySpeed, shortid.generate(), agentId);
        addProjectileToMatrix(projectile);
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
    for (let zone of agent.zones) {
        if (shouldBreak) break;
        for (let pickup of matrix.pickups[zone]) {
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
                    case ProjectileType.BAZOOKA:
                        addPickup(new BazookaPickup(0, 0, shortid.generate(),
                            agent.weapon.bulletsInChamber), pickup.bounds.position.x, pickup.bounds.position.y)
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
                    case ProjectileType.BAZOOKA:
                        agent.weapon = new Bazooka();
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

        let xSpeed;
        let ySpeed;
        switch (agent.weapon.projectileType) {
            case ProjectileType.PISTOL:
                xSpeed = Math.cos(Math.PI / 180 * agent.facingDirectionAngle);
                ySpeed = Math.sin(Math.PI / 180 * agent.facingDirectionAngle);
                spawnPistolProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile, agent.id);
                break;
            case ProjectileType.MACHINE_GUN:
                xSpeed = Math.cos(Math.PI / 180 * agent.facingDirectionAngle);
                ySpeed = Math.sin(Math.PI / 180 * agent.facingDirectionAngle);
                spawnMachineGunProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile, agent.id);
                break;
            case ProjectileType.BAZOOKA:
                xSpeed = Math.cos(Math.PI / 180 * agent.facingDirectionAngle);
                ySpeed = Math.sin(Math.PI / 180 * agent.facingDirectionAngle);
                spawnBazookaProjectile(edgePoint.x, edgePoint.y, xSpeed, ySpeed, broadcastNewProjectile, agent.id);
                break;
            case ProjectileType.SHOTGUN:
                spawnShotgunProjectiles(agent, edgePoint.x, edgePoint.y, broadcastNewProjectile, agent.id);
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

function projectToRectEdge(angle) {
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
        let collided = false;
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

    agent.invisible = true;
    agent.lastRespawn = new Date().getTime();
};

const removeAgent = id => {
    for (let i = 0; i < agents.length; i++) {
        if (agents[i].id === id) {
            let zones = agents[i].zones;
            zones.forEach(zone => matrix.agents[zone].splice(matrix.agents[zone].indexOf(agents[i]), 1));
            agents.splice(i, 1);
            break;
        }
    }

    for (let zone in matrix.agents) {
        for (let agent of zone) {
            if (agent.id === id) {
                zone.splice(zone.indexOf(agent, 1));
                break;
            }
        }
    }
};

const removePickup = id => {
    for (let i = 0; i < pickups.length; i++) {
        if (pickups[i].id === id) {
            let zones = pickups[i].zones;
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
    for (let zone of wall.zones) {
        matrix.walls[zone].push(wall)
    }
};

module.exports = {
    physicLoop,
    agents,
    projectiles,
    continueLooping,
    pickups,
    matrix,
    addAgent,
    removeAgent,
    addPickup,
    addWall,
    walls,
    moveAgentToRandomPlace,
    agentsToRemove,
};

