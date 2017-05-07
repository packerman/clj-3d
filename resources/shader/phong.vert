#version 410

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;

uniform mat4 model_view_projection_matrix, model_view_matrix;
uniform mat4 normal_matrix;

out vec3 v_normal, eye_coord;

void main() {
    v_normal = vec3(normal_matrix * vec4(normal, 0.0));
    eye_coord = vec3(model_view_matrix * position);
    gl_Position = model_view_projection_matrix * position;
}
