precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
uniform float inputWidth;
uniform float inputHeight;

void main() {
    vec2 coordinate = vec2(textureCoordinate.x, 1.0 - textureCoordinate.y);
    gl_FragColor = texture2D(inputTexture, coordinate);
}