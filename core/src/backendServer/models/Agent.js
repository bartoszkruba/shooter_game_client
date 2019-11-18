const Matter = require('matter-js');
const constants = require('../settings/constants');
const Pistol = require('./Pistol');
const BaseObject = require('./BaseObject');

class Agent extends BaseObject {

    constructor(x = 0, y = 0, name, kills = 0, deaths = 0, isDead = false, currentHealth, weapon = new Pistol(), facingDirectionAngle = 0, id) {
        super(id);
        this.bounds = Matter.Bodies.rectangle(x, y, constants.PLAYER_SPRITE_WIDTH, constants.PLAYER_SPRITE_HEIGHT);
        this.weapon = weapon;
        this.facingDirectionAngle = facingDirectionAngle;
        this.isDead = isDead;
        this.currentHealth = currentHealth;
        this.name = name;
        this.deaths = deaths;
        this.kills = kills;

        this.isWPressed = false;
        this.isAPressed = false;
        this.isSPressed = false;
        this.isDPressed = false;
        this.isRPressed = false;
        this.isLMPressed = false;

        this.pickWeapon = false;

        this.velocity = {x: 0, y: 0};

        this.reloadMark = -1;

        this.viewportZones = []
    }

    canShoot() {
        return this.weapon.canShoot();
    }

    shoot() {
        this.weapon.shoot();
    }

    takeDamage(damage) {
        this.currentHealth -= damage;
        if (this.currentHealth < 0) {
            this.currentHealth = 0;
            this.isDead = true;
        } else this.isDead = false;
    }
}

module.exports = Agent;