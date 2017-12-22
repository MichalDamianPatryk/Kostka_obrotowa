package pl.example.micha.kostka_obrotowa;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class OpenGLRenderer extends GLSurfaceView implements Renderer {

    /** Cube instance */
    private Kostka kostka;

    /* Rotation values */
    private float xrot;					//X Rotation
    private float yrot;					//Y Rotation

    /* Rotation speed values */
    private float xspeed =0.5f;				//X Rotation Speed ( NEW )
    private float yspeed =0.5f;				//Y Rotation Speed ( NEW )

    private float z = -5.0f;			//Depth Into The Screen ( NEW )

    private int filter = 0;				//Which texture filter? ( NEW )

    /** Is light enabled ( NEW ) */
    private boolean light = false;

    /*
     * The initial light values for ambient and diffuse
     * as well as the light position ( NEW )
     */
    private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};

    /* The buffers for our light values ( NEW ) */
    private FloatBuffer lightAmbientBuffer;
    private FloatBuffer lightDiffuseBuffer;
    private FloatBuffer lightPositionBuffer;

    /*
     * These variables store the previous X and Y
     * values as well as a fix touch scale factor.
     * These are necessary for the rotation transformation
     * added to this lesson, based on the screen touches. ( NEW )
     */
    private float oldX;
    private float oldY;
    private final float TOUCH_SCALE = 0.2f;		//Proved to be good for normal rotation ( NEW )

    /** The Activity Context */
    private Context context;

    /**
     * Instance the Cube object and set the Activity Context
     * handed over. Initiate the light buffers and set this
     * class as renderer for this now GLSurfaceView.
     * Request Focus and set if focusable in touch mode to
     * receive the Input from Screen and Buttons
     *
     * @param context - The Activity Context
     */
    public OpenGLRenderer(Context context) {
        super(context);

        //Set this as Renderer
        this.setRenderer(this);
        //Request focus, otherwise buttons won't react
        this.requestFocus();
        this.setFocusableInTouchMode(true);

        //
        this.context = context;

        //
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightAmbientBuffer = byteBuf.asFloatBuffer();
        lightAmbientBuffer.put(lightAmbient);
        lightAmbientBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightDiffuseBuffer = byteBuf.asFloatBuffer();
        lightDiffuseBuffer.put(lightDiffuse);
        lightDiffuseBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightPositionBuffer = byteBuf.asFloatBuffer();
        lightPositionBuffer.put(lightPosition);
        lightPositionBuffer.position(0);

        //
        kostka = new Kostka();
    }

    /**
     * The Surface is created/init()
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //And there'll be light!
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light ( NEW )
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light ( NEW )
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	//Position The Light ( NEW )
        gl.glEnable(GL10.GL_LIGHT0);											//Enable Light 0 ( NEW )

        //Settings
        gl.glDisable(GL10.GL_DITHER);				//Disable dithering ( NEW )
        gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping
        gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
        gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        //Load the texture for the cube once during Surface creation
        kostka.loadGLTexture(gl, this.context);
    }

    /**
     * Here we do our drawing
     */
    public void onDrawFrame(GL10 gl) {
        //Clear Screen And Depth Buffer
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();					//Reset The Current Modelview Matrix

        //Check if the light flag has been set to enable/disable lighting
        if(light) {
            gl.glEnable(GL10.GL_LIGHTING);
        } else {
            gl.glDisable(GL10.GL_LIGHTING);
        }

        //Drawing
        gl.glTranslatef(0.0f, 0.0f, z);			//Move z units into the screen
        gl.glScalef(0.8f, 0.8f, 0.8f); 			//Scale the Cube to 80 percent, otherwise it would be too large for the screen

        //Rotate around the axis based on the rotation matrix (rotation, x, y, z)
        gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);	//X
        gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);	//Y

        kostka.draw(gl, filter);					//Draw the Cube

        //Change rotation factors
        xrot += xspeed;
        yrot += yspeed;
    }

    /**
     * If the surface changes, reset the view
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if(height == 0) { 						//Prevent A Divide By Zero By
            height = 1; 						//Making Height Equal One
        }

        gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
        gl.glLoadIdentity(); 					//Reset The Projection Matrix

        //Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
        gl.glLoadIdentity(); 					//Reset The Modelview Matrix
    }

/* ***** Listener Events ( NEW ) ***** */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //
        xspeed =0;
        yspeed = 0;
        float x = event.getX();
        float y = event.getY();

        //If a touch is moved on the screen
        //Calculate the change
        float dx = x - oldX;
        float dy = y - oldY;
        //Define an upper area of 10% on the screen
        int upperArea = this.getHeight() / 10;
        //Zoom in/out if the touch move has been made in the upper

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (y < this.getHeight() / 2) {

                if (x < this.getWidth() / 2) {
                    z -= 20 * TOUCH_SCALE / 2;
                } else if (x > this.getWidth() / 2) {
                    z += 20 * TOUCH_SCALE / 2;
                }
            } else if (y > this.getHeight() / 2) {
                xrot += dy * TOUCH_SCALE;
                yrot += dx * TOUCH_SCALE;
            }
        }
        else if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            if (x < this.getWidth() / 2) {
                if (y < this.getHeight() / 2) {
                    z -= 20 * TOUCH_SCALE/2;
                }
                else if (y > this.getHeight() / 2) {
                    z += 20 * TOUCH_SCALE/2;
                }
            }
            else if (x > this.getWidth()/2){
                xrot += dy * TOUCH_SCALE;
                yrot += dx * TOUCH_SCALE;
            }
        }


        //Remember the values
        oldX = x;
        oldY = y;

        //We handled the event
        return true;
    }
}

