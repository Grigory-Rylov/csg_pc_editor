precision mediump float;
uniform vec3 u_LightPos;
varying vec3 v_Position;
varying vec3 v_Normal;
varying vec4 v_Color;

void main()
{
    float distance = length(u_LightPos - v_Position);
    vec3 lightVector = normalize(u_LightPos - v_Position);
    float diffuse = max(dot(v_Normal, lightVector), 0.0);
    float ambient = 0.35;
    float lighting = ambient + diffuse * 0.65;
    gl_FragColor = vec4(v_Color.rgb * lighting, v_Color.a);
}
