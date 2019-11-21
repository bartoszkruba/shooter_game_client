const constants = require('../../settings/constants');
const Weapon = require('./Weapon');
const ProjectileType = require('../projectiles/ProjectileType');

class Pistol extends Weapon {
    constructor() {
        super(constants.PISTOL_RELOAD_TIME, constants.PISTOL_BULLETS_IN_CHAMBER, constants.PISTOL_MAGAZINE_RELOAD_TIME,
            ProjectileType.PISTOL)
    }
}

module.exports = Pistol;