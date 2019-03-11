package com.gnarly.engine.shaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.joml.Vector4f;

public class LevelEffect {

	private double length;
	private Subeffect[] effects;
	
	public LevelEffect(String path) {
		try {
			Scanner scanner = new Scanner(new FileInputStream(new File(path)));
			int numEffects = scanner.nextInt();
			effects = new Subeffect[numEffects];
			for (int i = 0; i < effects.length; i++) {
				double lLength = scanner.nextDouble();
				boolean rgb = scanner.nextBoolean();
				length += lLength;
				effects[i] = new Subeffect(
					lLength, rgb, scanner.nextBoolean(), scanner.nextFloat(), scanner.nextFloat(),
					new Vector4f(scanner.nextFloat() / (rgb ? 1 : 360f), scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()),
					new Vector4f(scanner.nextFloat() / (rgb ? 1 : 360f), scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()),
					new Vector4f(scanner.nextFloat() / (rgb ? 1 : 360f), scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()),
					new Vector4f(scanner.nextFloat() / (rgb ? 1 : 360f), scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat())
				);
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public EffectPayload getPayload(double time) {
		double rtime = time % length;
		for (int i = 0; rtime > 0; ++i) {
			rtime -= effects[i].getLength();
			if (rtime <= 0)
				return effects[i].getPayload(rtime + effects[i].getLength());
		}
		//Should never reach here
		System.out.println("Program? Here!? Ha! Impossible! No program can make it pass the payload retrieval loop!");
		return null;
	}
	
	private class Subeffect {
		
		private double length;
		private boolean rgb, loop;
		private float timeScale, freq;
		private Vector4f c1, c2, c3, c4;
		
		public Subeffect(double length, boolean rgb, boolean loop, float timeScale, float freq, Vector4f color1, Vector4f color2, Vector4f color3, Vector4f color4) {
			this.length = length;
			this.rgb = rgb;
			this.loop = loop;
			this.timeScale = timeScale;
			this.freq = freq;
			c1 = color1;
			c2 = color2;
			c3 = color3;
			c4 = color4;
		}
		
		public EffectPayload getPayload(double time) {
			float percent = (float) (time / length);
			return new EffectPayload(
				c1.add(c3.sub(c1, new Vector4f()).mul(percent), new Vector4f()),
				c2.add(c4.sub(c2, new Vector4f()).mul(percent), new Vector4f()),
				(float) (timeScale * time / length), freq, rgb, loop
			);
		}
		
		public double getLength() {
			return length;
		}
	};
	
	public class EffectPayload {
		
		public Vector4f c1;
		public Vector4f c2;
		public float time, freq;
		public boolean rgb, loop;
		
		public EffectPayload(Vector4f color1, Vector4f color2, float time, float freq, boolean rgb, boolean loop) {
			this.c1 = color1;
			this.c2 = color2;
			this.time = time;
			this.freq = freq;
			this.rgb = rgb;
			this.loop = loop;
		}
	};
}
