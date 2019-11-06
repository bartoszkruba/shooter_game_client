const Pickup = require('./Pickup');
const constants = require('../settings/constants');
const ProjectileType = require('./ProjectileType');

class MachineGunPickup extends Pickup {
    constructor(x, y) {
        super(x, y, constants.MACHINE_GUN_SPRITE_WIDTH, constants.MACHINE_GUN_SPRITE_HEIGHT, ProjectileType.MACHINE_GUN)
    }
}

module.exports = MachineGunPickup;