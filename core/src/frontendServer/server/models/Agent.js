const Matter = require('matter-js');
const constants = require('../settings/constants');
const Pistol = require('./Pistol');
const BaseObject = require('./BaseObject');

class Agent extends BaseObject {

    constructor(x = 0, y = 0, isDead = false, currentHealth, weapon = new Pistol(), facingDirectionAngle = 0, id) {
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

    takeDamage(){
            console.log("before",  this.currentHealth)
        if(this.currentHealth >= constants.TAKE_DAMAGE) {
            this.currentHealth = this.currentHealth - 30;
            //this.takeHp = takeHp;
            console.log("after",  this.currentHealth)
        } else {this.isDead = true}
    }
}

module.exports = Agent;