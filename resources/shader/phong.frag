#version 410

precision mediump float;

uniform vec3 material_ambient, material_diffuse, material_specular;
uniform float specular_power;

uniform int number_of_lights;
uniform vec3 light_position[64], light_color[64];
uniform bool light_is_directional[64];

in vec3 v_normal, eye_coord;
out vec4 frag_color;

vec3 light_direction(int i) {
    if (light_is_directional[i]) {
        return light_position[i];
    }
    return light_position[i] - eye_coord;
}

vec3 phong_shading(vec3 n_normal, vec3 view_vector, int i) {
    vec3 n_light = normalize(light_direction(i));
    float cos_angle = max(0.0, dot(n_normal, n_light));
    float intensity = pow(max(0.0, dot(reflect(-n_light, n_normal), view_vector)), specular_power);
    return material_ambient * light_color[i] +
                     material_diffuse * light_color[i] * cos_angle +
                     material_specular * light_color[i] * intensity;
}

void main() {
    vec3 n_normal = normalize(v_normal);
    vec3 view_vector = normalize(-eye_coord);

    vec4 sum_color = vec4(0.0);
    for (int i = 0; i < number_of_lights; i++) {
        sum_color += vec4(phong_shading(n_normal, view_vector, i), 1.0);
    }
    frag_color = sum_color;
}
