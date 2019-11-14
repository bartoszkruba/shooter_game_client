const constants = require('../settings/constants');

const floor = Math.floor;

getZonesForCoordinates = (min, max) => {

    zones = [];

    for (let i = floor(min.x / constants.ZONE_SIZE); i <= floor(max.x / constants.ZONE_SIZE); i++) {
        for (let j = floor(min.y / constants.ZONE_SIZE); j <= floor(max.y / constants.ZONE_SIZE); j++) {
            zones.push("_" + i + "_" + j)
        }
    }

    return zones
};

getZonesMatrix = () => {
    matrix = {
        agents: {},
        projectiles: {},
        walls: {},
        pickups: {}
    };

    for (let i = 0; i <= floor(constants.MAP_WIDTH / constants.ZONE_SIZE); i++) {
        for (let j = 0; j <= floor(constants.MAP_HEIGHT / constants.ZONE_SIZE); j++) {
            matrix.agents["_" + i + "_" + j] = [];
            matrix.projectiles["_" + i + "_" + j] = [];
            matrix.walls["_" + i + "_" + j] = [];
            matrix.pickups["_" + i + "_" + j] = [];
        }
    }

    return matrix
};

getZonesForObject = object => {
    return getZonesForCoordinates(object.bounds.min, object.bounds.max)
};

function getRandomArbitrary(min, max) {
    return Math.random() * (max - min) + min;
}

module.exports = {getZonesForObject, getZonesMatrix, getRandomArbitrary};