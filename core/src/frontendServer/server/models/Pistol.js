const constants = require('../settings/constants');
const Weapon = require('./Weapon');

class Pistol extends Weapon {
    constructor() {
        super(constants.PISTOL_RELOAD_TIME)
    }
}

module.exports = Pistol;