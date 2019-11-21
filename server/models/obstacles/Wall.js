const Matter = require('matter-js');

const constants = require('../../settings/constants');
const BaseObject = require('../BaseObject');

class Wall extends BaseObject {
    constructor(x, y, id) {
        super(id);
        this.bounds = Matter.Bodies.rectangle(x, y, constants.WALL_SPRITE_WIDTH, constants.WALL_SPRITE_HEIGHT)
    }
}

module.exports = Wall;