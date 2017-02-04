#version 410

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;

uniform mat4 model_view_projection_matrix;
uniform mat4 normal_matrix;

out vec4 v_color;

void main() {
    gl_Position = model_view_projection_matrix * position;
    vec3 normalized = normalize(vec3(normal_matrix * vec4(normal, 0.0)));
    v_color = vec4((normalized.x + 1.0)/2.0, (normalized.y + 1.0)/2.0, (normalized.z + 1.0)/2.0, 1.0);
}
