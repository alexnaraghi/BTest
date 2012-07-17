package com.example.opengltest;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {

    private GLSurfaceView mGLView;
    
    float mPreviousX;
    float mPreviousY;

    
    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
*/
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    class MyGLSurfaceView extends GLSurfaceView {

    	private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;
		private MyRenderer mRenderer;
    	
        public MyGLSurfaceView(Context context){
            super(context);

            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);
            
            // Set the Renderer for drawing on the GLSurfaceView
            mRenderer = new MyRenderer();
            setRenderer(mRenderer);
            
         // Render the view only when there is a change in the drawing data
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    
        float initialOneX;
        float initialOneY;
        float initialDistance;
        
        float oneX;
        float oneY;
        
        float initialTwoX;
        float initialTwoY;
        
        float twoX;
        float twoY;
        
        boolean fingerOneDown;
        boolean fingerTwoDown;
        
        //Between 0 and 1, 0 being the initial finger positions or less, 1 being the horizontal screen edges
        float openState;
    
	    @Override
	    public boolean onTouchEvent(MotionEvent e) {
	        // MotionEvent reports input details from the touch screen
	        // and other input controls. In this case, you are only
	        // interested in events where the touch position changed.
	    	 int action = e.getAction() & MotionEvent.ACTION_MASK;
	         int pointerIndex = (e.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
	         int pointerId = e.getPointerId(pointerIndex);
	        
	         //Define events for finger touches and releases
            switch (action) {
            case MotionEvent.ACTION_DOWN: {
                    //Log.d("MultitouchExample","Action Down");
                    if (pointerId == 0)
                    {
                    	Log.d("MultitouchExample","Finger One Down");
                        fingerOneDown = true;
                        oneX = initialOneX = e.getX(pointerIndex);
                        oneY = initialOneY = e.getY(pointerIndex);
                    }
                    break;
            }
            case MotionEvent.ACTION_MOVE: {
                    //Log.d("MultitouchExample","Action Move");
            	 int pointerCount = e.getPointerCount();
                 for(int i = 0; i < pointerCount; ++i)
                 {
                     pointerIndex = i;
                     pointerId = e.getPointerId(pointerIndex);    
                     if (pointerId == 0)
	                    {
	                    	oneX = e.getX(pointerIndex);
	                        oneY = e.getY(pointerIndex);
	                    }
	                    else if (pointerId == 1)
	                    {
	                    	twoX = e.getX(pointerIndex);
	                        twoY = e.getY(pointerIndex);
	                    }
                 }
                    break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                    //Log.d("MultitouchExample","Pointer Down");
                    if (pointerId == 1)
                    {
                    	Log.d("MultitouchExample","Finger Two Down");
                        fingerTwoDown = true;
                        twoX = initialTwoX = e.getX(pointerIndex);
                        twoY = initialTwoY = e.getY(pointerIndex);
                        
                        //Define the initial distance as the distance 
                        //between the initial 2nd finger touch and the current 1st finger touch
                        initialDistance = Math.abs(initialTwoX - oneX);
                        initialOneX = oneX;
                        initialOneY = oneY;
                        
                        openState = 0;
                    }
            break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                    //Log.d("MultitouchExample","Pointer up");
                    if (pointerId == 1)
                    {
                    	Log.d("MultitouchExample","Finger Two Up");
                        fingerTwoDown = false;
                    }
                    break;
            }
            case MotionEvent.ACTION_UP: {
                    //Log.d("MultitouchExample", "Action up");
                    if (pointerId == 0)
                    {
                    	Log.d("MultitouchExample","Finger One Up");
                        fingerOneDown = false;
                    }
                    break;
            }
            }
            
            if(fingerOneDown && fingerTwoDown)
            {
            	mRenderer.isActive = true;
            	CalulateOpenState();
            }
            else
            	mRenderer.isActive = false;
	    	
	        float x = e.getX();
	        float y = e.getY();
	
	        switch (e.getAction()) 
	        {
	            case MotionEvent.ACTION_MOVE:
	
	                float dx = x - mPreviousX;
	                float dy = y - mPreviousY;
	
	                // reverse direction of rotation above the mid-line
	                if (y > getHeight() / 2) {
	                  dx = dx * -1 ;
	                }
	
	                // reverse direction of rotation to left of the mid-line
	                if (x < getWidth() / 2) {
	                  dy = dy * -1 ;
	                }
	
	                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
	                requestRender();
	        }
	
	        mPreviousX = x;
	        mPreviousY = y;
	        return true;
	    }
	    
	    int c = 0;
	    //OpenState is set between 0 and 1.
	    public void CalulateOpenState()
	    {
	    	//use left and right x, we dont need other state info here
	    	float leftFingerX;
	    	float rightFingerX;

	    	float initialDistanceFromLeft;
	    	float initialDistanceFromRight;
	    	
	    	boolean isFingerOneLeftOfFingerTwo = initialOneX < initialTwoX;
	    	
	    	if(isFingerOneLeftOfFingerTwo)
	    	{
	    		initialDistanceFromLeft = initialOneX;
	    		initialDistanceFromRight = getWidth() - initialTwoX;
	    		
	    		leftFingerX = Math.min(initialOneX, oneX);
	    		rightFingerX = Math.max(initialTwoX, twoX);
	    	}
	    	else
	    	{
	    		initialDistanceFromLeft = initialTwoX;
	    		initialDistanceFromRight = getWidth() - initialOneX;
	    		
	    		leftFingerX = Math.min(initialTwoX, twoX);
	    		rightFingerX = Math.max(initialOneX, oneX);
	    	}
	    	
	    	//now that left and right are defined, use their combined ratio of 
	    	//distance from the horizontal edges as openState
	    	
	    	float leftRatio = (initialDistanceFromLeft - leftFingerX) / initialDistanceFromLeft;
	    	float rightRatio = (rightFingerX - initialDistanceFromRight) / (getWidth() - initialDistanceFromRight);
	    	
	    	//take the average
	    	openState = (Math.min(1.0f,leftRatio) + Math.min(1.0f,rightRatio)) / 2;
	    	mRenderer.mOpenState = openState;
	    	
	    	//avoid log spam
	    	c++;
	    	if(c%5 == 0)
	    	{
	    		Log.d("MultitouchExample", "openState = " + openState);
	    		/*
	    		Log.d("MultitouchExample", "leftRatio = " + leftRatio);
	    		Log.d("MultitouchExample", "right = " + rightRatio);
	    		Log.d("MultitouchExample", "initialDistanceFromRight = " + initialDistanceFromRight);
	    		Log.d("MultitouchExample", "rightFingerX = " + rightFingerX);
	    		*/
	    		
	    	}
	    	
	    }
    }

    
}
