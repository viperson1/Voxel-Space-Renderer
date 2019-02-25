import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class smoothNoise {
	public static void main(String[] args) throws IOException {
		BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
		
		double[][] noise = genRandomNoise(512);
		
		int[] buffer = new int[512 * 512];
		for(int x = 0; x < 512; x++) {
			for(int y = 0; y < 512; y++) {
				double val = getSmoothNoise(noise, x, y, 16) * 8.0;
				
				if(val > 255) val = 255;
				if(val < 0) val = 0;
				buffer[(y * 512) + x] = new Color((int)val, (int)val, (int)val).getRGB();
			}
		}
		
		image.setRGB(0, 0, 512, 512, buffer, 0, 512);
		
		ImageIO.write(image, "png", new File("output.png"));
	}
	
	public static double[][] genRandomNoise(int width) {
		double[][] noise = new double[width][width];
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				noise[x][y] = (Math.random() * 16.0);
			}
		}
		
		return noise;
	}
	
	public static double getSmoothNoise(double[][] noiseMap, double x, double y, double size) {
		double value = 0.0;
		double baseSize = size;
		
		while(size > 1) {
			value += getSmoothNoisePixel(noiseMap, x / size, y / size) * size;
			size /= 2;
		}
		
		return ((value / baseSize));
	}
	
	public static double getSmoothNoisePixel(double[][] noiseMap, double x, double y) {
		double fractX = x - (int)(x);
		double fractY = y - (int)(y);
		
		int x1 = ((int)(x) + noiseMap.length) % noiseMap.length;
		int y1 = ((int)(y) + noiseMap.length) % noiseMap.length;
		
		int x2 = (x1 + noiseMap.length - 1) % noiseMap.length;
		int y2 = (y1 + noiseMap.length - 1) % noiseMap.length;
		
		double value = 0.0;
		
		value += (fractX * fractY) * noiseMap[x1][y1];
		value += ((1 - fractX) * fractY) * noiseMap[x2][y1];
		value += (fractX * (1 - fractY)) * noiseMap[x1][y2];
		value += ((1 - fractX) * (1 - fractY)) * noiseMap[x2][y2];
		
		return value;
 	}
}
