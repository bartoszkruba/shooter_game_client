const Matter = require('matter-js');
const constants = require('../settings/constants');
const Pistol = require('./Pistol');
const BaseObject = require('./BaseObject');

class Agent extends BaseObject {

    constructor(x = 0, y = 0, weapon = new Pistol(), facingDirectionAngle = 0, id) {
        super(id);
        this.bounds = Matter.Bodies.rectangle(x, y, constants.PLAYER_SPRITE_WIDTH, constants.PLAYER_SPRITE_HEIGHT);
        this.weapon = weapon;
        this.facingDirectionAngle = facingDirectionAngle;

        this.isWPressed = false;
        this.isAPressed = false;
        this.isSPressed = false;
        this.isDPressed = false;
        this.isRPressed = false;
        this.isLMPressed = false;

        this.velocity = {x: 0, y: 0}
    }

    canShoot() {
        return this.weapon.canShoot();
    }

    shoot() {
        this.weapon.shoot();
    }
}

module.exports = Agent;