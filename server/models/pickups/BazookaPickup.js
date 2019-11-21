const Pickup = require('./Pickup');
const constants = require('../../settings/constants');
const ProjectileType = require('../projectiles/ProjectileType');

class BazookaPickup extends Pickup {
    constructor(x, y, id, ammunition = constants.BAZOOKA_BULLETS_IN_CHAMBER) {
        super(x, y, constants.BAZOOKA_SPRITE_WIDTH, constants.BAZOOKA_SPRTE_HEIGHT, ProjectileType.BAZOOKA,
            id, ammunition)
    }
}

module.exports = BazookaPickup;