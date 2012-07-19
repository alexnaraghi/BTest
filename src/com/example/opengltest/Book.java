package com.example.opengltest;

import java.util.ArrayList;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Book 
{
	//x,y,z of the book.  The initial translation
	private final float initialBookLocation[] = { 0f,  0f, 0f};
	
	//Single square object, which we transform with a 
	//matrix every draw call to draw all necessary squares.
	private final Square mSquare;
	
	//Degree rotations for open and closed states
	private final float minRotation = 87f;
	private final float maxRotation = 3f;

	//The matrix that defines the book's location
	private final float[] mBookMatrix = new float[16];
	
	//A couple arbitrary matrices we can use for intermediate calulations
	private final float[] mResultMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	
	//between 0 and 1, defines how open the book is (0 closed, 1 open)
	private float mOpenState;
	
	//Offset for the book movement in the z direction (towards the camera)
	private float mZOffset;
	
	//Defines if the book openness is being controlled by the user
	//TODO: FIX THIS
	private boolean mIsActive;
	
	//A list of pages (just rotations currently)
	private ArrayList<Float> pages;
	//A list of temporary colors (will be removed when there is lighting)
	private ArrayList<float[]> colors;		
	
	public Book()
	{
		mSquare = new Square();
		pages = new ArrayList<Float>(10);
		colors =  new ArrayList<float[]>(10);
		
		for(int i = 0; i < 10; i++)
		{
			pages.add(0f);
			//generate color
			float[] color = { 0.2f , 0.209803922f + 0.05f * i, 0.998039216f - 0.05f * i, 1.0f };
			colors.add(color);
		}
	}
	
	public void draw(float[] mvpMatrix, float angle)
	{
		/*
		if(mIsActive)
			mOpenState = angle;
		else
			setInactiveState();
		*/
		mOpenState = angle;
		
		//Hardcoded scalar for z moevement based on open state
		mZOffset = -0.7f * mOpenState;
		
		//copy matrix
		Matrix.setIdentityM(mBookMatrix, 0);
		Matrix.multiplyMM(mBookMatrix, 0, mvpMatrix, 0, mBookMatrix, 0);
		
		//Position the book matrix for all other calculations
		Matrix.translateM(mBookMatrix, 0, initialBookLocation[0], initialBookLocation[1], initialBookLocation[2] + mZOffset);
		
		drawCovers();
		drawPages();
	}
	
	private void drawCovers()
	{
		float left = relToDegreeLeft(mOpenState);
		float right = relToDegreeRight(mOpenState);
		
		 // Create a rotation for the left cover
        Matrix.setRotateM(mRotationMatrix, 0, left, 0, -1.0f, 0);
        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(mResultMatrix, 0, mRotationMatrix, 0, mBookMatrix, 0);
        
        mSquare.draw(mResultMatrix);
        
        // Create a rotation for the right cover
        Matrix.setRotateM(mRotationMatrix, 0, right, 0, -1.0f, 0);
        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(mResultMatrix, 0, mRotationMatrix, 0, mBookMatrix, 0);
        
        mSquare.draw(mResultMatrix);
	}
	
	private void drawPages()
	{
		setPageRotations();
		/*
		for(int i = 0; i < pages.size(); i ++)
		{
			Float rotation = pages.get(i);
			// Create a rotation for each page and draw
	        Matrix.setRotateM(mRotationMatrix, 0, rotation, 0, -1.0f, 0);
	        Matrix.multiplyMM(mResultMatrix, 0, mRotationMatrix, 0, mBookMatrix, 0);
	        mSquare.draw(mResultMatrix,colors.get(i));
		}
		*/
		
		//TODO: Restore regular draw code once depth based drawing is coded (this is
		//a hack for sensible draw order, but isnt necessary if depth draw is done.
		int centerPage = Math.round((pages.size()-1)/2);
		
		for(int i = centerPage; i >= 0; i--)
		{
			Float rotation = pages.get(i);
			// Create a rotation for each page and draw
	        Matrix.setRotateM(mRotationMatrix, 0, rotation, 0, -1.0f, 0);
	        Matrix.multiplyMM(mResultMatrix, 0, mRotationMatrix, 0, mBookMatrix, 0);
	        mSquare.draw(mResultMatrix,colors.get(i));
		}
		
		//Edge case
		if(centerPage < 2)
			return;
		
		for(int i = pages.size() - 1; i >= centerPage + 1; i--)
		{
			Float rotation = pages.get(i);
			// Create a rotation for each page and draw
	        Matrix.setRotateM(mRotationMatrix, 0, rotation, 0, -1.0f, 0);
	        Matrix.multiplyMM(mResultMatrix, 0, mRotationMatrix, 0, mBookMatrix, 0);
	        mSquare.draw(mResultMatrix,colors.get(i));
		}
		 
	}
	
	//Currently, distribute evenly between min and max rotations
	private void setPageRotations()
	{
		int centerPage = Math.round((pages.size()-1)/2);
		
		for(int i = 0; i <= centerPage; i++)
		{
			pages.set(i, relToPageDegreeLeft(mOpenState * absRatioToPageRatio(((float)i)/centerPage)));
		}
		
		//Edge case
		if(centerPage < 2)
			return;
		
		for(int i = centerPage + 1; i < pages.size(); i++)
		{
			pages.set(i, relToPageDegreeRight(mOpenState * absRatioToPageRatio(((float)i - (centerPage + 1))/(pages.size() - centerPage))));
		}
	}
	
	private float absRatioToPageRatio(float input)
	{
		final float startLeftRatio = 0.15f;
		final float startRightRatio = 0.85f;
		float maxRatio = 0.3f + mOpenState * 0.2f;
		
		float leftRatio = startLeftRatio + maxRatio * mOpenState;
		float rightRatio = startRightRatio - maxRatio * mOpenState;
		
		return leftRatio + rightRatio * input;
		//return 1 - input * (0.1f + 0.9f * (1f - mOpenState));
	}
	
	//When book control is removed, take the 
	//current angle and make the book open or closed
	public void setInactiveState()
	{
		if (mOpenState > 0.75)
			mOpenState = 1.0f;
		else
			mOpenState = 0.0f;
	}

	public void setIsActive(boolean isActive) {
		this.mIsActive = isActive;
	}
	
	//Converts a float between 0 and 1 into an absolute rotation in degrees (left side)
	private float relToDegreeLeft(float relative)
	{
		if(relative < 0f || relative > 1f)
			throw new RuntimeException("left is getting incorrect input " + relative);
		return - minRotation +  (minRotation - maxRotation) * relative;
	}
	
	//Converts a float between 0 and 1 into an absolute rotation in degrees (right side)
	private float relToDegreeRight(float relative)
	{
		if(relative < 0f || relative > 1f)
			throw new RuntimeException("right is getting incorrect input " + relative);
		return -180 + (minRotation - (minRotation - maxRotation) * relative);
	}
	
		private float relToPageDegreeLeft(float relative)
		{
			if(relative < 0f || relative > 1f)
				throw new RuntimeException("left is getting incorrect input " + relative);
			return - 89f +  (89f - maxRotation) * relative;
		}
		
		//Converts a float between 0 and 1 into an absolute rotation in degrees (right side)
		private float relToPageDegreeRight(float relative)
		{
			if(relative < 0f || relative > 1f)
				throw new RuntimeException("right is getting incorrect input " + relative);
			return -180 + (91f - (89f - maxRotation) * relative);
		}
}
