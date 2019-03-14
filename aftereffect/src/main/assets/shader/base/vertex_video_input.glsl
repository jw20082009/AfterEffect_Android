attribute vec4 aPosition;//顶点位置
attribute vec4 aTextureCoord;//S T 纹理坐标
varying vec2 textureCoordinate;
uniform mat4 uMatrix;
uniform mat4 transformMatrix;
void main() {
    textureCoordinate = (transformMatrix * aTextureCoord).xy;
    gl_Position = uMatrix*aPosition;
}