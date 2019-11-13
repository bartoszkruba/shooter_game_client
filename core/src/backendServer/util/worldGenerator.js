const constants = require('../settings/constants');

const minX = constants.WALL_SPRITE_WIDTH;
const maxX = constants.MAP_WIDTH - 2 * constants.WALL_SPRITE_WIDTH;
const minY = constants.WALL_SPRITE_HEIGHT;
const maxY = constants.MAP_HEIGHT - 2 * constants.WALL_SPRITE_HEIGHT;

const generateWalls = () => {
    const walls = [];

    for (let i = 0; i < 200; i++) {
        walls.push(generateRandomWall())
    }

    return walls;
};

const generateRandomWall = () => {
    const x = Math.ceil(Math.random() * (maxX - minX)) + minX;
    const y = Math.ceil(Math.random() * (maxY - minY)) + minY;
    return {x, y}
};

module.exports = {generateWalls};