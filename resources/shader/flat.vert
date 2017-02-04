#version 410

layout(location = 0) in vec4 position;

uniform vec4 color;
uniform mat4 model_view_projection_matrix;

out vec4 v_color;

void main() {
    gl_Position = model_view_projection_matrix * position;
    v_color = color;
}