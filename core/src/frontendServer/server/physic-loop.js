const Matter = require('matter-js');
const shortid = require('shortid');

const Agent = require('./models/Agent');
const Pistol = require('./models/Pistol');
const PistolProjectile = require('./models/PistolProjectile');
const constants = require('./settings/constants');

const agents = [];
const projectiles = [];

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

let lastLoop;

async function physicLoop() {
    while (true) {
        const currentTime = new Date().getTime();
        delta = (currentTime - lastLoop) / 1000;
        lastLoop = currentTime;

        calculateProjectilePositions(delta);

        await sleep(1000 / 60)
    }
}

function calculateProjectilePositions(delta) {
    for (projectile of projectiles) {
        projectile.bounds.position.x = projectile.bounds.position.x + projectile.velocity * delta * projectile.speed;
        projectile.bounds.position.y = projectile.bounds.position.y + projectile.velocity * delta * projectile.speed;

        if (projectile.bounds.position.x < 0 || projectile.bounds.position.x > constants.MAP_WIDTH ||
            projectile.bounds.position.y < 0 || projectile.bounds.position.y > constants.MAP_HEIGHT) {
            projectiles.splice(projectiles.indexOf(projectile), 1);
        }

        for (agent of agents) {
            if (Matter.SAT.collides(agent.bounds, projectile.bounds).collided) {
                projectiles.splice(projectiles.indexOf(projectile), 1);
            }
        }
    }
}

function moveAgent(agent, x, y) {
    agent.bounds.position.x = Matter.clamp(x, constants.WALL_SPRITE_WIDTH,
        constants.MAP_WIDTH - constants.PLAYER_SPRITE_WIDTH - constants.WALL_SPRITE_WIDTH);
    agent.bounds.position.y = Matter.clamp(y, constants.WALL_SPRITE_HEIGHT,
        constants.MAP_HEIGHT - constants.WALL_SPRITE_HEIGHT - constants.PLAYER_SPRITE_HEIGHT);
}

function spawnPistolProjectile(x, y, xSpeed, ySpeed) {
    projectiles.push(new PistolProjectile(x, y, xSpeed, ySpeed, shortid.generate()));
}

function checkControls() {

}

projectiles.push(new PistolProjectile(0, 0, 0, 0, shortid.generate()));
projectiles.push(new PistolProjectile(0, 0, 0, 0, shortid.generate()));
projectiles.push(new PistolProjectile(0, 0, 0, 0, shortid.generate()));
projectiles.push(new PistolProjectile(0, 0, 0, 0, shortid.generate()));

agents.push(new Agent(100, 100, new Pistol(), 45, shortid.generate()));

lastLoop = new Date().getTime();

physicLoop();

