import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
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
	int darkLevel;
	final int drawDist;
	final double heightScale;
	final double objectHeightScale;
	
	KeyListener input;
	
	File inputHeightMap;
	File inputColorMap;
	BufferedImage imageHeightMap;
	BufferedImage imageColorMap;
	int mapWidth;
	int mapHeight;
	int[][] heightMap;
	HashMap<Integer, StaticObject> objects;
	int objectRotation;
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
		moveSpeed = 30;
		currentSpeed = moveSpeed;
		turnSpeed = 60;
		jumpTime = 0;
		jumpHeight = cameraHeight;
		jumpLength = 1;
		
		horizon = screenHeight / 2;
		drawDist = 800;
		heightScale = 240;
		objectHeightScale = 200;
		
		for(int i = 0; i < 16; i++) {
			testObject1Model[i] = ImageIO.read(new File("testObj/" + (i + 1) + ".png"));
		}
		
		inputHeightMap = new File("D1.png");
		imageHeightMap = ImageIO.read(inputHeightMap);
		inputColorMap = new File("C1W.png");
		imageColorMap = ImageIO.read(inputColorMap);
		mapWidth = imageHeightMap.getWidth();
		mapHeight = imageHeightMap.getHeight();
		heightMap = new int[mapWidth][mapHeight];
		objects = new HashMap();
		objectRotation = 30;
		objectMap = new int[mapWidth][mapHeight];
		colorMap = new int[mapWidth][mapHeight];
		
		for(int x = 0; x < mapWidth; x++) {
			for(int y = 0; y < mapHeight; y++) {
				heightMap[x][y] = evaluatePixel(imageHeightMap, x, y);
				if(new Color(imageHeightMap.getRGB(x, y)).getGreen() != 0 && objectMap[x][y] == 0) {
					objects.put((y * mapWidth) + x, new StaticObject(testObject1Model, objectRotation));
					for(int coordX = 0; coordX < 32; coordX++) {
						for(int coordY = 0; coordY < 32; coordY++) {
							int rotatedX = (int)Math.floor((Math.cos(Math.toRadians(objectRotation)) * coordX) + (-1 * Math.sin(Math.toRadians(objectRotation)) * coordY));
							int rotatedY = (int)Math.floor((Math.sin(Math.toRadians(objectRotation)) * coordX) + (Math.cos(Math.toRadians(objectRotation)) * coordY));
							objectMap[rotatedX + x][rotatedY + y] = (y * mapWidth) + x;
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
	
	public static void main(String[] args) throws IOException, AWTException {
		VoxelSpaceEngine engine = new VoxelSpaceEngine();
		JFrame display = new JFrame("Display");
		display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		display.setSize(engine.screenWidth, engine.screenHeight);
		display.setVisible(true);
		display.setResizable(false);
		display.addKeyListener(engine);
		Robot mouseTransform = new Robot();
		
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
			
			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");
			
			display.setCursor(blankCursor);
			
			int mousePosX = MouseInfo.getPointerInfo().getLocation().x - display.getLocationOnScreen().x;
			int mousePosY = MouseInfo.getPointerInfo().getLocation().y - display.getLocationOnScreen().y;
			
			display.getGraphics().drawString("" + (1 / engine.elapsedTime), 100, 100);
			
			engine.checkingKeys = true;
			for(int key : engine.keysPressed) {
				switch(key) {
				case KeyEvent.VK_EQUALS:
					engine.darkLevel += 50 * engine.elapsedTime;
					if(engine.darkLevel > 100) engine.darkLevel = 0;
					break;
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
				case KeyEvent.VK_ESCAPE:
					running = false;
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
			
			engine.direction -= 0.25 * (mousePosX - (engine.screenWidth / 2));
			engine.horizon -= (mousePosY - (engine.screenHeight / 2));
			
			mouseTransform.mouseMove(display.getLocationOnScreen().x + (engine.screenWidth / 2), display.getLocationOnScreen().y + (engine.screenHeight / 2));
			
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
		int skyColor = new Color(0, 100 - darkLevel, 200 - darkLevel).getRGB();
		
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
				
				Color mapColor = new Color(colorMap[loopedX][loopedY]);
				int mapColorRDarkened = (int)(mapColor.getRed() - (darkLevel * 1.2));
				if(mapColorRDarkened < 0) mapColorRDarkened = 0;
				int mapColorGDarkened = mapColor.getGreen() - darkLevel;
				if(mapColorGDarkened < 0) mapColorGDarkened = 0;
				int mapColorBDarkened = (int)(mapColor.getBlue() - (darkLevel * 0.80));
				if(mapColorBDarkened < 0) mapColorBDarkened = 0;
				
				Color mapColorDarkened = new Color(mapColorRDarkened, mapColorGDarkened, mapColorBDarkened);
				
				for(int row = heightOnScreen; row > yBuffer[column]; row--) {
					if(tempFrame[(((screenHeight - row) * screenWidth) + column)] == skyColor)
						tempFrame[(((screenHeight - row) * screenWidth) + column)] = mapColorDarkened.getRGB();
				}
				if(yBuffer[column] < heightOnScreen) yBuffer[column] = heightOnScreen;
				
				boolean[] objectColumn;
				if(objectMap[loopedX][loopedY] != 0) {
					int objectOrigin = objectMap[loopedX][loopedY];
					int relativePointX = (loopedX - (objectOrigin % mapWidth));
					int relativePointY = (int)(loopedY - Math.floor(objectOrigin / mapWidth));
					objectColumn = objects.get(objectOrigin).getColumn(relativePointX, relativePointY);
					for(int i = 0; i < 16; i++) {
						if(objectColumn[i]) {
							int startHeight = screenHeight - (int)((((posZ + cameraHeight) - (heightMap[loopedX][loopedY] + (i - 0.5))) / layer * objectHeightScale + horizon));
							int endHeight = screenHeight - (int)((((posZ + cameraHeight) - (heightMap[loopedX][loopedY] + (i + 0.5))) / layer * objectHeightScale + horizon));
							
							//fix any boundary issues
							if(startHeight < yBuffer[column]) startHeight = yBuffer[column];
							if(endHeight < yBuffer[column]) endHeight = yBuffer[column];
							if(startHeight > screenHeight) startHeight = screenHeight;
							else if(startHeight <= 0) startHeight = 1;
							if(endHeight > screenHeight) endHeight = screenHeight;
							else if(endHeight <= 0) endHeight = 1;
							
							if(endHeight - startHeight > 0) {
								for(int height = startHeight; height < endHeight; height++) {
									if(tempFrame[(((screenHeight - height) * screenWidth) + column)] == skyColor)
										tempFrame[(((screenHeight - height) * screenWidth) + column)] = Color.gray.getRGB();
								}
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
		if(!checkingKeys)keysPressed.add(in.getKeyCode());
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