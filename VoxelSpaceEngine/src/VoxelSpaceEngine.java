import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
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
	double heightScale;
	double objectHeightScale;
	
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
	
	final int screenHeight;
	final int screenWidth;
	final double renderScale;
	int renderedScreenWidth;
	int renderedScreenHeight;
	int FOV;
	double fovScale;
	
	double elapsedTime;
	boolean checkingKeys;
	final boolean fpsMLook;
	
	static BufferedImage[] testObject1Model = new BufferedImage[16];
	
	VoxelSpaceEngine() throws IOException {
		screenWidth = 640;
		screenHeight = 360;
		
		renderScale = 1;
		
		renderedScreenWidth = (int)(screenWidth * renderScale);
		renderedScreenHeight = (int)(screenHeight * renderScale);
		
		horizon = renderedScreenHeight * 0.5;
		drawDist = 1024;
		heightScale = 127;
		objectHeightScale = 127;
		
		//for(int i = 0; i < 16; i++) {
		//	testObject1Model[i] = ImageIO.read(new File("testObj/" + (i + 1) + ".png"));
		//}
		
		inputHeightMap = new File("Maps/D20.png");
		imageHeightMap = ImageIO.read(inputHeightMap);
		inputColorMap = new File("Maps/C20W.png");
		imageColorMap = ImageIO.read(inputColorMap);
		mapWidth = imageHeightMap.getWidth();
		mapHeight = imageHeightMap.getHeight();
		heightMap = new int[mapWidth][mapHeight];
		objects = new HashMap<Integer, StaticObject>();
		objectRotation = 30;
		objectMap = new int[mapWidth][mapHeight];
		colorMap = new int[mapWidth][mapHeight];
		
		posX = 512;
		posY = 512;
		
		cameraHeight = 8;
		direction = 90;
		
		fallingMoveSpeed = 40;
		moveSpeed = 60;
		currentSpeed = moveSpeed;
		turnSpeed = 100;
		jumpTime = 0;
		jumpHeight = cameraHeight * 2;
		jumpLength = 1;
		fpsMLook = true;
		FOV = 90;
		fovScale = FOV / 90.0;
		
		for(int x = 0; x < mapWidth; x++) {
			for(int y = 0; y < mapHeight; y++) {
				heightMap[x][y] = evaluatePixel(imageHeightMap, x, y);
				/*if(new Color(imageHeightMap.getRGB(x, y)).getGreen() != 0 && objectMap[x][y] == 0) {
					objects.put((y * mapWidth) + x, new StaticObject(testObject1Model, objectRotation));
					for(int coordX = 0; coordX < 32; coordX++) {
						for(int coordY = 0; coordY < 32; coordY++) {
							int rotatedX = (int)Math.floor((Math.cos(Math.toRadians(objectRotation)) * coordX) + (-1 * Math.sin(Math.toRadians(objectRotation)) * coordY));
							int rotatedY = (int)Math.floor((Math.sin(Math.toRadians(objectRotation)) * coordX) + (Math.cos(Math.toRadians(objectRotation)) * coordY));
							objectMap[rotatedX + x][rotatedY + y] = (y * mapWidth) + x;
						}
					}
				}*/
				colorMap[x][y] = imageColorMap.getRGB(x, y);
				
				//if(Math.random() * 100 < 5) {
					//heightMap[x][y] += 2;
					//colorMap[x][y] = new Color(90, 140, 50).getRGB();
				//}
			}
		}
		inputHeightMap = null;
		inputColorMap = null;
		imageHeightMap = null;
		imageColorMap = null;
		
		posZ = 255;
	}
	
	public static void main(String[] args) throws IOException, AWTException {
		VoxelSpaceEngine engine = new VoxelSpaceEngine();
		JFrame display = new JFrame("Display");
		display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		display.setSize(engine.renderedScreenWidth, engine.renderedScreenHeight);
		display.setVisible(true);
		display.setResizable(false);
		display.addKeyListener(engine);
		Robot mouseTransform = new Robot();
		
		BufferedImage frame = new BufferedImage(engine.renderedScreenWidth, engine.renderedScreenHeight, BufferedImage.TYPE_INT_ARGB);
		
		boolean running = true;
		double currentTime = System.nanoTime();
		
		double originalJumpPosZ = 0;
		
		int frameNum = 0;
		
		double countingElapsedTime = 0;
		double avgFPS = 0;
		
		while(running) { //game loop
			frame.setRGB(0, 0, engine.renderedScreenWidth, engine.renderedScreenHeight, engine.renderFrame(), 0, engine.renderedScreenWidth);
			frame.getGraphics().drawString("" + Math.round(1 / (avgFPS)), 100, 100);
			
			if(frameNum == 60) {
				avgFPS = countingElapsedTime / 60;
				countingElapsedTime = 0;
				frameNum = 0;
			}
			
			frameNum++;
			
			display.getGraphics().drawImage(frame, 0, 0, null);			
			engine.elapsedTime = ((System.nanoTime() - currentTime) * .000000001);
			
			countingElapsedTime += engine.elapsedTime;
			
			currentTime = System.nanoTime();
			
			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");
			
			display.setCursor(blankCursor);
			
			int mousePosX = MouseInfo.getPointerInfo().getLocation().x - display.getLocationOnScreen().x;
			int mousePosY = MouseInfo.getPointerInfo().getLocation().y - display.getLocationOnScreen().y;
			
			
			engine.checkingKeys = true;
			for(int key : engine.keysPressed) {
				switch(key) {
				/*case KeyEvent.VK_EQUALS:
					engine.darkLevel += 50 * engine.elapsedTime;
					if(engine.darkLevel > 100) engine.darkLevel = 0;
					break;
				case KeyEvent.VK_UP:
					engine.horizon += Math.tan(Math.toRadians(engine.turnSpeed)) * (engine.renderedScreenHeight / 2) * engine.elapsedTime;
					if(engine.horizon > engine.renderedScreenHeight) engine.horizon = engine.renderedScreenHeight;
					break;
				case KeyEvent.VK_DOWN:
					engine.horizon -= Math.tan(Math.toRadians(engine.turnSpeed)) * (engine.renderedScreenHeight / 2) * engine.elapsedTime;
					if(engine.horizon < 0) engine.horizon = 0;
					break;
				case KeyEvent.VK_LEFT:
					engine.direction += engine.turnSpeed * engine.elapsedTime;
					break;
				case KeyEvent.VK_RIGHT:
					engine.direction -= engine.turnSpeed * engine.elapsedTime;
					break;*/
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
					}
					break;
				}
			}
			engine.checkingKeys = false;
			
			
			
			if(!engine.fpsMLook) {
				double distFromCenter = Math.abs((engine.screenWidth / 2) - mousePosX) + Math.abs((engine.screenHeight / 2) - mousePosY);
				double maxDistFromCenter = 100;
				
				double maxDistFromX = ((engine.screenWidth / 2) - mousePosX) * (maxDistFromCenter / distFromCenter);
				double maxDistFromY = ((engine.screenHeight / 2) - mousePosY) * (maxDistFromCenter / distFromCenter);
				
				if(distFromCenter > maxDistFromCenter) {
					engine.direction -= 0.06125 * (mousePosX - (((engine.screenWidth / 2)) - maxDistFromX));
					engine.horizon -= 0.25 * (mousePosY - (((engine.screenHeight / 2)) - maxDistFromY));
					mouseTransform.mouseMove((display.getLocationOnScreen().x + (engine.screenWidth / 2)) - (int)maxDistFromX, display.getLocationOnScreen().y + (engine.screenHeight / 2) - (int)maxDistFromY);
				}
			}
			else {
				engine.direction -= 0.25 * (mousePosX - (engine.screenWidth * .5));
				engine.horizon -= (mousePosY - (engine.screenHeight * .5));
				mouseTransform.mouseMove((int)(display.getLocationOnScreen().x + (engine.screenWidth * .5)), (int)(display.getLocationOnScreen().y + (engine.screenHeight * .5)));
			}
			
			if(engine.horizon < 0) engine.horizon = 0;
			if(engine.horizon > engine.renderedScreenHeight) engine.horizon = engine.renderedScreenHeight;
			if(engine.direction > 360) engine.direction -= 360;
			if(engine.direction <= 0) engine.direction += 360;
			
			//mouseTransform.mouseMove(display.getLocationOnScreen().x + (engine.screenWidth / 2), display.getLocationOnScreen().y + (engine.screenHeight / 2));
			
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
	
	int[] renderFrameByLayer() {
		int[] tempFrame = new int[renderedScreenWidth * renderedScreenHeight];
		int skyColor = new Color(0, 100 - darkLevel, 150 - darkLevel).getRGB();
		
		for(int i = 0; i < renderedScreenWidth * renderedScreenHeight; i++) tempFrame[i] = skyColor;
		
		double sinViewAngle = Math.sin(Math.toRadians(direction));
		double cosViewAngle = Math.cos(Math.toRadians(direction));
		
		int[] yBuffer = new int[renderedScreenWidth];
		
		double dZ = 1.0;
		for(int layer = 6; layer < drawDist; layer += dZ) {
			double posXLeft = (-cosViewAngle*layer - sinViewAngle*layer) + posX;
			double posYLeft = (sinViewAngle*layer - cosViewAngle*layer) + posY;
			
			double posXRight = (cosViewAngle*layer - sinViewAngle*layer) + posX;
			double posYRight = (-sinViewAngle*layer - cosViewAngle*layer) + posY;
			
			double dx = (posXRight - posXLeft) / renderedScreenWidth;
			double dy = (posYRight - posYLeft) / renderedScreenWidth;
			
			for(int column = 0; column < renderedScreenWidth; column++) {
				if(posXLeft < mapWidth && posXLeft >= 0 && posYLeft < mapHeight && posYLeft >= 0) {
					int loopedX = (int)posXLeft; // = (int)(((mapWidth * 4) + posXLeft) % mapWidth);
					int loopedY = (int)posYLeft; // =  (int)(((mapHeight * 4) + posYLeft) % mapHeight);
					
					int heightOnScreen = renderedScreenHeight - (int)(((posZ + cameraHeight) - heightMap[loopedX][loopedY]) / layer * heightScale + horizon);
					
					if(heightOnScreen < 0) heightOnScreen = 1;
					if(heightOnScreen > renderedScreenHeight) heightOnScreen = renderedScreenHeight - 1;
					
					Color mapColor = new Color(colorMap[loopedX][loopedY]);
					/*int mapColorRDarkened = (int)(mapColor.getRed() - (darkLevel * 1.2)); //lighting changing
					if(mapColorRDarkened < 0) mapColorRDarkened = 0;
					int mapColorGDarkened = mapColor.getGreen() - darkLevel;
					if(mapColorGDarkened < 0) mapColorGDarkened = 0;
					int mapColorBDarkened = (int)(mapColor.getBlue() - (darkLevel * 0.80));
					if(mapColorBDarkened < 0) mapColorBDarkened = 0;
					
					mapColor = new Color(mapColorRDarkened, mapColorGDarkened, mapColorBDarkened);*/
					
					/*if(heightOnScreen > yBuffer[column]) { //Tron-like rendering
						tempFrame[(((screenHeight - heightOnScreen) * screenWidth) + column)] = Color.cyan.getRGB();
						if(heightOnScreen < screenHeight && column > 0 && tempFrame[(((screenHeight - heightOnScreen - 1) * screenWidth) + (column - 1))] != Color.green.getRGB()) {
							if(heightOnScreen > yBuffer[column - 1]) 
								for(int row = heightOnScreen; row > yBuffer[column - 1]; row--) {
									if(row < screenHeight) {
										tempFrame[(((screenHeight - row) * screenWidth) + column)] = Color.cyan.getRGB();
									}
								}
							else {
								for(int row = heightOnScreen; row < yBuffer[column - 1]; row++) {
									if(row < screenHeight) {
										tempFrame[(((screenHeight - row) * screenWidth) + column)] = Color.cyan.getRGB();
									}
								}
							}
						}
					}*/
					
	
					
					for(int row = heightOnScreen; row > yBuffer[column]; row--) {
						//if(tempFrame[(((screenHeight - row) * screenWidth) + column)] == skyColor)
							tempFrame[(((renderedScreenHeight - row) * renderedScreenWidth) + column)] = mapColor.getRGB();
					}
					if(yBuffer[column] < heightOnScreen) yBuffer[column] = heightOnScreen;
					
					/*boolean[] objectColumn; //(object rendering, may lower FPS)
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
					else objectColumn = null;*/
				}
				posXLeft += dx;
				posYLeft += dy;
			}
			if(layer < 400) dZ += 0.02;
			else dZ += 0.05;
		}
		return tempFrame;
	}
	
	int[] renderFrame() {
		int[] tempFrame = new int[renderedScreenWidth * renderedScreenHeight];
		int skyColor = new Color(0, 100 - darkLevel, 150 - darkLevel).getRGB();
		
		for(int i = 0; i < renderedScreenWidth * renderedScreenHeight; i++) tempFrame[i] = skyColor;
		
		//double dirVectorX = -Math.sin(Math.toRadians(direction));
		//double dirVectorY = -Math.cos(Math.toRadians(direction));
		
		//double[] dir = new double[] {dirVectorX, dirVectorY}; //direction vector, index 0 = x, index 1 = y
		//double[] cameraPlaneRight = new double[] {dirVectorX + fovScale * Math.cos(Math.toRadians(direction)), dirVectorY - fovScale * Math.sin(Math.toRadians(direction))}; //camera plane distance from the direction vector, if Y is the same as direction vector there will be a 90 degree FOV
		//double[] cameraPlaneLeft = new double[] {dirVectorX - fovScale * Math.cos(Math.toRadians(direction)), dirVectorY + fovScale * Math.sin(Math.toRadians(direction))};		
		
		for(int column = 0; column < renderedScreenWidth; column++) {
			double rayDeg = ((((double)column / (double)renderedScreenWidth) * ((direction - (FOV * 0.5)) - (direction + (FOV * 0.5)))) + (direction + (FOV / 2)));
			
			double[] rayDir = new double[] {-Math.sin(Math.toRadians(rayDeg)), -Math.cos(Math.toRadians(rayDeg))};//{(((double)column / (double)renderedScreenWidth) * (cameraPlaneRight[0] - cameraPlaneLeft[0])) + cameraPlaneLeft[0], (((double)column / (double)renderedScreenWidth) * (cameraPlaneRight[1] - cameraPlaneLeft[1])) + cameraPlaneLeft[1]};
			
			int yBuffer = 0;
			int heightBuffer = 0;
			
			double checkPosX = posX;
			double checkPosY = posY;
			
			double renderDist = 0;
			
			boolean inBounds = true;
			
			boolean quickCheck = false;
			
			//which box of the map we're in
		    int[] mapSquare = new int[] {(int)(checkPosX), (int)(checkPosY)};
	
		    //length of ray from current position to next x or y-side
		    double sideDistX;
		    double sideDistY;
		    
		    double jumpDist = 1; //dist to jump for the next square, allows adjusting for LOD
	
		    //length of ray from one x or y-side to next x or y-side
		    double deltaDistX = Math.abs(1 / rayDir[0]);
		    double deltaDistY = Math.abs(1 / rayDir[1]);
		      
		    //which direction to step in
		    int stepX;
		    int stepY;
		    
		    int side = 1;
		    
		    if(rayDir[0] < 0) {
		    	stepX = -1;
		    	sideDistX = (checkPosX - mapSquare[0]) * deltaDistX;
		    }
		    else {
		    	stepX = 1;
		        sideDistX = (mapSquare[0] + 1.0 - checkPosX) * deltaDistX;
		    }
		    if(rayDir[1] < 0) {
		    	stepY = -1;
		        sideDistY = (checkPosY - mapSquare[1]) * deltaDistY;
		    }
		    else {
		    	stepY = 1;
		    	sideDistY = (mapSquare[1] + 1.0 - checkPosY) * deltaDistY;
		    }
			
		    //increase check position to next intersection with grid lines	
			while(inBounds) {
				for(int i = 0; i < 1 << (int)(jumpDist - 1); i++) {
					if (sideDistX < sideDistY) {
			          sideDistX += deltaDistX;
			          mapSquare[0] += stepX;
			          side = 0;
			        }
			        else {
			          sideDistY += deltaDistY;
			          mapSquare[1] += stepY;
			          side = 1;
			        }
				}
				
				int testSquareX = (mapSquare[0] >> ((int)(jumpDist) - 1)) << ((int)(jumpDist) - 1);
				int testSquareY = (mapSquare[1] >> ((int)(jumpDist) - 1)) << ((int)(jumpDist) - 1);
				
				if(quickCheck || renderDist > 75) {
					if (side == 0) {
						renderDist = ((testSquareX - checkPosX) + ((1 - (stepX)) * 0.5)) / rayDir[0];
					}
					else {
						renderDist = ((testSquareY - checkPosY) + ((1 - (stepY)) * 0.5)) / rayDir[1];
					}
				}
				else {
					double distX = Math.abs(testSquareX - checkPosX);
					double distY = Math.abs(testSquareY - checkPosY);
					if(distY > distX) {
						renderDist = ((0.41 * distX) + (0.941246 * distY));
					}
					else renderDist = ((0.41 * distY) + (0.941246 * distX));
				}
				
				if(testSquareX >= 0 && testSquareX < mapWidth && testSquareY >= 0 && testSquareY < mapWidth) {
					if(!quickCheck || heightMap[testSquareX][testSquareY] > heightBuffer) {
						int heightOnScreen = renderedScreenHeight - (int)(((posZ + cameraHeight) - heightMap[testSquareX][testSquareY]) / renderDist * heightScale + horizon);
						Color mapColor = new Color(colorMap[testSquareX][testSquareY]);
						
						if(heightOnScreen > renderedScreenHeight) {
							heightOnScreen = renderedScreenHeight;
							inBounds = false;
						}
						else if(heightOnScreen < yBuffer) {
							continue;
						}
						
						for(int row = heightOnScreen; row > yBuffer; row--) {
								tempFrame[(((renderedScreenHeight - row) * renderedScreenWidth) + column)] = mapColor.getRGB();
						}
							yBuffer = heightOnScreen;
						heightBuffer = heightMap[testSquareX][testSquareY];
						quickCheck = quickCheck || heightOnScreen > renderedScreenHeight - horizon;
					}
				}
				else inBounds = false;
				
				jumpDist += .01;
			}
			
		}
		return tempFrame;
	}
	
	int evaluatePixel(BufferedImage image, int x, int y) {
		int sampleX = x;//(image.getWidth() + x) % image.getWidth();
		int sampleY = y;//(image.getHeight() + y) % image.getHeight();
		
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
		//posX = (mapWidth + posX) % mapWidth;
		//posY = (mapHeight + posY) % mapHeight;
		
		if(posX >= mapWidth || posX < 0 || posY >= mapWidth || posY < 0) {
			if(moveDirection == 2 || moveDirection == 4) {
				move(moveDirection - 1, speed);
			}
			else move(moveDirection + 1, speed);
		}
		
		if(posZ < heightMap[(int)posX][(int)posY]) {
			posZ = heightMap[(int)posX][(int)posY];
			if(posZ - lastPosZ > cameraHeight / 4) {
				if(moveDirection == 2 || moveDirection == 4) {
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