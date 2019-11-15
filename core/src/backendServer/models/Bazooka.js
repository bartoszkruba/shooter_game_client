const constants = require('../settings/constants');
const Weapon = require('./Weapon');
const ProjectileType = require('./ProjectileType');

class Bazooka extends Weapon {
    constructor() {
        super(constants.BAZOOKA_RELOAD_TIME, constants.BAZOOKA_BULLETS_IN_CHAMBER,
            constants.BAZOOKA_MAGAZINE_RELOAD_TIME, ProjectileType.BAZOOKA)
    }
}

module.exports = Bazooka;