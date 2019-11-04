const Matter = require('matter-js');

const c = Matter.Bodies.circle(10, 10, 10);

const d = Matter.Bodies.circle(200, 200, 10);

console.log(c.position);

console.log(Matter.SAT.collides(c, d).collided);

Matter.Body.setPosition(c, {x: 200, y: 200});

console.log(Matter.SAT.collides(c, d).collided);

console.log(c.position);