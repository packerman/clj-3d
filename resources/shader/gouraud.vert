#version 410

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;

uniform mat4 model_view_projection_matrix;
uniform mat4 model_view_matrix;
uniform mat4 normal_matrix;

uniform vec3 material_ambient, material_diffuse, material_specular;
uniform float specular_power;

uniform vec3 light_position, light_color;
uniform bool light_is_directional;

out vec4 v_color;

vec3 light_direction(vec3 eye_coord) {
    if (light_is_directional) {
        return light_position;
    }
    return light_position - eye_coord;
}

void main() {
    vec3 n_normal = normalize(vec3(normal_matrix * vec4(normal, 0.0)));
    vec3 eye_coord = vec3(model_view_matrix * position);
    vec3 n_light = normalize(light_direction(eye_coord));
    vec3 view_vector = normalize(-eye_coord);
    float cos_angle = max(0.0, dot(n_normal, n_light));
    float intensity = pow(max(0.0, dot(reflect(-n_light, n_normal), view_vector)), specular_power);
    v_color = vec4(material_ambient * light_color +
                   material_diffuse * light_color * cos_angle +
                   material_specular * light_color * intensity, 1);
    gl_Position = model_view_projection_matrix * position;
}
