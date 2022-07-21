#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

uniform float Time;
varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

const float PI = 3.1415926535897932384626433832795;

int getFrame(sampler2D sampler, float xpos) {
	ivec4 left_test = ivec4(-1, -1, -1, -1);
	ivec4 middle_test = ivec4(-1, -1, -1, -1);
	ivec4 right_test = ivec4(-1, -1, -1, -1);
	for (int i = 0; i < 4; i++) {
		vec4 testTexel = texture2D(sampler, vec2(xpos + oneTexel.x * ((i + 1) * 3 - 1), 0.0));
		if (testTexel.g < 0.003 && testTexel.b < 0.003) {
			if (testTexel.r > 0.984)
				left_test[i] = 0;
			else if (testTexel.r > 0.976)
				left_test[i] = 1;
			else
				return -1;
		}
		else {
			return -1;
		}
	}
	for (int i = 0; i < 4; i++) {
		vec4 testTexel = texture2D(sampler, vec2(xpos + oneTexel.x * ((i + 5) * 3 - 1), 0.0));
		if (testTexel.g < 0.003 && testTexel.b < 0.003) {
			if (testTexel.r > 0.984)
				middle_test[i] = 0;
			else if (testTexel.r > 0.976)
				middle_test[i] = 1;
			else
				return -1;
		}
		else {
			return -1;
		}
	}
	for (int i = 0; i < 4; i++) {
		vec4 testTexel = texture2D(sampler, vec2(xpos + oneTexel.x * ((i + 9) * 3 - 1), 0.0));
		if (testTexel.g < 0.003 && testTexel.b < 0.003) {
			if (testTexel.r > 0.984)
				right_test[i] = 0;
			else if (testTexel.r > 0.976)
				right_test[i] = 1;
			else
				return -1;
		}
		else {
			return -1;
		}
	}
	
	return left_test[0] * 2048 + left_test[1] * 1024 + left_test[2] * 512 + left_test[3] * 256 + middle_test[0] * 128 + middle_test[1] * 64 + middle_test[2] * 32 + middle_test[3] * 16 + right_test[0] * 8 + right_test[1] * 4 + right_test[2] * 2 + right_test[3];
}

int imod(int num, int m) {
	return num - (num / m * m);
}

vec3 hsb2rgb(float h, float s, float v) {
	float r = 0.0;
	float g = 0.0;
	float b = 0.0;
	
	int i = imod(int(h) / 60, 6);
	
	float f = (h / 60) - i;
	float p = v * (1 - s);
	float q = v * (1 - f * s);
	float t = v * (1 - (1 - f) * s);
	if (i == 0) {
		r = v;
		g = t;
		b = p;
	} else if (i == 1) {
		r = q;
		g = v;
		b = p;
	} else if (i == 2) {
		r = p;
		g = v;
		b = t;
	} else if (i == 3) {
		r = p;
		g = q;
		b = v;
	} else if (i == 4) {
		r = t;
		g = p;
		b = v;
	} else if (i == 5) {
		r = v;
		g = p;
		b = q;
	}
	
	return vec3(r, g, b);
}

vec3 turnHue(vec3 color, int angle) {
	vec3 RGB = color * 255;
	int maxi = int(max(max(RGB.r, RGB.g), RGB.b));
	int mini = int(min(min(RGB.r, RGB.g), RGB.b));

	float hsbB = maxi / 255.0;
	float hsbS;
	if (maxi == 0)
		hsbS = 0.0;
	else
		hsbS = (maxi - mini) / float(maxi);

	return hsb2rgb(float(angle), hsbS, hsbB);
}

void recordColor(int num, float xpos) {
	if (texCoord.x <= xpos + oneTexel.x * 3) {
		if (num / 2048 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 6) {
		if (imod(num, 2048) / 1024 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 9) {
		if (imod(num, 1024) / 512 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 12) {
		if (imod(num, 512) / 256 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 15) {
		if (imod(num, 256) / 128 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 18) {
		if (imod(num, 128) / 64 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 21) {
		if (imod(num, 64) / 32 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 24) {
		if (imod(num, 32) / 16 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 27) {
		if (imod(num, 16) / 8 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 30) {
		if (imod(num, 8) / 4 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if (texCoord.x <= xpos + oneTexel.x * 33) {
		if (imod(num, 4) / 2 == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else {
		if (imod(num, 2) == 1) 
			gl_FragColor = vec4(0.98, 0.0, 0.0, 1.0);
		else 
			gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
}

void theWorld(int frame, vec4 CurrTexel) {
	float tick = frame / 108.0;
	
	float r = sqrt(pow((texCoord.x - 0.5) * InSize.x, 2) + pow((texCoord.y - 0.5) * InSize.y, 2));
	float R = pow(sin(tick * PI), 8) * InSize.x * 9;
	
	if (r < R) {
		vec2 dir = texCoord.xy - vec2(0.5, 0.5);
		vec2 offset = 0.15 * dir * pow(length(dir), 0.5);
		if (tick < 0.2)
			offset *= tick * 15 + 1;
		else if (tick < 0.75)
			offset *= 4;
		else
			offset *= (0.95 - tick) * 15 + 1;
		vec2 uv = texCoord.xy - offset;
		
		vec4 ori_color = texture2D(DiffuseSampler, uv);
		vec3 reverse_color = vec3(1.0 - ori_color.r, 1.0 - ori_color.g, 1.0 - ori_color.b);
		vec3 color = turnHue(reverse_color, int(180 * pow(sin(tick * PI), 4)));
		gl_FragColor = vec4(color, 1.0);
	}
	else {
		if (tick < 0.5) {
			if (CurrTexel.g < 0.003 && CurrTexel.b < 0.003)
				gl_FragColor = texture2D(PrevSampler, texCoord);
			else
				gl_FragColor = vec4(CurrTexel.rgb, 1.0);
		}
		else {
			float gray = (CurrTexel.r + CurrTexel.g * 2 + CurrTexel.b) / 4;
			vec3 faded = vec3((CurrTexel.r + gray * 4) / 5, (CurrTexel.g + gray * 4) / 5, (CurrTexel.b + gray * 4) / 5);
			gl_FragColor = vec4(faded, 1.0);
		}
	}
}

void main() {
	vec4 PrevTexel = texture2D(PrevSampler, texCoord);
	
	if (texCoord.y <= oneTexel.y && texCoord.x > oneTexel.x * 36 && texCoord.x <= oneTexel.x * 72) {
		if (Time < 0.1)
			recordColor(0, oneTexel.x * 36);
		else if (Time < 0.9)
			recordColor(getFrame(PrevSampler, oneTexel.x * 36) + 1, oneTexel.x * 36);
		else
			gl_FragColor = vec4(PrevTexel.rgb, 1.0);
	}
	else if (texCoord.y <= oneTexel.y && texCoord.x > oneTexel.x * 72 && texCoord.x <= oneTexel.x * 108) {
		if (Time < 0.9)
			gl_FragColor = vec4(PrevTexel.rgb, 1.0);
		else
			gl_FragColor = texture2D(PrevSampler, vec2(texCoord.x - oneTexel.x * 36, texCoord.y));
	}
	else {
		float fps_rate = getFrame(PrevSampler, oneTexel.x * 72) / 0.8 / 60;
		if (fps_rate < 0)
			fps_rate = 1.0;
		
		vec4 judgeTexel = texture2D(DiffuseSampler, vec2(0.5, 0.5));
		if (judgeTexel.r > 0.976 && judgeTexel.g < 0.003 && judgeTexel.b < 0.003) {
			if (texCoord.y <= oneTexel.y && texCoord.x <= oneTexel.x * 36)
				recordColor(0, 0.0);
			else
				gl_FragColor = vec4(PrevTexel.rgb, 1.0);
		}
		else {
			int frame = getFrame(PrevSampler, 0.0);
			vec4 CurrTexel = texture2D(DiffuseSampler, texCoord);
			
			if (frame >= 0) {
				if (frame < 109 * fps_rate) {
					if (texCoord.y <= oneTexel.y && texCoord.x <= oneTexel.x * 36)
						recordColor(frame + 1, 0.0);
					else if (frame > 0)
						theWorld(int(frame / fps_rate), CurrTexel);
					else
						gl_FragColor = vec4(CurrTexel.rgb, 1.0);
				}
				else if (frame < 541 * fps_rate) {
					if (texCoord.y <= oneTexel.y && texCoord.x <= oneTexel.x * 36)
						recordColor(frame + 1, 0.0);
					else {
						float gray = (CurrTexel.r + CurrTexel.g * 2 + CurrTexel.b) / 4;
						vec3 faded = vec3((CurrTexel.r + gray * 4) / 5, (CurrTexel.g + gray * 4) / 5, (CurrTexel.b + gray * 4) / 5);
						gl_FragColor = vec4(faded, 1.0);
					}
				}
				else if (frame < 561 * fps_rate) {
					if (texCoord.y <= oneTexel.y && texCoord.x <= oneTexel.x * 36)
						recordColor(frame + 1, 0.0);
					else {
						float gray = (CurrTexel.r + CurrTexel.g * 2 + CurrTexel.b) / 4;
						vec3 faded = vec3((CurrTexel.r * (int(frame / fps_rate) - 536) + gray * (561 - int(frame / fps_rate))) / 25, (CurrTexel.g * (int(frame / fps_rate) - 536) + gray * (561 - int(frame / fps_rate))) / 25, (CurrTexel.b * (int(frame / fps_rate) - 536) + gray * (561 - int(frame / fps_rate))) / 25);
						gl_FragColor = vec4(faded, 1.0);
					}
				}
				else {
					gl_FragColor = vec4(CurrTexel.rgb, 1.0);
				}
			}
			else {
				if (CurrTexel.g < 0.03 && CurrTexel.b < 0.03)
					gl_FragColor = vec4(PrevTexel.rgb, 1.0);
				else
					gl_FragColor = vec4(CurrTexel.rgb, 1.0);
			}
		}
	}
}
