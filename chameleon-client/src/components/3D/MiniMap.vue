<template>
    <canvas id="canvasId" width="800" height="800"/>
</template>

<script>
    export default {
        name: "MiniMap",
        props: {
            translation: {
                x: Number,
                y: Number,
            },
            rotation: Number
        },
        data() {
            return {
                ctx: undefined,
                canvas: undefined,
            }
        },
        watch: {
            location: {
                deep: true,
                handler() {
                    this.draw();
                }
            },
        },
        methods: {
            draw() {
                this.clearBoard();
                this.drawPlayer();
                // this.drawLine();
                this.drawTarget();
                this.drawText();
            },
            drawText() {
                this.ctx.fillStyle = "whitesmoke";
                this.ctx.fillText(`X: ${this.translation.x}, Y: ${this.translation.y}, ∠${Math.PI * this.radians / 180}° `, this.translation.x - 50, this.translation.y - 25);
            },
            drawTarget() {
                const width = 40;
                const height = 6;
                // first save the untranslated/unrotated context
                this.ctx.save();
                this.ctx.beginPath();
                // move the rotation point to the center of the rect
                this.ctx.translate(this.translation.x + width / 2, this.translation.y + height / 2);
                // rotate the rect
                this.ctx.rotate(this.radians);

                // draw the rect on the transformed context
                // Note: after transforming [0,0] is visually [x,y]
                //       so the rect needs to be offset accordingly when drawn
                this.ctx.rect(-width / 2, -height / 2, width, height);

                this.ctx.fillStyle = "#01a209";
                this.ctx.fill();

                // restore the context to its untranslated/unrotated state
                this.ctx.restore();

            },
            drawPlayer() {
                this.ctx.beginPath();
                this.ctx.moveTo(400, 820);
                this.ctx.lineTo(700, 650);
                this.ctx.lineTo(100, 650);
                this.ctx.closePath();
                this.ctx.fillStyle = this.grad;
                this.ctx.fill();
            },
            clearBoard() {
                this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height); // clearing the canvas
            }
        },
        mounted: function () {
            const canvas = document.getElementById("canvasId"); // getting the canvas element
            const ctx = canvas.getContext("2d"); // getting the canvas context
            this.canvas = canvas; // setting the canvas as a vue variable
            this.ctx = ctx; // setting the canvas context as a vue variable
            this.grad = this.ctx.createLinearGradient(400, 800, 400, 600);
            this.grad.addColorStop(0, "rgba(1,92,9,0.44)");
            this.grad.addColorStop(0.7, "#2b2b2b");

            // setting canvas context values for drawing


            this.ctx.font = "26px Arial";
            this.ctx.strokeStyle = "whitesmoke";

            this.$nextTick(function () {
                this.drawPlayer();
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