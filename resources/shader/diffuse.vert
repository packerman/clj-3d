#version 410

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;

uniform mat4 model_view_projection_matrix;
uniform mat4 model_view_matrix;
uniform mat4 normal_matrix;

uniform vec3 material_ambient, material_diffuse;

uniform vec3 light_color;
uniform vec3 light_position;

out vec4 v_color;

void main() {
    vec3 normalized = normalize(vec3(normal_matrix * vec4(normal, 0.0)));
    vec3 eye_coord = vec3(model_view_matrix * position);
    vec3 n_light = normalize(light_position - eye_coord);
    v_color = vec4(material_ambient * light_color +
              material_diffuse * light_color * max(0.0, dot(normalized, n_light)), 1);
    gl_Position = model_view_projection_matrix * position;
}
