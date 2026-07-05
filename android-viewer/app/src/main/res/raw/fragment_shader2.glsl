precision mediump float;		// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
uniform vec3 u_LightPos;       	// The position of the light in eye space.
varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.
varying vec4 v_Color;           // This is the color from the vertex shader interpolated across the
		  						// triangle per fragment.
void main()                     // The entry point for our fragment shader.
{
	// Will be used for attenuation.
    float distance = length(u_LightPos - v_Position);

	// Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - v_Position);
    // Использование интенсивности и радиуса
    float intensity = 1.2; // Увеличение интенсивности света
    float range = 500.0; // Увеличение радиуса действия света

    // Нормализованное затухание света на основе расстояния и радиуса
    float attenuation = clamp(1.0 - distance / range, 0.0, 1.0);
    attenuation = attenuation * attenuation;

	// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
	// pointing in the same direction then it will get max illumination.
    float diffuse = max(dot(v_Normal, lightVector), 0.0);

	// Add attenuation.
	diffuse = diffuse * intensity * attenuation;

    // Add ambient lighting
    float ambient = 0.9;
    diffuse = diffuse + ambient;
    //diffuse = diffuse + 0.7;

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = (diffuse * v_Color);
}
