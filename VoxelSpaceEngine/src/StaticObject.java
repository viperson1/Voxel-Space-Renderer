import java.awt.Color;
import java.awt.image.BufferedImage;

public class StaticObject {
	boolean[][][] objectModel = new boolean[32][32][16];
	int rotation;
	
	StaticObject(BufferedImage[] layers, int rotation) {
		this.rotation = rotation;
		for(int layer = 0; layer < 16; layer++) {
			for(int x = 0; x < 32; x++) {
				for(int y = 0; y < 32; y++) {
					objectModel[x][y][layer] = new Color(layers[layer].getRGB(x, y)).getRed() != 0;
				}
			}
		}
	}
	
	public boolean[] getColumn(int x, int y) {
		int unRotatedX = (int)((Math.cos(Math.toRadians(rotation)) * x) + (Math.sin(Math.toRadians(rotation)) * y));
		int unRotatedY = (int)(-1 * (Math.sin(Math.toRadians(rotation)) * x) + (Math.cos(Math.toRadians(rotation)) * y));
		return objectModel[unRotatedX][unRotatedY];
	}
}
