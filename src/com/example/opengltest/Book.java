package com.example.opengltest;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Book 
{
	private float mOpenState;
	
	//x,y,z of the top of our book's spine.
	private float bookCoord[] = { -0.5f,  0.5f, 0.0f};
	
	//Single square object, which we transform with a 
	//matrix every draw call to draw all necessary squares.
	private Square mSquare;
	
	
	float leftCoverRotation = -0.1f;
	float rightCoverRotation = -0.9f;

	private final float[] resultMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	
	private boolean mIsActive;
			
	public Book()
	{
		mSquare = new Square();
	}
	
	public void draw(float[] mvpMatrix, float angle)
	{
		if(mIsActive)
			mOpenState = angle;
		else
			setInactiveState();
		
		float left = -92;
		float right = -88;
		left -= mOpenState * 83;
		right += mOpenState * 83;
		
		 // Create a rotation for the triangle
        Matrix.setRotateM(mRotationMatrix, 0, left, 0, -1.0f, 0);
        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(resultMatrix, 0, mRotationMatrix, 0, mvpMatrix, 0);
        
        mSquare.draw(resultMatrix);
        
        // Create a rotation for the triangle
        Matrix.setRotateM(mRotationMatrix, 0, right, 0, -1.0f, 0);
        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(resultMatrix, 0, mRotationMatrix, 0, mvpMatrix, 0);
        
        mSquare.draw(resultMatrix);
	}
	
	//When book control is removed, take the 
	//current angle and make the book open or closed
	public void setInactiveState()
	{
		if (mOpenState > 0.75)
			mOpenState = 1.0f;
		else
			mOpenState = 0-.0f;
	}

	public void setIsActive(boolean isActive) {
		this.mIsActive = isActive;
	}
}
