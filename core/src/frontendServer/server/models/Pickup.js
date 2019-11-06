const Matter = require('matter-js');

class Pickup {
    constructor(x, y, width, height, type) {
        this.bounds = Matter.Bodies.rectangle(x, y, width, height)
        this.type = type
    }
}

module.exports = Pickup;

