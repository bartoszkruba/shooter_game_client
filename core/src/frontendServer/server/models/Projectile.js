const Matter = require('matter-js');

export default class Projectile {
    constructor(x = 0, y = 0, xSpeed = 0, ySpeed = 0, radius, type, speed) {
        this.bounds = Matter.Body.circle(x, y, radius);
        this.velocity = Matter.Vector(xSpeed, ySpeed);
        this.type = type;
        this.speed = speed;
    }
}