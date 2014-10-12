//FRAGMENT

#version 330

uniform sampler2D textures[9];

struct Material {
	float shininess;
	vec4 diffuseColor;
	vec3 ambientColor;
	vec3 specularColor;
};

const Material defaultMaterial = Material(1.0, vec4(1, 1, 1, 1), vec3(0.1, 0.1, 0.1), vec3(1, 1, 1));
uniform Material materials[19];

uniform vec3 lightAmbient;
uniform int lightCount;

struct Light {
	vec3 position;
	vec3 color;
	float attenuation;
}; 

const Light defaultLight = Light(vec3(0,0,0), vec3(1,1,1), 0.0);
uniform Light lights[5];

smooth in vec3 fragVert;
smooth in vec3 fragNormal;
smooth in vec2 fragTexCoord;
flat in int fragMaterialIndex;
flat in int fragTextureIndex;

out vec4 finalColor;

void main() {

	vec4 texColor = vec4(1, 1, 1, 1);
	switch (fragTextureIndex) {
		case 0: texColor = texture2D( textures[0], fragTexCoord); break;
		case 1: texColor = texture2D( textures[1], fragTexCoord); break;
		case 2: texColor = texture2D( textures[2], fragTexCoord); break;
		case 3: texColor = texture2D( textures[3], fragTexCoord); break;
		case 4: texColor = texture2D( textures[4], fragTexCoord); break;
		case 5: texColor = texture2D( textures[5], fragTexCoord); break;
		case 6: texColor = texture2D( textures[6], fragTexCoord); break;
		case 7: texColor = texture2D( textures[7], fragTexCoord); break;
		case 8: texColor = texture2D( textures[8], fragTexCoord); break;
	}

    Material material = defaultMaterial;
    switch (fragMaterialIndex) {
    	case 0: material = materials[0]; break;
		case 1: material = materials[1]; break;
		case 2: material = materials[2]; break;
		case 3: material = materials[3]; break;
		case 4: material = materials[4]; break;
		case 5: material = materials[5]; break;
		case 6: material = materials[6]; break;
		case 7: material = materials[7]; break;
		case 8: material = materials[8]; break;
		case 9: material = materials[9]; break;
		case 10: material = materials[10]; break;
		case 11: material = materials[11]; break;
		case 12: material = materials[12]; break;
		case 13: material = materials[13]; break;
		case 14: material = materials[14]; break;
		case 15: material = materials[15]; break;
		case 16: material = materials[16]; break;
		case 17: material = materials[17]; break;
		case 18: material = materials[18]; break;
	}
    
	vec4 surfaceColor = texColor * material.diffuseColor;// * material.diffuseColor; 
    
    vec3 diffuse = surfaceColor.rgb;
    vec3 specular = material.specularColor;
    
    for (int i = 0; i < lightCount; i++) {
    	Light light = defaultLight;
    	switch (i) 
    	{
    		case 0: light = lights[0]; break;
			case 1: light = lights[1]; break;
			case 2: light = lights[2]; break;
			case 3: light = lights[3]; break;
			case 4: light = lights[4]; break;
    	}
    	
		vec3 lightDirection = normalize(light.position - fragVert);
		
		//float dist = dot(surfaceToLight, surfaceToLight);
		//float attenuation = 1.0 / (1.0 + light.attenuation * dist);
		
		//surfaceToLight = normalize(surfaceToLight);
				
		float diffuseCoefficient = max(0.0, dot(fragNormal, lightDirection));
		diffuse *= light.color * diffuseCoefficient; //* attenuation; 
		
		float specularCoefficient = 0.0;
		if (diffuseCoefficient > 0.0) {
			specularCoefficient = pow(clamp(dot(fragNormal, reflect(-lightDirection, fragNormal) ) , 0.0, 1.0), material.shininess);
		}
		specular *= light.color * specularCoefficient; //* attenuation;
	}

	vec3 ambient = lightAmbient * material.ambientColor; //
	
	finalColor = vec4(diffuse + ambient + specular, surfaceColor.a); // + ambient + specular
}
