#version 330 core

layout (location = 0) in vec4 vertex; // <vec2 position, vec2 texCoords>

out vec2 TexCoords;

uniform mat4 model;
uniform mat4 projection;

void main() {
    TexCoords = vertex.zw;
    gl_Position = vec4(vertex.xy, 1.0f, 1.0f);
}