const constants = require('../settings/constants');

const Matter = require("matter-js");

const minX = constants.WALL_SPRITE_WIDTH;
const maxX = constants.MAP_WIDTH - 2 * constants.WALL_SPRITE_WIDTH;
const minY = constants.WALL_SPRITE_HEIGHT;
const maxY = constants.MAP_HEIGHT - 2 * constants.WALL_SPRITE_HEIGHT;


function getRandomArbitrary(min, max) {
    return Math.random() * (max - min) + min;
}

const occupiedPlaces = [];

const generateWalls = () => {
    let walls = [];

    for (let i = 0; i < constants.MAP_OBJECTS_COUNT / 3; i++) {
        walls = [...walls, ...generateRandomVerticalWall()];
        walls = [...walls, ...generateRandomHorizontalWall()];
        walls = [...walls, ...generateRandomHouse()]
    }

    return walls;
};

const generateRandomVerticalWall = () => {
    let x;
    let y;

    while (true) {
        let free = true;
        x = Math.ceil(Math.random() * (maxX - minX)) + minX;
        y = Math.ceil(Math.random() * (maxY - minY - 11 * constants.WALL_SPRITE_HEIGHT)) + minY;

        rect = Matter.Bodies.rectangle(
            x + constants.WALL_SPRITE_WIDTH / 2,
            y + ((10 * constants.WALL_SPRITE_HEIGHT) / 2),
            constants.WALL_SPRITE_WIDTH,
            10 * constants.WALL_SPRITE_HEIGHT
        );

        occupiedPlaces.forEach(place => {
            if (Matter.SAT.collides(place, rect).collided) free = false
        });

        if (free) break;
    }

    return generateVerticalWall(x, y)
};

const generateRandomHorizontalWall = () => {
    let x, y;

    while (true) {
        let free = true;
        x = Math.ceil(Math.random() * (maxX - minX - 11 * constants.WALL_SPRITE_WIDTH)) + minX;
        y = Math.ceil(Math.random() * (maxY - minY)) + minY;
        rect = Matter.Bodies.rectangle(
            x + ((10 * constants.WALL_SPRITE_WIDTH) / 2),
            y + constants.WALL_SPRITE_HEIGHT / 2,
            10 * constants.WALL_SPRITE_WIDTH,
            constants.WALL_SPRITE_HEIGHT
        );

        occupiedPlaces.forEach(place => {
            if (Matter.SAT.collides(place, rect).collided) free = false
        });

        if (free) break;
    }

    return generateHorizontalWall(x, y)
};

const generateRandomHouse = () => {
    const minX = constants.WALL_SPRITE_WIDTH + constants.PLAYER_SPRITE_WIDTH;
    const maxX = constants.MAP_WIDTH - 12 * constants.WALL_SPRITE_WIDTH - constants.PLAYER_SPRITE_WIDTH;

    const minY = constants.WALL_SPRITE_HEIGHT + constants.PLAYER_SPRITE_HEIGHT;
    const maxY = constants.MAP_HEIGHT - 12 * constants.WALL_SPRITE_HEIGHT - constants.PLAYER_SPRITE_HEIGHT;

    let x, y;

    while (true) {
        let free = true;
        x = getRandomArbitrary(minX, maxX);
        y = getRandomArbitrary(minY, maxY);
        const rect = Matter.Bodies.rectangle(
            x + ((10 * constants.WALL_SPRITE_WIDTH) / 2),
            y + ((11 * constants.WALL_SPRITE_HEIGHT) / 2),
            10 * constants.WALL_SPRITE_WIDTH,
            11 * constants.WALL_SPRITE_HEIGHT);

        occupiedPlaces.forEach(place => {
            if (Matter.SAT.collides(place, rect).collided) free = false
        });

        if (free) break;
    }

    return generateHouse(x, y)
};

const generateVerticalWall = (x, y) => {
    const wall = [];

    let pos = y;
    for (let i = 0; i < 10; i++) {
        wall.push({x, y: pos});
        pos += constants.WALL_SPRITE_HEIGHT
    }

    occupiedPlaces.push(Matter.Bodies.rectangle(
        x + constants.WALL_SPRITE_WIDTH / 2,
        y + ((10 * constants.WALL_SPRITE_HEIGHT) / 2),
        constants.WALL_SPRITE_WIDTH + 2 * constants.PLAYER_SPRITE_WIDTH,
        10 * constants.WALL_SPRITE_HEIGHT + 2 * constants.PLAYER_SPRITE_HEIGHT
    ));

    return wall;
};

const generateHorizontalWall = (x, y) => {
    const wall = [];

    let pos = x;
    for (let i = 0; i < 10; i++) {
        wall.push({x: pos, y});
        pos += constants.WALL_SPRITE_HEIGHT
    }

    occupiedPlaces.push(Matter.Bodies.rectangle(
        x + ((10 * constants.WALL_SPRITE_WIDTH) / 2),
        y + constants.WALL_SPRITE_HEIGHT / 2,
        10 * constants.WALL_SPRITE_WIDTH + 2 * constants.PLAYER_SPRITE_WIDTH,
        constants.WALL_SPRITE_HEIGHT + 2 * constants.PLAYER_SPRITE_HEIGHT
    ));

    return wall;
};

const generateHouse = (x, y) => {
    let house = [];

    house = [...house, ...generateVerticalWall(x, y)];
    house = [...house, ...generateVerticalWall(x + 9 * constants.WALL_SPRITE_WIDTH, y)];
    house = [...house, ...generateHorizontalWall(x, y + 10 * constants.WALL_SPRITE_HEIGHT)];

    for (let i = 1; i <= 2; i++) {
        house.push({x: (x + i * constants.WALL_SPRITE_WIDTH), y})
    }

    for (let i = 1; i <= 3; i++) {
        house.push({x: (x + (10 * constants.WALL_SPRITE_WIDTH) - i * constants.WALL_SPRITE_WIDTH), y})
    }

    occupiedPlaces.push(Matter.Bodies.rectangle(
        x + ((10 * constants.WALL_SPRITE_WIDTH) / 2),
        y + ((11 * constants.WALL_SPRITE_HEIGHT) / 2),
        10 * constants.WALL_SPRITE_WIDTH + 2 * constants.PLAYER_SPRITE_WIDTH,
        11 * constants.WALL_SPRITE_HEIGHT + 2 * constants.PLAYER_SPRITE_HEIGHT));

    return house;
};

module.exports = {generateWalls};