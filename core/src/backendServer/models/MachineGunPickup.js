const Pickup = require('./Pickup');
const constants = require('../settings/constants');
const ProjectileType = require('./ProjectileType');

class MachineGunPickup extends Pickup {
    constructor(x, y, id, ammunition = constants.MACHINE_GUN_BULLETS_IN_CHAMBER) {
        super(x, y, constants.MACHINE_GUN_SPRITE_WIDTH, constants.MACHINE_GUN_SPRITE_HEIGHT, ProjectileType.MACHINE_GUN,
            id, ammunition)
    }
}

module.exports = MachineGunPickup;