//FRAGMENT

#version 120

varying vec4 colour;
varying vec2 texCoord;
varying vec4 vertex;
varying vec3 normal;

uniform sampler2D texture1;

void main() {
    gl_FragColor = colour * texture2D(texture1, texCoord);

    vec3 lightDirection = normalize(gl_LightSource[0].position.xyz - vertex.xyz);
    float diffuseLightIntensity = max(0.0, dot(normal, lightDirection));
    gl_FragColor.rgb *= diffuseLightIntensity;
    gl_FragColor.rgb += mix(gl_LightModel.ambient.rgb , gl_FrontMaterial.ambient.rgb, 0.5);

    vec3 reflectionDirection = normalize(reflect(-lightDirection, normal) );
    float specular = max(0.0, dot(normal, reflectionDirection) );

    if (diffuseLightIntensity != 0.0) {
        float fspecular = pow(specular, gl_FrontMaterial.shininess);

        gl_FragColor.rgb *= vec3(fspecular, fspecular, fspecular);
    }
}