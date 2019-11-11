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

getZonesForObject = object => {
    return getZonesForCoordinates(object.bounds.min, object.bounds.max)
};

module.exports = {getZonesForObject};