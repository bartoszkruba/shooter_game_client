const Matter = require('matter-js');
const Agent = require('./models/Agent');
const PistolProjectile = require('./models/PistolProjectile');

const agents = [];
const projectiles = [];

const sleep = ms => new Promise((resolve => setTimeout(resolve, ms)));

let lastLoop;

async function physicLoop() {
    while (true) {
        const currentTime = new Date().getTime();
        delta = (currentTime - lastLoop) / 1000;
        lastLoop = currentTime;



        await sleep(1000 / 60)
    }
}

lastLoop = new Date().getTime();

physicLoop();

