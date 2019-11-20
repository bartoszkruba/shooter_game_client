const Matter = require('matter-js');

const constants = require('../../settings/constants');
const BaseObject = require('../BaseObject');

class ExplosiveBarrel extends BaseObject {
    constructor(x, y, id) {
        super(id);
        this.bounds = Matter.Bodies.rectangle(x, y, constants.EXPLOSIVE_BARREL_SPRITE_WIDTH,
            constants.EXPLOSIVE_BARREL_SPRITE_HEIGHT)
    }
}

module.exports = ExplosiveBarrel;