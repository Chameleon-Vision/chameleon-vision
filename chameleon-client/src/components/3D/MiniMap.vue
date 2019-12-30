<template>
    <div>
        <canvas id="canvasId" width="800" height="800"/>
    </div>
</template>

<script>
    export default {
        name: "MiniMap",
        props: {
            translation: {
                x: Number,
                y: Number,
            },
            radians: Number,
            horizontalFOV: Number,
        },
        data() {
            return {
                ctx: undefined,
                canvas: undefined,
                x: 0,
                y: 0,
                targetWidth: 40,
                targetHeight: 6
            }
        },
        watch: {
            translation: {
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
                this.getFieldLocation();
                this.drawTarget();
                this.drawText();
            },
            drawText() {
                this.ctx.fillStyle = "whitesmoke";
                this.ctx.fillText(`X: ${this.translation.x.toFixed(2)}, Y: ${this.translation.y.toFixed(2)}, ∠${(Math.PI * this.radians / 180).toFixed(2)}° `, this.y - 30, this.x - 5);
            },
            drawTarget() {
                // first save the untranslated/unrotated context
                this.ctx.save();
                this.ctx.beginPath();
                // move the rotation point to the center of the rect
                this.ctx.translate(this.y + this.targetWidth / 2, this.x + this.targetHeight / 2); // wpi lib makes x forward and back and y left to right
                // rotate the rect
                this.ctx.rotate(this.radians);

                // draw the rect on the transformed context
                // Note: after transforming [0,0] is visually [x,y]
                //       so the rect needs to be offset accordingly when drawn
                this.ctx.rect(-this.targetWidth / 2, -this.targetHeight / 2, this.targetWidth, this.targetHeight);

                this.ctx.fillStyle = "#01a209";
                this.ctx.fill();

                // restore the context to its untranslated/unrotated state
                this.ctx.restore();

            },
            drawPlayer() {
                this.ctx.beginPath();

                this.ctx.moveTo(400, 820);
                this.ctx.lineTo(400 + this.hLen, 650);
                this.ctx.lineTo(400 - this.hLen, 650);
                this.ctx.closePath();
                this.ctx.fillStyle = this.grad;
                this.ctx.fill();
            },
            clearBoard() {
                this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height); // clearing the canvas
            },
            getFieldLocation() {
                this.x = 800 - (160 * this.translation.x); // getting meters as pixels
                this.y = 160 * this.translation.y;
            }
        },
        computed: {
            hLen: {
                get() {
                    return Math.tan(this.horizontalFOV / 2 * Math.PI / 180) * 150;
                }
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