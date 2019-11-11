const Matter = require('matter-js');
const BaseObject = require('./BaseObject');

class Pickup extends BaseObject {
    constructor(x, y, width, height, type, id, ammunition) {
        super(id);
        this.bounds = Matter.Bodies.rectangle(x, y, width, height);
        this.type = type;
        this.id = id;
        this.ammunition = ammunition
    }
}

module.exports = Pickup;

