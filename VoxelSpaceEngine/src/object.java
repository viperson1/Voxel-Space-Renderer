import java.awt.Color;
import java.awt.image.BufferedImage;

public class object {
	boolean[][][] objectModel = new boolean[16][16][16];
	
	object(BufferedImage[] layers) {
		for(int layer = 0; layer < 16; layer++) {
			for(int x = 0; x < 16; x++) {
				for(int y = 0; y < 16; y++) {
					objectModel[x][y][layer] = new Color(layers[layer].getRGB(x, y)).getRed() != 0;
				}
			}
		}
	}
	
	public boolean[] getColumn(int x, int y) {
		return objectModel[x][y];
	}
}
