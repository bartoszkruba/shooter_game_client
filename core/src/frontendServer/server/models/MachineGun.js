const constants = require('../settings/constants');
const Weapon = require('./Weapon');
const ProjectileType = require('./ProjectileType');

class MachineGun extends Weapon {
    constructor() {
        super(constants.MACHINE_GUN_RELOAD_TIME, constants.MACHINE_GUN_BULLETS_IN_CHAMBER,
            constants.MACHINE_GUN_MAGAZINE_RELOAD_TIME, ProjectileType.MACHINE_GUN)
    }
}

module.exports = MachineGun;