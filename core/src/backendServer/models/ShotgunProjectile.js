const Projectile = require('./Projectile');
const ProjectileType = require('./ProjectileType');
const constants = require('../settings/constants');

class ShotgunProjectile extends Projectile {
    constructor(x = 0, y = 0, xSpeed = 0, ySpeed = 0, id, agentId) {
        super(x, y, xSpeed, ySpeed, constants.STANDARD_PROJECTILE_WIDTH / 2, ProjectileType.SHOTGUN,
            constants.SHOTGUN_BULLET_SPEED, constants.SHOTGUN_PROJECTILE_DAMAGE, id, agentId)
    }
}

module.exports = ShotgunProjectile;