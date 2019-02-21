
public class DiamondSquare {
	public static void main(String[] args) {
		DiamondSquare test = new DiamondSquare();
		
		int sideLength = 9;
		
		int[][] map = test.genMap(sideLength);
		
		for(int x = 0; x < sideLength; x++) {
			for(int y = 0; y < sideLength; y++) {
				System.out.printf("%4d", map[x][y]);
			}
			System.out.print("\n");
		}
		
	}
	
	public int[][] genMap(int sideLength) {
		if(sideLength % 2 == 0) sideLength++;
		
		int[][] baseMap = new int[sideLength][sideLength];
		
		int randomRange = 255;
		
		//initialize first four corners
		baseMap[0][0] = (int)(Math.random() * randomRange);
		baseMap[sideLength - 1][0] = (int)(Math.random() * randomRange);
		baseMap[0][sideLength - 1] = (int)(Math.random() * randomRange);
		baseMap[sideLength - 1][sideLength - 1] = (int)(Math.random() * randomRange);
		
		int step = sideLength - 1;
		
		boolean isSquareStep = true; //operator to do at step, true = square step, false = diamond step
		
		while(step > 1) {
			int halfStep = step / 2;
			
			if(isSquareStep) {
				for(int x = halfStep; x < sideLength - 1; x += step) {
					for(int y = halfStep; y < sideLength - 1; y += step) {
						baseMap[x][y] = squareStep(baseMap, x, y, step, (int)(Math.random() * randomRange) - (randomRange / 2));
					}
				}
			}
			else {
				for (int y = 0; y < sideLength - 1; y += step) {
					for (int x = 0; x < sideLength - 1; x += step) {
						baseMap[x + halfStep][y] = diamondStep(baseMap, x + halfStep, y, step, (int)(Math.random() * randomRange) - (randomRange / 4));
						baseMap[x][y + halfStep] = diamondStep(baseMap, x, y + halfStep, step, (int)(Math.random() * randomRange) - (randomRange / 4));
					}
				}
				step /= 2;
				randomRange /= 2;
			}
			
			isSquareStep = !isSquareStep;
		}
		
		return baseMap;
	}
	
	public int squareStep(int[][] map, int x, int y, int step, int random) {
		/* tl			tr
		 * 
		 * 		x,y
		 * 
		 * bl			br
		 */
		
		int halfStep = step / 2;
		
		int[] topLeft = {x - halfStep, y - halfStep};
		int[] topRight = {x + halfStep, y - halfStep};
		int[] botLeft = {x - halfStep, y + halfStep};
		int[] botRight = {x + halfStep, y + halfStep};
		
		topLeft[0] += map.length; topLeft[1] += map[0].length;
		topLeft[0] %= map.length; topLeft[1] %= map[0].length;
		
		botLeft[0] += map.length; botLeft[1] += map[0].length;
		botLeft[0] %= map.length; botLeft[1] %= map[0].length;
		
		topRight[0] += map.length; topRight[1] += map[0].length;
		topRight[0] %= map.length; topRight[1] %= map[0].length;
		
		botRight[0] += map.length; botRight[1] += map[0].length;
		botRight[0] %= map.length; botRight[1] %= map[0].length;
		
		int average = Math.abs(map[topLeft[0]][topLeft[1]] + map[topRight[0]][topRight[1]] + map[botLeft[0]][botLeft[1]] + map[botRight[0]][botRight[1]]) / 4;
		
		return average + (random * 8);
	}
	
	public int diamondStep(int[][] map, int x, int y, int step, int random) {
		//x, y center of diamond
		
		int[] right = {x + (step / 2), y};
		int[] bot = {x, y + (step / 2)};
		int[] top = {x, y - (step / 2)};
		int[] left = {x - (step / 2), y};
		
		//loop around, first line makes sure x or y are not negative, second line loops the values
		top[0] += map.length; top[1] += map[0].length;
		top[0] %= map.length; top[1] %= map[0].length;
		
		left[0] += map.length; left[1] += map[0].length;
		left[0] %= map.length; left[1] %= map[0].length;
		
		right[0] += map.length; right[1] += map[0].length;
		right[0] %= map.length; right[1] %= map[0].length;
		
		bot[0] += map.length; bot[1] += map[0].length;
		bot[0] %= map.length; bot[1] %= map[0].length;
		
		int average = (map[top[0]][top[1]] + map[right[0]][right[1]] + map[left[0]][left[1]] + map[bot[0]][bot[1]]) / 4;
		
		return average + (random * 8);
	}
}
