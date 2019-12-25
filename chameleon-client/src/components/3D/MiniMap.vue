<template>
    <canvas id="canvasId" width="800" height="800"/>
</template>

<script>
    export default {
        name: "MiniMap",
        props: {
            location: {
                x: 0,
                y: 0,
            },
            angle: Number
        },
        data() {
            return {
                ctx: undefined,
                canvas: undefined,
                image: undefined
            }
        },
        watch: {
            location: {
                deep: true,
                handler() {
                    this.draw();
                }
            }
        },
        methods: {
            draw() {
                this.ctx.save(); // saving canvas state before setting origin back to norman(0,0)
                this.clearBoard();
                this.drawLine();
                // start of static canvas objects
                this.drawTarget();
                this.drawText();
                // end of static canvas objects
                this.ctx.restore(); // restoring older state but with cleaned canvas
                this.drawPlayer();
            },
            drawText() {
                this.ctx.fillStyle = "whitesmoke";
                this.ctx.fillText(`X: ${this.location.x}, Y: ${this.location.y}, ∠${this.angle}° `, 200 - 15, this.location.y + 25);
            },
            drawTarget() {
                this.ctx.fillStyle = "#01a209";
                this.ctx.fillRect(380, 0, 40, 6); // setting the target rectangle
            },
            drawLine() {
                //player line draw
                this.ctx.beginPath();
                this.ctx.moveTo(400, this.location.y);
                this.ctx.lineTo(this.location.x, this.location.y);
                this.ctx.moveTo(400, this.location.y);
                this.ctx.lineTo(400, 0);
                this.ctx.stroke();
            },
            drawPlayer() {
                // drawing the player icon onto the canvas
                this.ctx.setTransform(0.4, 0, 0, 0.4, this.location.x, this.location.y);
                this.ctx.rotate((Math.PI * this.angle) / 180);
                this.ctx.drawImage(this.image, -this.image.width / 2, -this.image.height / 2);
            },
            clearBoard() {
                this.ctx.setTransform(1, 0, 0, 1, 0, 0); // setting origin to 0,0
                this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height); // clearing the canvas
            }
        },
        mounted: function () {
            const canvas = document.getElementById("canvasId"); // getting the canvas element
            const ctx = canvas.getContext("2d"); // getting the canvas context
            this.canvas = canvas; // setting the canvas as a vue variable
            this.ctx = ctx; // setting the canvas context as a vue variable
            const image = new Image; // creating a new image
            image.src = require('../../assets/robotIcon.svg'); // setting image src to be the player icon
            this.image = image; // setting the image as a vue variable
            // setting canvas context values for drawing

            this.ctx.lineWidth = 1;
            this.ctx.font = "26px Arial";
            this.ctx.strokeStyle = "whitesmoke";
            this.ctx.setLineDash([12, 30]);

            this.$nextTick(function () {
                this.drawTarget();
            });
        }
    }
</script>

<style scoped>
    #canvasId {
        width: 400px;
        height: 400px;
        background-color: #2b2b2b;
        border-radius: 5px;
        border: 2px solid grey;
        box-shadow: 0 0 5px 1px;
    }
</style>