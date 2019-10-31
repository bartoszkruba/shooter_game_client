const constants = require('../settings/constants');
const Weapon = require('Weapon');

export default class Pistol extends Weapon {
    constructor() {
        super(constants.PISTOL_RELOAD_TIME)
    }
}