package pl.example.micha.kostka_obrotowa;

import android.app.Activity;
import android.os.Bundle;

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson07.java
 *
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class MainActivity extends Activity {

    /** Our own OpenGL View overridden */
    private OpenGLRenderer OpenGLRenderer;

    /**
     * Initiate our @see Lesson07.java,
     * which is GLSurfaceView and Renderer
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initiate our Lesson with this Activity Context handed over
        OpenGLRenderer = new OpenGLRenderer(this);
        //Set the lesson as View to the Activity
        setContentView(OpenGLRenderer);
    }

    /**
     * Remember to resume our Lesson
     */
    @Override
    protected void onResume() {
        super.onResume();
        OpenGLRenderer.onResume();
    }

    /**
     * Also pause our Lesson
     */
    @Override
    protected void onPause() {
        super.onPause();
        OpenGLRenderer.onPause();
    }

}