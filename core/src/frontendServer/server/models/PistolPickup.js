const Pickup = require('./Pickup');
const constants = require('../settings/constants');
const ProjectileType = require('./ProjectileType');

class PistolPickup extends Pickup {
    constructor(x, y) {
        super(x, y, constants.PISTOL_SPRITE_WIDTH, constants.PISTOL_SPRITE_HEIGHT, ProjectileType.PISTOL)
    }
}

module.exports = PistolPickup;