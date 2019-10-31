const Matter = require('matter-js');
const constants = require('../settings/constants');
const Pistol = require('Pistol');

export default class Agent {

    isWPressed = false;
    isAPressed = false;
    isSPressed = false;
    isDPressed = false;
    isLMPressed = false;

    constructor(x = 0, y = 0, weapon = Pistol(), facingDirectionAngle = 0) {
        this.bounds = Matter.Body.rectangle(x, y, constants.PLAYER_SPRITE_WIDTH, constants.PLAYER_SPRITE_HEIGHT);
        this.weapon = weapon;
        this.facingDirectionAngle = facingDirectionAngle;
    }

    canShoot() {
        return this.weapon.canShoot();
    }

    shoot() {
        this.weapon.shoot();
    }
}