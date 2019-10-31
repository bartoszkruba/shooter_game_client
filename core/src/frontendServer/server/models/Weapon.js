export default class Weapon {
    #lastShoot = 0;

    constructor(reloadTime) {
        this.reloadTime = reloadTime;
    }

    canShoot() {
        return new Date().getTime() - this.#lastShoot > this.reloadTime
    }

    shoot() {
        if (this.canShoot()) {
            this.#lastShoot = new Date().getTime();
            return true
        } else return false
    }
}