const Pickup = require('./Pickup');
const constants = require('../settings/constants');
const ProjectileType = require('./ProjectileType');

class ShotgunPickup extends Pickup {
    constructor(x, y, id, ammunition = constants.SHOTGUN_BULLETS_IN_CHAMBER) {
        super(x, y, constants.SHOTGUN_SPRITE_WIDTH, constants.SHOTGUN_SPRITE_HEIGHT, ProjectileType.SHOTGUN);
    }
}

module.exports = ShotgunPickup