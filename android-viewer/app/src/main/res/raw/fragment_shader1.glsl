precision mediump float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
uniform vec3 u_LightPos;       	// The position of the light in eye space.

varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.

// The entry point for our fragment shader.
void main()
{
	// Will be used for attenuation.
    float distance = length(u_LightPos - v_Position);

	// Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - v_Position);
    // Использование интенсивности и радиуса
    float intensity = 1.2; // Увеличение интенсивности света
    float range = 1000.0; // Увеличение радиуса действия света

    // Нормализованное затухание света на основе расстояния и радиуса
    float attenuation = clamp(1.0 - distance / range, 0.0, 1.0);
    attenuation = attenuation * attenuation;

    float diffuse = max(dot(v_Normal, lightVector), 0.0);
    // Применяем интенсивность и затухание
    diffuse = diffuse * intensity * attenuation;

    float ambient = 0.1;
    diffuse = diffuse + ambient;

	// Add attenuation.
    //diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance)));

    // Add ambient lighting
    //diffuse = diffuse + 0.7;

    vec4 fragmentColor = vec4(0.5, 0.5, 0.0, 1.0);

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = (diffuse * fragmentColor);
  }

