_kernel void renderColumn(_global const float8 camera, //6 objects in camera: posX, posY, posZ, direction, horizon, FOV
						  _global const int drawDist,
						  _global const float recipScreenWidth,
						  _global const float recipScreenHeight,
						  _global const image2d_t heightMap,
						  _global const image2d_t colorMap
						  _global const image2d_t outputFrame) {
	const sampler_t samplerA = CLK_NORMALIZED_COORDS_FALSE |
                           CLK_ADDRESS_NONE         |
                           CLK_FILTER_NEAREST;					  
	
	float horizontalScreenPoint = get_global_id(0) * recipScreenWidth;
	float rayDeg = (((horizontalScreenPoint * 0.5f * ((camera.s3 - (camera.s4 * 0.5f)) - (camera.s3 + (camera.s4 * 0.5f)))) + (camera.s3 + (camera.s4 * 0.5f))));
	
	if(rayDeg < 0f) rayDeg += 360f;
	if(rayDeg > 360f) rayDeg -= 360f;
	
	float2 rayDir = (float2)(-sin(radians(rayDeg)), -cos(radians(rayDeg)));
	
	int yBuffer = 0;
	int heightBuffer = 0;
	
	float renderDist = 0f;
	
	bool inBounds = true;
	bool quickCheck = false;
	
	int2 mapSquare = (int2)((int)camera.x, (int)camera.y);
	
	float2 sideDist;
	
	float2 deltaDist = (float2)(abs(1f / rayDir.x), abs(1f / rayDir.y));
	
	int2 stepDir;
	
	int side = 1;
	
	if(rayDir.x < 0) {
    	stepDir.x = -1;
    	sideDist.x = (camera.x - mapSquare.x) * deltaDist.x;
    }
    else {
    	stepDir.x = 1;
        sideDist.x = (mapSquare.x + 1f - camera.x) * deltaDist.x;
    }
    if(rayDir.y < 0) {
    	stepDir.y = -1;
        sideDist.y = (camera.y - mapSquare.y) * deltaDist.y;
    }
    else {
    	step.y = 1;
    	sideDist.y = (mapSquare.y + 1f - camera.y) * deltaDist.y;
    }
    
    while(inBounds && renderDist < drawDist) {
    	if (sideDist.x < sideDist.y) {
          sideDist.x += deltaDist.x;
          mapSquare.x += stepDir.x;
          side = 0;
        }
        else {
          sideDist.y += deltaDist.y;
          mapSquare.y += stepDir.y;
          side = 1;
        }
        
        int2 testSquare = mapSquare;
        
        if(!quickCheck) {
        	if(side == 0) testSquare.x -= stepDir.x;
        	else testSquare.y -= stepDir.y;
        }
        
        if(testSquare.x >= 0 && testSquare.x < get_image_width(heightMap) && testSquare.y >= 0 && testSquare.y < get_image_height(heightMap)) {
			if(!quickCheck || read_imagei(heightMap, samplerA, (int2)(testSquare.x, testSquare.y)).s1 > heightBuffer) {
		        if (side == 0) {
					renderDist = (((mapSquare.x - camera.x) + ((1f - stepDir.x) * 0.5f)) * deltaDist.x);
				}
				else {
					renderDist = (((mapSquare.y - camera.y) + ((1f - stepDir.y) * 0.5f)) * deltaDist.y);
				}
				
		        renderDist = abs(renderDist);
		        
		        int heightOnScreen = (((1 / recipScreenHeight) - (((camera.s3) - 
										(read_imagei(heightMap, samplerA, (int2)(testSquare.x, testSquare.y)).s1)
										) / renderDist * 512f + camera.s5)));
				
				float4 mapColor = read_imagef(colorMap, samplerA, (int2)(testSquare.x, testSquare.y));
				
				if(heightOnScreen > get_image_height(outputFrame)) {
					heightOnScreen = get_image_height(outputFrame);
					inBounds = false;
				}
				else if(heightOnScreen < yBuffer) {
					continue;
				}
				
				for(int i = heightOnScreen; i > yBuffer; i--) {
					write_imagei(outputFrame, (int2)(get_global_id(0), i), mapColor);
				}
				
				yBuffer = heightOnScreen;
				quickCheck = quickCheck || yBuffer > (2 - camera.5 * recipScreenHeight);
				heightBuffer = read_imagei(heightMap, samplerA, (int2)(testSquare.x, testSquare.y)).s1;
			}
		}
		else inBounds = false;
    }
    
}						  	   