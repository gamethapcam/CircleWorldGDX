package com.fdangelo.circleworld.universeview;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.fdangelo.circleworld.GameLogic;
import com.fdangelo.circleworld.GameLogicState;
import com.fdangelo.circleworld.universeview.objects.AvatarInputMode;
import com.fdangelo.circleworld.universeview.objects.AvatarViewInput;
import com.fdangelo.circleworld.universeview.objects.InputAreas;
import com.fdangelo.circleworld.universeview.objects.ShipInputMode;
import com.fdangelo.circleworld.universeview.objects.ShipViewInput;
import com.fdangelo.circleworld.universeview.tilemap.PlanetView;
import com.fdangelo.circleworld.utils.Mathf;

public class UniverseViewCamera
{
	static public UniverseViewCamera Instance;
    
    private static final float SMOOTH_TIME = 0.5f;
    private static final float ZOOM_SMOOTH_TIME = 0.15f;
    
    private static final float MIN_CAMERA_DISTANCE = 0.01f;
    private static final float MAX_CAMERA_DISTANCE = 100;
    
    private float cameraDistance = 0.02f;
    private float zoomSpeed = 10;
    private float scale = 1.0f;
    private OrthographicCamera cam;
    private boolean moving;

    private float movingFromInputPositionX;
    private float movingFromInputPositionY;

    private boolean zooming;
    private float zoomingTouchFinger1FromPositionX;
    private float zoomingTouchFinger1FromPositionY;
    private float zoomingTouchFinger2FromPositionX;
    private float zoomingTouchFinger2FromPositionY;
    
    private float zoomingCameraDistanceDelta;
    
    private Actor followingObject;
    private boolean followRotation;
    private boolean followScale;
    
    private Vector2 followingObjectPositionDelta = new Vector2();
    //private Vector2 followingObjectPositionDeltaVelocity = new Vector2();
    
    private float followingObjectScaleDelta;
    //private float followingObjectScaleDeltaVelocity;
    
    private float followingObjectCameraDistanceDelta;
    //private float followingObjectCameraDistanceDeltaVelocity;
    
    private float followingObjectRotationDelta;
    
    private float followObjectSmoothTime;
    
    static private Vector3 tmpv3 = new Vector3();
    
    public Actor getFollowingObject()
    {
        return followingObject; 
    }
    
    public Camera getCamera()
    {
    	return cam;
    }
    
    public UniverseViewCamera(Stage stage)
    {
        Instance = this;
        
        cam = (OrthographicCamera) stage.getCamera();
        
        //trans.position = new Vector3(0, 0, CAMERA_Z);
    }
    
    public void Update(float deltaTime)
    {
        switch(GameLogic.Instace.getState())
        {
            case PlayingAvatar:
            case PlayingShip:
                UpdatePosition(deltaTime);
                UpdateZoomInput(deltaTime);
                UpdateZoom();
                break;
                
            case Travelling:
                UpdatePositionSmooth(deltaTime);
                UpdateZoom();
                break;
                
            case Loading:
            	//Do nothing
            	break;
        }
    }
 
    private void UpdatePosition(float deltaTime)
    {
        followObjectSmoothTime = 0;
        
        if (followingObject != null)
        {
            if (GameLogic.Instace.getState() == GameLogicState.PlayingAvatar && AvatarViewInput.mode == AvatarInputMode.Move ||
                GameLogic.Instace.getState() == GameLogicState.PlayingShip && ShipViewInput.mode == ShipInputMode.Move)
            {
                followingObjectPositionDelta.set(Mathf.lerp(followingObjectPositionDelta, Vector2.Zero, deltaTime * (1.0f / SMOOTH_TIME)));
                followingObjectScaleDelta = Mathf.lerp(followingObjectScaleDelta, 0, deltaTime * (1.0f / SMOOTH_TIME));
                followingObjectRotationDelta = Mathf.lerp(followingObjectRotationDelta, 0, deltaTime * (1.0f / SMOOTH_TIME));
                followingObjectCameraDistanceDelta = Mathf.lerp(followingObjectCameraDistanceDelta, 0, deltaTime * (1.0f / SMOOTH_TIME));
            }
            
            float newPositionX, newPositionY;
            
            if (GameLogic.Instace.getState() == GameLogicState.PlayingAvatar && AvatarViewInput.mode == AvatarInputMode.Edit)
            {
                //newPosition = followingObject.position + trans.up * followingObjectPositionDelta.y + trans.right * followingObjectPositionDelta.x;
            	
            	newPositionX = followingObject.getX();
            	newPositionY = followingObject.getY();
            	
            	tmpv3.set(cam.up).scl(followingObjectPositionDelta.y);
            	newPositionX += tmpv3.x;
            	newPositionX += tmpv3.y;
            	
            	//Calculate "right" direction by making the cross product between the camera's foward and up vectors
            	tmpv3.set(cam.up).crs(cam.direction).scl(followingObjectPositionDelta.x);
            	newPositionX += tmpv3.x;
            	newPositionX += tmpv3.y;
            }
            else
            {
                newPositionX = followingObject.getX() + followingObjectPositionDelta.x;
                newPositionY = followingObject.getY() + followingObjectPositionDelta.y;
            }
            
            //TODO: Implemented camera rotation on libgdx
            //float newRotation;
            
            //if (followRotation)
            //    newRotation = followingObject.getRotation();
            //else
            //    newRotation = 0;
            
            float newScale;
            
            if (followScale)
                newScale = followingObject.getScaleX();
            else
                newScale = 1.0f;
            
            cam.position.x = newPositionX;
            cam.position.y = newPositionY;
            
            //trans.rotation = newRotation * followingObjectRotationDelta;
            
            scale = newScale + followingObjectScaleDelta;
        }
    }
    
    //Called by GameLogic
    private boolean UpdatePositionSmooth(float deltaTime)
    {
        followObjectSmoothTime += deltaTime;
        
        //followingObjectPositionDelta = Vector3.SmoothDamp(followingObjectPositionDelta, Vector2.zero, ref followingObjectPositionDeltaVelocity, SMOOTH_TIME);
        
        float newPositionX, newPositionY;
        
        if (followingObject != null)
        {
        	followingObjectPositionDelta.set(Mathf.lerp(followingObjectPositionDelta, Vector2.Zero, followObjectSmoothTime / 1.0f));
        	
        	newPositionX = followingObject.getX();
        	newPositionY = followingObject.getY();
        	
        	tmpv3.set(cam.up).scl(followingObjectPositionDelta.y);
        	newPositionX += tmpv3.x;
        	newPositionX += tmpv3.y;
        	
        	//Calculate "right" direction by making the cross product between the camera's foward and up vectors
        	tmpv3.set(cam.up).crs(cam.direction).scl(followingObjectPositionDelta.x);
        	newPositionX += tmpv3.x;
        	newPositionX += tmpv3.y;
            
            //TODO: Implemented camera rotation on libgdx
            //Quaternion newRotation = followingObject.rotation;
        	
            float newScale = followingObject.getScaleX();
            
            cam.position.x = Mathf.lerp(cam.position.x, newPositionX, followObjectSmoothTime / 1.0f);
            cam.position.y = Mathf.lerp(cam.position.y, newPositionY, followObjectSmoothTime / 1.0f);;
            
            //trans.rotation = Quaternion.Lerp(trans.rotation, newRotation, followObjectSmoothTime / 1.0f);
            
            scale = Mathf.lerp(scale, newScale, followObjectSmoothTime / 1.0f);
            
            return followObjectSmoothTime > 1.0f;
        }
        else
        {
            return true;
        }
    }
    
    public void UpdateZoomInput(float deltaTime)
    {
    	if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS)
    	{
            int touchCount = 0;
            int touch1x = -1, touch1y = -1;
            int touch2x = -1, touch2y = -1;
            
            if (Gdx.input.isTouched(0))
            {
            	touch1x = Gdx.input.getX(0);
            	touch1y = Gdx.input.getY(0);
            	touchCount++;
            }
            
            if (Gdx.input.isTouched(1))
            {
            	touch2x = Gdx.input.getX(1);
            	touch2y = Gdx.input.getY(1);
            	touchCount++;
            }
    		
            if (touchCount == 2)
            {
                if (!zooming)
                {
                    zooming = true;
                }
                else
                {
                    float deltaFrom = Mathf.len(zoomingTouchFinger1FromPositionX - zoomingTouchFinger2FromPositionX, zoomingTouchFinger1FromPositionY - zoomingTouchFinger2FromPositionY);
                    float deltaTo = Mathf.len(touch1x - touch2x, touch1y - touch2y);

                    float zoom = (deltaTo - deltaFrom) / Mathf.len(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    
                    zoomingCameraDistanceDelta -= (cameraDistance + zoomingCameraDistanceDelta) * zoom * 4;
                }
                
                zoomingTouchFinger1FromPositionX = touch1x;
                zoomingTouchFinger1FromPositionY = touch1y;
                zoomingTouchFinger2FromPositionX = touch2x;
                zoomingTouchFinger2FromPositionY = touch2y;
            }
            else
            {
                zooming = false;
            }

        }
        else
        {
            //Use mouse
        	//TODO: Fix scroll wheel rotation detection!
            //float zoom = Input.GetAxis("Mouse ScrollWheel");
            //zoomingCameraDistanceDelta -= (cameraDistance + zoomingCameraDistanceDelta) * zoom * zoomSpeed * deltaTime;
        	
        	if (Gdx.input.isKeyPressed(Input.Keys.Z))
        		zoomingCameraDistanceDelta -= (cameraDistance + zoomingCameraDistanceDelta) * -1.0f * zoomSpeed * deltaTime * 0.1f;
        	else if (Gdx.input.isKeyPressed(Input.Keys.X))
        		zoomingCameraDistanceDelta -= (cameraDistance + zoomingCameraDistanceDelta) * 1.0f * zoomSpeed * deltaTime * 0.1f;
        }
    }
    
    private void UpdateZoom()
    {
        float oldDelta = zoomingCameraDistanceDelta;
        zoomingCameraDistanceDelta = Mathf.lerp(zoomingCameraDistanceDelta, 0.0f, ZOOM_SMOOTH_TIME / 1.0f);
        cameraDistance += (oldDelta - zoomingCameraDistanceDelta);
        cameraDistance = MathUtils.clamp(cameraDistance, MIN_CAMERA_DISTANCE, MAX_CAMERA_DISTANCE);  

        cam.zoom = (cameraDistance + followingObjectCameraDistanceDelta) * scale;
    }
    
    public void UpdateMove()
    {
        float movingToInputPositionX = movingFromInputPositionX;
        float movingToInputPositionY = movingFromInputPositionY;

    	if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS)
    	{
            int touchCount = 0;
            int touch1x = -1, touch1y = -1;
            
            if (Gdx.input.isTouched(0))
            {
            	touch1x = Gdx.input.getX(0);
            	touch1y = Gdx.input.getY(0);
            	touchCount++;
            }
            
            if (Gdx.input.isTouched(1))
            	touchCount++;
            
            if (touchCount == 1)
            {
                if (!moving )
                {
                    moving = true;

                    movingFromInputPositionX = touch1x;
                    movingFromInputPositionY = touch1y;
                }
                else
                {
                	movingToInputPositionX = touch1x;
                	movingToInputPositionY = touch1y;
                }
            }
            else
            {
                moving = false;
            }
        }
        else
        {
            //Use mouse
        	if (Gdx.input.isTouched())
            {
        		if (!moving)
        		{
        			moving = true;
        			movingFromInputPositionX = Gdx.input.getX();
        			movingFromInputPositionY = Gdx.input.getY();
        		}
        		else
        		{
        			movingToInputPositionX = Gdx.input.getX();
        			movingToInputPositionY = Gdx.input.getY();
        			
        		}
            }
            else
            {
                moving = false;
            }
        }

        if (moving)
        {
            if (movingFromInputPositionX != movingToInputPositionX ||
        		movingFromInputPositionY != movingToInputPositionY)
            {
            	cam.unproject(tmpv3.set(movingFromInputPositionX, movingFromInputPositionY, 0));
            	float movingFromWorldPositionX = tmpv3.x;
            	float movingFromWorldPositionY = tmpv3.y;
            	
            	cam.unproject(tmpv3.set(movingToInputPositionX, movingToInputPositionY, 0));
            	float movingToWorldPositionX = tmpv3.x;
            	float movingToWorldPositionY = tmpv3.y;
            	
            	float dx = movingToWorldPositionX - movingFromWorldPositionX;
            	float dy = movingToWorldPositionY - movingFromWorldPositionY;
                
                if (followingObject != null)
                {
                	//Calculate "right" direction by making the cross product between the camera's forward and up vectors
                	tmpv3.set(cam.up).crs(cam.direction);
                	
                    float deltaX = Vector3.dot(dx, dy, 0, tmpv3.x, tmpv3.y, tmpv3.z);
                    float deltaY = Vector3.dot(dx, dy, 0, cam.up.x, cam.up.y, cam.up.z);
                    
                    followingObjectPositionDelta.set(-deltaX, -deltaY);
                    
                    float newPositionX, newPositionY;
                    //Vector3 newPosition = followingObject.position + trans.up * followingObjectPositionDelta.y + trans.right * followingObjectPositionDelta.x;
                    //newPosition.z = CAMERA_Z;
                    
                	newPositionX = followingObject.getX();
                	newPositionY = followingObject.getY();
                	
                	tmpv3.set(cam.up).scl(followingObjectPositionDelta.y);
                	newPositionX += tmpv3.x;
                	newPositionX += tmpv3.y;
                	
                	//Calculate "right" direction by making the cross product between the camera's foward and up vectors
                	tmpv3.set(cam.up).crs(cam.direction).scl(followingObjectPositionDelta.x);
                	newPositionX += tmpv3.x;
                	newPositionX += tmpv3.y;
                    
                    cam.position.x = newPositionX;
                    cam.position.y = newPositionY;
                }

                movingFromInputPositionX = movingToInputPositionX;
                movingFromInputPositionY = movingToInputPositionY;
            }
        }
    }
    

    private boolean travelInput;
    private float travelInputStartPositionX;
    private float travelInputStartPositionY;
    private float travelInputLastPositionX;
    private float travelInputLastPositionY;
    
    public void UpdateClickOnPlanetToTravel(UniverseView universeView)
    {
    	boolean clickTravel = false;
        float clickPositionX = 0;
        float clickPositionY = 0;
        
        int touchCount = 0;
        int touch1x = -1, touch1y = -1;
        
        if (Gdx.input.isTouched(0))
        {
        	touch1x = Gdx.input.getX(0);
        	touch1y = Gdx.input.getY(0);
        	touchCount++;
        }
        
        if (Gdx.input.isTouched(1))
        	touchCount++;
    	
        if (touchCount == 1 && !InputAreas.IsInputArea(touch1x, touch1y))
        {
        	if (!travelInput)
        	{
                travelInput = true;
                travelInputStartPositionX = touch1x;
                travelInputStartPositionY = touch1y;
        	}
        	else
        	{
        		travelInputLastPositionX = touch1x;
        		travelInputLastPositionY = touch1y;
        	}
        }
        else
        {
        	if (travelInput)
        	{
                if (Mathf.len(travelInputStartPositionX - travelInputLastPositionX, travelInputStartPositionY - travelInputLastPositionY) < 10)
                {
                    clickTravel = true;
                    clickPositionX = travelInputLastPositionX;
                    clickPositionY = travelInputLastPositionY;
                }
        		
        		travelInput = false;
        	}
        }

        if (clickTravel)
        {
        	cam.unproject(tmpv3.set(clickPositionX, clickPositionY, 0));
        	
            float worldPosX = tmpv3.x;
            float worldPosY = tmpv3.y;
            
            float dpi = Gdx.graphics.getDensity() * 160.0f;
            if (dpi == 0)
            	dpi = 72.0f;
            float clickTolerance = (dpi / cam.zoom) / 2.54f; //1cm click tolerance
            
            int clickedThingIndex = universeView.getUniverse().FindClosestRenderedThing(worldPosX, worldPosY, clickTolerance);
               
            if (clickedThingIndex >= 0)
            {
                PlanetView targetPlanetView = universeView.GetPlanetView((short) clickedThingIndex);
                if (universeView.avatarView.getUniverseObject().getParent() != targetPlanetView.getTilemapCircle())
                    GameLogic.Instace.TravelToPlanet(targetPlanetView);
            }
        }
    }
    
    public void FollowObject(Actor toFollow, int followCameraParameters, boolean smoothTransition)
    {
        followRotation = (followCameraParameters & FollowCameraParameters.FollowRotation) != 0;
        followScale = (followCameraParameters & FollowCameraParameters.FollowScale) != 0;
        
        if (followingObject != toFollow)
        {
            if (smoothTransition && followingObject != null && toFollow != null)
            {
                followingObjectPositionDelta.set(cam.position.x - toFollow.getX(), cam.position.y - toFollow.getY());
                
                if (followScale)
                {
                    followingObjectScaleDelta = scale - toFollow.getScaleX();
                    
                    followingObjectCameraDistanceDelta = cameraDistance - cameraDistance * (scale / toFollow.getScaleX());
                    cameraDistance = cameraDistance * (scale / toFollow.getScaleX());
                }
                else
                {
                    followingObjectScaleDelta = scale - 1.0f;
                    
                    followingObjectCameraDistanceDelta = cameraDistance - cameraDistance * (scale / 1.0f);
                    cameraDistance = cameraDistance * (scale / 1.0f);
                }
                
                //if (followRotation)
                //    followingObjectRotationDelta = trans.rotation * Quaternion.Inverse(toFollow.rotation);
                //else
                //    followingObjectRotationDelta = trans.rotation * Quaternion.Inverse(Quaternion.identity);
            }
            
            followingObject = toFollow; 
        }
    }
}
