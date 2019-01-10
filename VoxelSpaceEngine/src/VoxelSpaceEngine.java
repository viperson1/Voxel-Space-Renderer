import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class VoxelSpaceEngine implements KeyListener {
	
	
	double posX;
	double posY;
	double posZ;
	final double cameraHeight;
	final double moveSpeed;
	double currentSpeed;
	final double fallingMoveSpeed;
	final double turnSpeed;
	double jumpTime;
	final double jumpHeight;
	final double jumpLength;
	
	double direction;
	
	double horizon;
	final int drawDist;
	final double heightScale;
	
	KeyListener input;
	
	File inputHeightMap;
	File inputColorMap;
	BufferedImage imageHeightMap;
	BufferedImage imageColorMap;
	int mapWidth;
	int mapHeight;
	int[][] heightMap;
	HashMap<Integer, object> objects;
	int[][] objectMap;
	int[][] colorMap;
	
	final int screenWidth;
	final int screenHeight;
	
	double elapsedTime;
	boolean checkingKeys;
	
	static BufferedImage[] testObject1Model = new BufferedImage[16];
	
	VoxelSpaceEngine() throws IOException {
		screenWidth = 960;
		screenHeight = 720;
		
		posX = 0;
		posY = 0;
		posZ = 178;
		cameraHeight = 16;
		direction = 90;
		
		fallingMoveSpeed = 10;
		moveSpeed = 50;
		currentSpeed = moveSpeed;
		turnSpeed = 60;
		jumpTime = 0;
		jumpHeight = cameraHeight;
		jumpLength = 1;
		
		horizon = screenHeight / 2;
		drawDist = 800;
		heightScale = 240;
		
		for(int i = 0; i < 16; i++) {
			testObject1Model[i] =ImageIO.read(new File((i + 1) + ".png"));
		}
		
		inputHeightMap = new File("D1.png");
		imageHeightMap = ImageIO.read(inputHeightMap);
		inputColorMap = new File("C1W.png");
		imageColorMap = ImageIO.read(inputColorMap);
		mapWidth = imageHeightMap.getWidth();
		mapHeight = imageHeightMap.getHeight();
		heightMap = new int[mapWidth][mapHeight];
		objects = new HashMap();
		objectMap = new int[mapWidth][mapHeight];
		colorMap = new int[mapWidth][mapHeight];
		
		for(int x = 0; x < mapWidth; x++) {
			for(int y = 0; y < mapHeight; y++) {
				heightMap[x][y] = evaluatePixel(imageHeightMap, x, y);
				if(new Color(imageHeightMap.getRGB(x, y)).getGreen() != 0 && objectMap[x][y] == 0) {
					objects.put((y * mapWidth) + x, new object(testObject1Model));
					for(int coordX = x; coordX < x + 16; coordX++) {
						for(int coordY = y; coordY < y + 16; coordY++) {
							objectMap[coordX][coordY] = (y * mapWidth) + x;
						}
					}
				}
				colorMap[x][y] = imageColorMap.getRGB(x, y);
			}
		}
		inputHeightMap = null;
		inputColorMap = null;
		imageHeightMap = null;
		imageColorMap = null;
	}
	
	public static void main(String[] args) throws IOException {
		VoxelSpaceEngine engine = new VoxelSpaceEngine();
		JFrame display = new JFrame("Display");
		display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		display.setSize(engine.screenWidth, engine.screenHeight);
		display.setVisible(true);
		display.setResizable(false);
		display.addKeyListener(engine);
		
		BufferedImage frame = new BufferedImage(engine.screenWidth, engine.screenHeight, BufferedImage.TYPE_INT_ARGB);
		
		boolean running = true;
		double currentTime = System.currentTimeMillis();
		
		double originalJumpPosZ = 0;
		
		while(running) { //game loop
			frame.setRGB(0, 0, engine.screenWidth, engine.screenHeight, engine.renderFrame(), 0, engine.screenWidth);
			display.getGraphics().drawRect(0, 0, engine.screenWidth, engine.screenHeight);
			display.getGraphics().drawImage(frame, 0, 0, null);			
			engine.elapsedTime = ((System.currentTimeMillis() - currentTime) / 1000);
			currentTime = System.currentTimeMillis();
			
			engine.checkingKeys = true;
			for(int key : engine.keysPressed) {
				switch(key) {
				case KeyEvent.VK_UP:
					engine.horizon += Math.tan(Math.toRadians(engine.turnSpeed)) * (engine.screenHeight / 2) * engine.elapsedTime;
					if(engine.horizon > engine.screenHeight) engine.horizon = engine.screenHeight;
					break;
				case KeyEvent.VK_DOWN:
					engine.horizon -= Math.tan(Math.toRadians(engine.turnSpeed)) * (engine.screenHeight / 2) * engine.elapsedTime;
					if(engine.horizon < 0) engine.horizon = 0;
					break;
				case KeyEvent.VK_LEFT:
					engine.direction += engine.turnSpeed * engine.elapsedTime;
					break;
				case KeyEvent.VK_RIGHT:
					engine.direction -= engine.turnSpeed * engine.elapsedTime;
					break;
				case KeyEvent.VK_W:
					engine.move(1, engine.currentSpeed);
					break;
				case KeyEvent.VK_S:
					engine.move(2, engine.currentSpeed);	
					break;
				case KeyEvent.VK_A:
					engine.move(3, engine.currentSpeed);
					break;
				case KeyEvent.VK_D:
					engine.move(4, engine.currentSpeed);
					break;
				case KeyEvent.VK_SPACE:
					if(engine.jumpTime == 0 && engine.posZ == engine.heightMap[(int)engine.posX][(int)engine.posY]) {
						originalJumpPosZ = (int) engine.posZ;
						engine.jumpTime = engine.jumpLength;
						break;
					}
				}
			}
			engine.checkingKeys = false;
			
			//jumping
			if(engine.jumpTime > 0) {
					engine.posZ = (((-engine.jumpHeight * Math.pow(engine.jumpTime - (engine.jumpLength / 2), 2) * 4) + engine.jumpHeight) + originalJumpPosZ);
					engine.jumpTime -= engine.elapsedTime;
					if(engine.posZ < engine.heightMap[(int)engine.posX][(int)engine.posY]) {
						engine.posZ = engine.heightMap[(int)engine.posX][(int)engine.posY];
						engine.jumpTime = 0;
				}
			}
			else if(engine.jumpTime < 0) engine.jumpTime = 0;
			
			//gravity and move speed slow while falling
			if(engine.posZ > (engine.heightMap[(int)engine.posX][(int)engine.posY]) && engine.jumpTime == 0) {
				engine.posZ -= ((engine.cameraHeight / 2) * 10) * engine.elapsedTime;
				if(engine.currentSpeed > engine.fallingMoveSpeed) {
					engine.currentSpeed -= (engine.moveSpeed - engine.fallingMoveSpeed) / 50;
				}
				if(engine.posZ < engine.heightMap[(int)engine.posX][(int)engine.posY]) engine.posZ = engine.heightMap[(int)engine.posX][(int)engine.posY];
			}
			else if(engine.currentSpeed != engine.moveSpeed) {
				engine.currentSpeed = engine.moveSpeed;
			}
		}
	}
	
	int[] renderFrame() {
		int[] tempFrame = new int[screenWidth * screenHeight];
		int skyColor = Color.cyan.getRGB();
		
		for(int i = 0; i < screenWidth * screenHeight; i++) tempFrame[i] = skyColor;
		
		double sinViewAngle = Math.sin(Math.toRadians(direction));
		double cosViewAngle = Math.cos(Math.toRadians(direction));
		
		int[] yBuffer = new int[screenWidth];
		
		double dZ = 1.0;
		for(int layer = 1; layer < drawDist; layer += dZ) {
			double posXLeft = (-cosViewAngle*layer - sinViewAngle*layer) + posX;
			double posYLeft = (sinViewAngle*layer - cosViewAngle*layer) + posY;
			
			double posXRight = (cosViewAngle*layer - sinViewAngle*layer) + posX;
			double posYRight = (-sinViewAngle*layer - cosViewAngle*layer) + posY;
			
			double dx = (posXRight - posXLeft) / screenWidth;
			double dy = (posYRight - posYLeft) / screenWidth;
			
			for(int column = 0; column < screenWidth; column++) {
				int loopedX = (int)(((mapWidth * 4) + posXLeft) % mapWidth);
				int loopedY = (int)(((mapHeight * 4) + posYLeft) % mapHeight);
				
				int heightOnScreen = screenHeight - (int)(((posZ + cameraHeight) - heightMap[loopedX][loopedY]) / layer * heightScale + horizon);
				
				if(heightOnScreen < 0) heightOnScreen = 0;
				if(heightOnScreen > screenHeight) heightOnScreen = screenHeight;
				
				for(int row = heightOnScreen; row > yBuffer[column]; row--) {
					if(tempFrame[(((screenHeight - row) * screenWidth) + column)] == Color.cyan.getRGB())
						tempFrame[(((screenHeight - row) * screenWidth) + column)] = colorMap[loopedX][loopedY];
				}
				if(yBuffer[column] < heightOnScreen) yBuffer[column] = heightOnScreen;
				
				boolean[] objectColumn;
				if(objectMap[loopedX][loopedY] != 0) {
					int objectOrigin = objectMap[loopedX][loopedY];
					int objectOriginX = objectOrigin % mapWidth;
					int objectOriginY = (int) Math.floor(objectOrigin / mapWidth);
					int relativePointX = loopedX - objectOriginX;
					int relativePointY = loopedY - objectOriginY;
					objectColumn = objects.get(objectOrigin).getColumn(relativePointX, relativePointY);
					for(int i = 0; i < 16; i++) {
						if(objectColumn[i]) {
							int startHeight = screenHeight - (int)((((posZ + cameraHeight) - (heightMap[loopedX][loopedY] + (i - 0.5))) / layer * heightScale + horizon));
							int endHeight = screenHeight - (int)((((posZ + cameraHeight) - (heightMap[loopedX][loopedY] + (i + 0.5))) / layer * heightScale + horizon));
							if(startHeight > screenHeight) startHeight = screenHeight;
							if(startHeight < 0) startHeight = 0;
							if(endHeight > screenHeight) endHeight = screenHeight;
							if(endHeight < 0) endHeight = 0;
							for(int height = startHeight; height < endHeight; height++) {
								if(tempFrame[(((screenHeight - height) * screenWidth) + column)] == Color.cyan.getRGB())
									tempFrame[(((screenHeight - height) * screenWidth) + column)] = Color.black.getRGB();
							}
						}
					}
				}
				else objectColumn = null;
				
				posXLeft += dx;
				posYLeft += dy;
			}
			dZ += 0.01;
		}
		return tempFrame;
	}
	
	int evaluatePixel(BufferedImage image, int x, int y) {
		int sampleX = (image.getWidth() + x) % image.getWidth();
		int sampleY = (image.getHeight() + y) % image.getHeight();
		
		int color = image.getRGB(sampleX, sampleY);
		int redChannel = (new Color(color).getRed());
		return redChannel;
	}
	
	void move(int moveDirection, double speed) {
		double lastPosZ = posZ;
		switch(moveDirection) {
			case 1: //forward
				posX -= Math.sin(Math.toRadians(direction)) * speed * elapsedTime;
				posY -= Math.cos(Math.toRadians(direction)) * speed * elapsedTime;
				break;
			case 2: //back
				posX += Math.sin(Math.toRadians(direction)) * speed * elapsedTime;
				posY += Math.cos(Math.toRadians(direction)) * speed * elapsedTime;
				break;
			case 3: //left
				posX -= Math.cos(Math.toRadians(direction)) * speed * elapsedTime;
				posY += Math.sin(Math.toRadians(direction)) * speed * elapsedTime;
				break;
			case 4: //right
				posX += Math.cos(Math.toRadians(direction)) * speed * elapsedTime;
				posY -= Math.sin(Math.toRadians(direction)) * speed * elapsedTime;
				break;
		}
		posX = (mapWidth + posX) % mapWidth;
		posY = (mapHeight + posY) % mapHeight;
		if(posZ < heightMap[(int)posX][(int)posY]) {
			posZ = heightMap[(int)posX][(int)posY];
			if(posZ - lastPosZ > cameraHeight / 4) {
				if(moveDirection % 2 == 0) {
					move(moveDirection - 1, speed);
				}
				else move(moveDirection + 1, speed);
				posZ = lastPosZ;
			}
		}
	}

	Set<Integer> keysPressed = new HashSet<>();
	
	public void keyPressed(KeyEvent in) {
		keysPressed.add(in.getKeyCode());
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if(!checkingKeys)keysPressed.remove(e.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}