const Matter = require('matter-js');
const BaseObject = require('../BaseObject');


class Projectile extends BaseObject {
    constructor(x = 0, y = 0, xSpeed = 0, ySpeed = 0, radius, type, speed, damage, id, agentId) {
        super(id);
        this.bounds = Matter.Bodies.circle(x, y, radius);
        this.velocity = {};
        this.velocity.x = xSpeed;
        this.velocity.y = ySpeed;
        this.speed = speed;
        this.type = type;
        this.agentId = agentId;
        this.damage = damage;
    }
}

module.exports = Projectile;