//VERTEX

#version 120

varying vec4 colour;
varying vec2 texCoord;
varying vec4 vertex;
varying vec3 normal;

void main() {
    vertex = gl_ModelViewMatrix * gl_Vertex;
    normal = normalize(gl_NormalMatrix * gl_Normal);
    colour = gl_FrontMaterial.diffuse;
    texCoord = gl_MultiTexCoord0.st;

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}