import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opencl.CL;
import org.lwjgl.opengl.GL;

public class VoxelSpaceEngine {
	
	
	float posX;
	float posY;
	float posZ;
	float cameraPosX;
	float cameraPosY;
	float cameraPosZ;
	double cameraDistance;
	final float cameraHeight;
	final double moveSpeed;
	double currentSpeed;
	final double fallingMoveSpeed;
	double fallSpeed;
	double fallAccel;
	final double turnSpeed;
	double jumpTime;
	final double jumpHeight;
	final double jumpLength;
	double originalJumpPosZ;
	
	float direction;
	
	float horizon;
	int darkLevel;
	final int drawDist;
	float heightScale;
	double objectHeightScale;
	
	KeyListener input;
	
	File inputHeightMap;
	File inputColorMap;
	BufferedImage imageHeightMap;
	BufferedImage imageColorMap;
	BufferedImage skyBoxImage;
	int mapWidth;
	int mapHeight;
	short[][] heightMap;
	HashMap<Integer, StaticObject> objects;
	int objectRotation;
	int[][] objectMap;
	int[][] colorMap;
	int[][] skyBox;
	
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
	
	int mousePosX;
	int mousePosY;
	
	boolean running;
	
	float recipScreenWidth;
	float recipScreenHeight;
	
	//Robot mouseTransform;
	//JFrame display;
	
	//static BufferedImage[] testObject1Model = new BufferedImage[16];
	
	VoxelSpaceEngine() throws IOException, AWTException {
		screenWidth = 1366;
		screenHeight = 734;
		
		renderScale = 1;
		
		renderedScreenWidth = (int)(screenWidth * renderScale);
		renderedScreenHeight = (int)(screenHeight * renderScale);
		
		horizon = renderedScreenHeight * 0.5f;
		drawDist = 512;
		heightScale = 512;
		objectHeightScale = 127;
		
		//for(int i = 0; i < 16; i++) {
		//	testObject1Model[i] = ImageIO.read(new File("testObj/" + (i + 1) + ".png"));
		//}
		
		inputHeightMap = new File("Maps/D1.png");
		imageHeightMap = ImageIO.read(inputHeightMap);
		inputColorMap = new File("Maps/C1W.png");
		imageColorMap = ImageIO.read(inputColorMap);
		skyBoxImage = ImageIO.read(new File("skybox.png"));
		skyBox = new int[360 * 3][skyBoxImage.getHeight()];
		for(int x = 0; x < 360 * 3; x++) {
			for(int y = 0; y < skyBoxImage.getHeight(); y++) {
				skyBox[x][y] = skyBoxImage.getRGB((int)(x * (skyBoxImage.getWidth() / (360.0 * 3))), y);
			}
		}
		skyBoxImage = null;
		mapWidth = 2048;//imageHeightMap.getWidth();
		mapHeight = 2048;//imageHeightMap.getHeight();
		heightMap = new short[mapWidth * 2][mapHeight * 2];
		objects = new HashMap<Integer, StaticObject>();
		objectRotation = 30;
		objectMap = new int[mapWidth][mapHeight];
		colorMap = new int[mapWidth][mapHeight];
		
		posX = (mapWidth  / 2) + 100;
		posY = (mapHeight / 2) + 100;
		cameraPosX = 0;
		cameraPosY = 0;
		cameraPosZ = 0;
		
		cameraHeight = 32;
		direction = 90;
		
		cameraDistance = 1.5*cameraHeight;
		
		fallingMoveSpeed = 40;
		moveSpeed = 60;
		currentSpeed = moveSpeed;
		fallSpeed = 9.8;
		fallAccel = 9.8;
		turnSpeed = 100;
		jumpTime = 0;
		jumpHeight = cameraHeight * 0.5;
		jumpLength = 1;
		fpsMLook = true;
		FOV = 80;
		fovScale = FOV / 90.0;
		
		//mouseTransform = new Robot();
		
		//DiamondSquare test = new DiamondSquare();
		//double[][] testColorMap = smoothNoise.genRandomNoise(mapWidth);
		
		//int[][] testMap = test.genMap(mapWidth + 1, smoothNoise.genRandomNoise(mapWidth / 2 + 1));
		//testMap = test.gaussianSmooth(testMap);
		
		for(int x = 0; x < mapWidth; x++) {
			for(int y = 0; y < mapHeight; y++) {
				//heightMap[x][y] = testMap[x / 2][y / 2] / 2;
				heightMap[x][y] = evaluatePixel(imageHeightMap, x % 1024, y % 1024);
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
				//int colorVal = (int)(smoothNoise.getSmoothNoise(testColorMap, x, y, 32) * 8);
				//if(colorVal > 255) colorVal = 255;
				//else if(colorVal < 0) colorVal = 0;
				
				//colorMap[x][y] = new Color(colorVal, colorVal, colorVal).getRGB();
				colorMap[x][y] = imageColorMap.getRGB(x % 1024, y % 1024);
				
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
		
		//heightMap = test.gaussianSmooth(heightMap);
		
		posZ = heightMap[(int)posX][(int)posY];
		
		recipScreenWidth  = 2f / renderedScreenWidth;
		recipScreenHeight = 2f / renderedScreenHeight;
	}
	
	long window;
	
	int frameNum = 0;
	
	public static void main(String[] args) throws IOException, AWTException {
		VoxelSpaceEngine engine = new VoxelSpaceEngine();

		engine.init();
		
		double currentTime = System.nanoTime();
		
		
		
		while(!glfwWindowShouldClose(engine.window)) {
			engine.elapsedTime = ((System.nanoTime() - currentTime) * .000000001);
			currentTime = System.nanoTime();
			
			engine.frameNum++;
			
			if(engine.frameNum == 30) {
				glfwSetWindowTitle(engine.window, "Game Window | FPS: " +  (1 / engine.elapsedTime));
				engine.frameNum = 0;
			}
			
			glfwPollEvents();
			
			//glClearColor(0, 100f * 0.00392156862f, 150f * 0.00392156862f, 0);
			
			//glClear(GL_COLOR_BUFFER_BIT);
			
			engine.gameUpdate();
			
			engine.renderFrame();
			
			glFlush();
			
			//glfwSwapBuffers(engine.window);
		}
		
		glfwTerminate();
	}
	
	void init() {
		if(!glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW");
		}
		
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		window = glfwCreateWindow(renderedScreenWidth, renderedScreenHeight, "Window", 0, 0);
		
		if(window == 0) {
			throw new IllegalStateException("Failed to create window");
		}
		
		GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (videoMode.width() - renderedScreenWidth) / 2, (videoMode.height() - renderedScreenHeight) / 2 );
		
		glfwShowWindow(window);
		
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	}
	
	void gameUpdate() {
		//set camera position;
		cameraPosZ = posZ; //- (((engine.horizon / engine.renderedScreenHeight) - .5) * (engine.heightScale / 48));
		cameraPosX = posX; //+ Math.sin(Math.toRadians(engine.direction)) * engine.cameraDistance;
		cameraPosY = posY; //+ Math.cos(Math.toRadians(engine.direction)) * engine.cameraDistance;
		
			if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
				glfwSetWindowShouldClose(window, true);
			if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
				move(1, currentSpeed);
			if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
				move(2, currentSpeed);	
			if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
				move(3, currentSpeed);
			if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
				move(4, currentSpeed);
			if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
				if(jumpTime == 0 && posZ == heightMap[(int)posX][(int)posY]) {
					originalJumpPosZ = (int) posZ;
					jumpTime = jumpLength;
				}
			}
		checkingKeys = false;
		
		DoubleBuffer mousePosX = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer mousePosY = BufferUtils.createDoubleBuffer(1);
		glfwGetCursorPos(window, mousePosX, mousePosY);
		
		direction -= 0.25 * (mousePosX.get(0) - (screenWidth * .5));
		horizon -= (mousePosY.get(0) - (screenHeight * .5));
		
		glfwSetCursorPos(window, renderedScreenWidth / 2.0, renderedScreenHeight / 2.0);
	
		if(horizon < 0) horizon = 0;
		if(horizon > renderedScreenHeight) horizon = renderedScreenHeight;
		if(direction > 360) direction -= 360;
		if(direction <= 0) direction += 360;
		
		//mouseTransform.mouseMove(display.getLocationOnScreen().x + (screenWidth / 2), display.getLocationOnScreen().y + (screenHeight / 2));
		
		//jumping
		if(jumpTime > 0) {
				posZ = (float)(((-jumpHeight * Math.pow(jumpTime - (jumpLength * .5), 2) * 4) + jumpHeight) + originalJumpPosZ);
				jumpTime -= elapsedTime;
				if(posZ < heightMap[(int)posX][(int)posY]) {
					posZ = heightMap[(int)posX][(int)posY];
					jumpTime = 0;
			}
		}
		else if(jumpTime < 0) jumpTime = 0;

		//gravity and move speed slow while falling
		if(posZ > (heightMap[(int)posX][(int)posY]) && jumpTime == 0) {
			posZ -= ((fallSpeed) * (cameraHeight * 0.5)) * elapsedTime;
			if(currentSpeed > fallingMoveSpeed) {
				currentSpeed -= (moveSpeed - fallingMoveSpeed) * 0.02; // 0.02 = 1 / 50, 50 is falling speed
			}
			if(posZ < heightMap[(int)posX][(int)posY]) {
				posZ = heightMap[(int)posX][(int)posY];
				fallSpeed = 9.8;
			}
			
			fallSpeed += fallAccel * elapsedTime;
		}
		else if(currentSpeed != moveSpeed) {
			currentSpeed = moveSpeed;
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
	
	void renderFrame() {
		for(int column = frameNum & 7; column < renderedScreenWidth; column += 4) {
			float horizontalScreenPoint = column * recipScreenWidth;
			float rayDeg = (((horizontalScreenPoint * 0.5f * ((direction - (FOV * 0.5f)) - (direction + (FOV * 0.5f)))) + (direction + (FOV * 0.5f))));
			
			if(rayDeg < 0) rayDeg += 360;
			if(rayDeg >= 360) rayDeg -= 360;
			
			
			float[] rayDir = new float[] {(float)-Math.sin(Math.toRadians(rayDeg)), (float)-Math.cos(Math.toRadians(rayDeg))};
			
			float yBuffer = 0;	
			int heightBuffer = 0;
			
			float renderDist = 0;
			
			boolean inBounds = true;
			boolean quickCheck = false;
			
			//which box of the map we're in
		    int[] mapSquare = new int[] {(int)cameraPosX, (int)cameraPosY};
	
		    //length of ray from current position to next x or y-side
		    float sideDistX;
		    float sideDistY;
		    
		    int jumpDist = 1; //dist to jump for the next square, allows adjusting for LOD
		    int jumpCount = 0;
	
		    //length of ray from one x or y-side to next x or y-side
		    float deltaDistX = Math.abs(1f / rayDir[0]);
		    float deltaDistY = Math.abs(1f / rayDir[1]);
		      
		    //which direction to step in
		    int stepX;
		    int stepY;
		    
		    int side = 1;
		    
		    if(rayDir[0] < 0) {
		    	stepX = -1;
		    	sideDistX = (cameraPosX - mapSquare[0]) * deltaDistX;
		    }
		    else {
		    	stepX = 1;
		        sideDistX = (mapSquare[0] + 1 - cameraPosX) * deltaDistX;
		    }
		    if(rayDir[1] < 0) {
		    	stepY = -1;
		        sideDistY = (cameraPosY - mapSquare[1]) * deltaDistY;
		    }
		    else {
		    	stepY = 1;
		    	sideDistY = (mapSquare[1] + 1 - cameraPosY) * deltaDistY;
		    }
		    
		    //increase check position to next intersection with grid lines	
			while(inBounds && renderDist < drawDist) {
				for(int i = 0; i < (1 << (jumpDist - 1)); i++) {
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
					if(++jumpCount == 512 && jumpDist < 5) {
						jumpDist++;
						jumpCount = 0;
					}
				}
				
				int testSquareX = mapSquare[0];//((mapSquare[0]) >> ((jumpDist) - 1)) << (jumpDist - 1);
				int testSquareY = mapSquare[1];//((mapSquare[1]) >> ((jumpDist) - 1)) << (jumpDist - 1);
				
				if(testSquareX >= 0 && testSquareX < mapWidth && testSquareY >= 0 && testSquareY < mapWidth) {
					if(!quickCheck || heightMap[testSquareX][testSquareY] > heightBuffer) {
						if(!quickCheck) {
							if(side == 0) testSquareX -= stepX;
							else testSquareY -= stepY;
						}
						
						if (side == 0) {
							renderDist = (((mapSquare[0] - cameraPosX) + ((1 - stepX) * 0.5f)) * deltaDistX);
						}
						else {
							renderDist = (((mapSquare[1] - cameraPosY) + ((1 - stepY) * 0.5f)) * deltaDistY);
						}
						
						double relDir = rayDeg - direction;
						if((relDir < 45 && relDir > -45) || (relDir < -135 || relDir > 135)) renderDist *= Math.cos(Math.toRadians(relDir));
						else renderDist *= Math.sin(Math.toRadians(relDir));
						
						renderDist = Math.abs(renderDist);
						
						float heightOnScreen = ((renderedScreenHeight - (((cameraPosZ + cameraHeight) - 
								(heightMap[testSquareX][testSquareY])
								) / renderDist * heightScale + horizon)) * recipScreenHeight);
						
						Color mapColor = new Color(colorMap[testSquareX][testSquareY]);
						
						if(heightOnScreen > 2) {
							heightOnScreen = 2;
							inBounds = false;
						}
						else if(heightOnScreen < yBuffer) {
							continue;
						}

						glBegin(GL_QUADS);
							glColor4f(mapColor.getRed() * 0.00392156862f, mapColor.getGreen() * 0.00392156862f, mapColor.getBlue() * 0.00392156862f, 0);
							glVertex2f((float)horizontalScreenPoint - 1, (float)yBuffer - 1);
							glVertex2f((float)horizontalScreenPoint - 1 + (float)recipScreenWidth * 2, (float)yBuffer - 1);
							glVertex2f((float)horizontalScreenPoint - 1 + (float)recipScreenWidth * 2, (float)heightOnScreen - 1);
							glVertex2f((float)horizontalScreenPoint - 1, (float)heightOnScreen - 1);
						glEnd();
						
						yBuffer = heightOnScreen;
						quickCheck = quickCheck || yBuffer > (2 - horizon * recipScreenHeight);
						heightBuffer = heightMap[testSquareX][testSquareY];
					}
				}
				else inBounds = false;
			}
			glBegin(GL_QUADS);
				glColor4f(0, 100f * 0.00392156862f, 150f * 0.00392156862f, 0);
				glVertex2f((float)horizontalScreenPoint - 1, yBuffer - 1);
				glVertex2f((float)horizontalScreenPoint - 1 + recipScreenWidth * 2, yBuffer - 1);
				glVertex2f((float)horizontalScreenPoint - 1 + recipScreenWidth * 2, 1f);
				glVertex2f((float)horizontalScreenPoint - 1, 1f);
			glEnd();
		}
	}
	
	short evaluatePixel(BufferedImage image, int x, int y) {
		int sampleX = x;
		int sampleY = y;
		
		int color = image.getRGB(sampleX, sampleY);
		int redChannel = (new Color(color).getRed());
		return (short)redChannel;
	}
	
	void move(int moveDirection, double speed) {
		float lastPosZ = posZ;
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
		
		if(posX >= mapWidth || posX < 0 || posY >= mapWidth || posY < 0) {
			if(moveDirection == 2 || moveDirection == 4) {
				move(moveDirection - 1, speed);
			}
			else move(moveDirection + 1, speed);
		}
		
		if(posZ < heightMap[(int)posX][(int)posY]) {
			posZ = heightMap[(int)posX][(int)posY];
			if(posZ - lastPosZ > cameraHeight / 2) {
				if(moveDirection == 2 || moveDirection == 4) {
					move(moveDirection - 1, speed);
				}
				else move(moveDirection + 1, speed);
				posZ = lastPosZ;
			}
		}
	}
	
	public static void initializeCL() {
		CL.create();
	}
	
	
}