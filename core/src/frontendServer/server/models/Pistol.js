const constants = require('../settings/constants');
const Weapon = require('./Weapon');

class Pistol extends Weapon {
    constructor() {
        super(constants.PISTOL_RELOAD_TIME, constants.PISTOL_BULLETS_IN_CHAMBER, constants.PISTOL_MAGAZINE_RELOAD_TIME)
    }
}

module.exports = Pistol;