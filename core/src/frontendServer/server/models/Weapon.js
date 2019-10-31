class Weapon {

    constructor(reloadTime) {
        this.reloadTime = reloadTime;
        this.lastShoot = 0;
    }

    canShoot() {
        return new Date().getTime() - this.lastShoot > this.reloadTime
    }

    shoot() {
        if (this.canShoot()) {
            this.lastShoot = new Date().getTime();
            return true
        } else return false
    }
}

module.exports = Weapon;