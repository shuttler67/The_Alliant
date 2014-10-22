//VERTEX

#version 330

uniform mat4 ModelViewMatrix;
uniform mat4 ModelViewProjectionMatrix;
uniform mat3 NormalMatrix;

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec3 normal;
layout(location = 2) in float materialIndex;
layout(location = 3) in vec2 texCoord;

smooth out vec3 fragVert;
smooth out vec3 fragNormal;
smooth out vec2 fragTexCoord;
flat out int fragMaterialIndex;

void main() {
    fragVert = (ModelViewMatrix * vec4(vertex, 1)).xyz;
    fragNormal = normalize(NormalMatrix * normal);
    fragTexCoord = texCoord;
	fragMaterialIndex = int(materialIndex);

    gl_Position = ModelViewProjectionMatrix * vec4(vertex, 1);
}
