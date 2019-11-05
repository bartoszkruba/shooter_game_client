const Matter = require('matter-js');
const constants = require('../settings/constants');
const Pistol = require('./Pistol');
const BaseObject = require('./BaseObject');

class Agent extends BaseObject {

    constructor(x = 0, y = 0, isDead = false, currentHealth = PLAYER_MAX_HEALTH, weapon = new Pistol(), facingDirectionAngle = 0, id) {
        super(id);
        this.bounds = Matter.Bodies.rectangle(x, y, constants.PLAYER_SPRITE_WIDTH, constants.PLAYER_SPRITE_HEIGHT);
        this.weapon = weapon;
        this.facingDirectionAngle = facingDirectionAngle;
        this.isDead = isDead;
        this.currentHealth = currentHealth;

        this.isWPressed = false;
        this.isAPressed = false;
        this.isSPressed = false;
        this.isDPressed = false;
        this.isRPressed = false;
        this.isLMPressed = false;

        this.velocity = {x: 0, y: 0};

        this.reloadMark = -1;
    }

    canShoot() {
        return this.weapon.canShoot();
    }

    shoot() {
        this.weapon.shoot();
    }

    takeDamage(currentHealth){
        if(currentHealth >= 50) {
            this.currentHealth =  this.currentHealth - 50;
        } else {this.isDead = true}
    }
}

module.exports = Agent;