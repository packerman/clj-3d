#version 410

precision mediump float;

uniform vec3 material_ambient, material_diffuse, material_specular;
uniform vec3 light_position, light_color;
uniform float specular_power;

/*
vec3 n_light = normalize(light_position - eye_coord);
    vec3 view_vector = normalize(-eye_coord);
    float cos_angle = max(0.0, dot(n_normal, n_light));
    float intensity = pow(max(0.0, dot(reflect(-n_light, n_normal), view_vector)), specular_power);
    v_color = vec4(material_ambient * light_color +
                   material_diffuse * light_color * cos_angle +
                   material_specular * light_color * intensity, 1);
*/

in vec3 v_normal, eye_coord;
out vec4 frag_color;

void main() {
    vec3 n_normal = normalize(v_normal);
    vec3 n_light = normalize(light_position - eye_coord);
    vec3 view_vector = normalize(-eye_coord);

    float cos_angle = max(0.0, dot(n_normal, n_light));
    float intensity = pow(max(0.0, dot(reflect(-n_light, n_normal), view_vector)), specular_power);

    frag_color = vec4(material_ambient * light_color +
                      material_diffuse * light_color * cos_angle +
                      material_specular * light_color * intensity, 1);
}
