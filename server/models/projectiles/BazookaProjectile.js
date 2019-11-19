const Projectile = require('./Projectile');
const ProjectileType = require('./ProjectileType');
const constants = require('../../settings/constants');

class BazookaProjectile extends Projectile {
    constructor(x = 0, y = 0, xSpeed = 0, ySpeed = 0, id, agentId) {
        super(x, y, xSpeed, ySpeed, constants.BAZOOKA_PROJECTILE_WIDTH / 2, ProjectileType.BAZOOKA,
            constants.BAZOOKA_BULLET_SPEED, 0, id, agentId)
    }
}

module.exports = BazookaProjectile;