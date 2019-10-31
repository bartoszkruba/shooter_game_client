const Matter = require('matter-js');
const BaseObject = require('BaseObject');

export default class Projectile extends BaseObject {
    constructor(x = 0, y = 0, xSpeed = 0, ySpeed = 0, radius, type, speed, id) {
        super(id);
        this.bounds = Matter.Body.circle(x, y, radius);
        this.velocity = Matter.Vector(xSpeed, ySpeed);
        this.type = type;
        this.speed = speed;
    }
}