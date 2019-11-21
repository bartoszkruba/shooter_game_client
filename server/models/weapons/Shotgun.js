const constants = require('../../settings/constants');
const Weapon = require('./Weapon');
const ProjectileType = require('../projectiles/ProjectileType');

class Shotgun extends Weapon {
    constructor() {
        super(constants.SHOTGUN_RELOAD_TIME, constants.SHOTGUN_BULLETS_IN_CHAMBER,
            constants.SHOTGUN_MAGAZINE_RELOAD_TIME, ProjectileType.SHOTGUN)
    }
}

module.exports = Shotgun