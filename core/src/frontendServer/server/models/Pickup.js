const Matter = require('matter-js');

class Pickup {
    constructor(x, y, width, height, type, id) {
        this.bounds = Matter.Bodies.rectangle(x, y, width, height);
        this.type = type;
        this.id = id;
    }
}

module.exports = Pickup;

