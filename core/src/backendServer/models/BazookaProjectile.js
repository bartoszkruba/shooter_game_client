const Projectile = require('./Projectile');
const ProjectileType = require('./ProjectileType');
const constants = require('../settings/constants');

class BazookaProjectile extends Projectile {
    constructor(x = 0, y = 0, xSpeed = 0, ySpeed = 0, id) {
        super(x, y, xSpeed, ySpeed, constants.STANDARD_PROJECTILE_WIDTH / 2, ProjectileType.BAZOOKA,
            constants.BAZOOKA_BULLET_SPEED, id)
    }
}

module.exports = BazookaProjectile;