const Matter = require('matter-js');

class Pickup {
    constructor(x, y, width, height, type, id, ammunition) {
        this.bounds = Matter.Bodies.rectangle(x, y, width, height);
        this.type = type;
        this.id = id;
        this.ammunition = ammunition
    }
}

module.exports = Pickup;

