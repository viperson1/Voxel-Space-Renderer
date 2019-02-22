
public class smoothNoise {
	
	
	public static double[][] genRandomNoise(int width) {
		double[][] noise = new double[width][width];
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < width; y++) {
				noise[x][y] = (Math.random());
			}
		}
		
		return noise;
	}
	
	public static double getSmoothNoise(double[][] noiseMap, double x, double y, double size) {
		double value = 0.0;
		double baseSize = size;
		
		while(size > 1) {
			value += getSmoothNoisePixel(noiseMap, x / size, y / size);
			size /= 2;
		}
		
		return ((value / baseSize));
	}
	
	public static double getSmoothNoisePixel(double[][] noiseMap, double x, double y) {
		double fractX = x - (int)x;
		double fractY = y - (int)y;
		
		int x1 = ((int)x + noiseMap.length) % noiseMap.length;
		int y1 = ((int)x + noiseMap.length) % noiseMap.length;
		
		int x2 = (x1 + noiseMap.length - 1) % noiseMap.length;
		int y2 = (y1 + noiseMap.length - 1) % noiseMap.length;
		
		double value = 0.0;
		
		value += fractX * fractY * noiseMap[x1][y1];
		value += (1 - fractX) * fractY * noiseMap[x2][y1];
		value += fractX * (1 - fractY) * noiseMap[x1][y2];
		value += (1 - fractX) * (1 - fractY) * noiseMap[x2][y2];
		
		return value;
 	}
}
