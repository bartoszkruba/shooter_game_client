const Pickup = require('./Pickup');
const constants = require('../../settings/constants');
const ProjectileType = require('../projectiles/ProjectileType');

class PistolPickup extends Pickup {
    constructor(x, y, id, ammunition = constants.PISTOL_BULLETS_IN_CHAMBER) {
        super(x, y, constants.PISTOL_SPRITE_WIDTH, constants.PISTOL_SPRITE_HEIGHT, ProjectileType.PISTOL, id,
            ammunition)
    }
}

module.exports = PistolPickup;