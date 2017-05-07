#version 410

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;

uniform mat4 model_view_projection_matrix;
uniform mat4 model_view_matrix;
uniform mat4 normal_matrix;

uniform vec3 material_ambient, material_diffuse, material_specular;
uniform float specular_power;

uniform int number_of_lights;
uniform vec3 light_position[64], light_color[64];
uniform bool light_is_directional[64];

out vec4 v_color;

vec3 light_direction(vec3 eye_coord, int i) {
    if (light_is_directional[i]) {
        return light_position[i];
    }
    return light_position[i] - eye_coord;
}

vec3 gouraud_shading(vec3 n_normal, vec3 eye_coord, vec3 view_vector, int i) {
    vec3 n_light = normalize(light_direction(eye_coord, i));
    float cos_angle = max(0.0, dot(n_normal, n_light));
    float intensity = pow(max(0.0, dot(reflect(-n_light, n_normal), view_vector)), specular_power);
    return material_ambient * light_color[i] +
           material_diffuse * light_color[i] * cos_angle +
           material_specular * light_color[i] * intensity;
}

void main() {
    vec3 n_normal = normalize(vec3(normal_matrix * vec4(normal, 0.0)));
    vec3 eye_coord = vec3(model_view_matrix * position);
    vec3 view_vector = normalize(-eye_coord);

    vec4 sum_color = vec4(0.0);
    for (int i = 0; i < number_of_lights; i++) {
        sum_color += vec4(gouraud_shading(n_normal, eye_coord, view_vector, i), 1.0);
    }
    v_color = sum_color;

    gl_Position = model_view_projection_matrix * position;
}
