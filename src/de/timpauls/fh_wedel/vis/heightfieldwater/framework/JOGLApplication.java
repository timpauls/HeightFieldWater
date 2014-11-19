package de.timpauls.fh_wedel.vis.heightfieldwater.framework;

import com.jogamp.opengl.util.Animator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by tim on 05.11.2014.
 */
public class JOGLApplication {

    public static void main(String[] args) {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);

        canvas.addGLEventListener(new RenderThread());

        Animator animator = new Animator(canvas);
        animator.setUpdateFPSFrames(FPSCounter.DEFAULT_FRAMES_PER_INTERVAL, System.out);
        animator.start();

        Frame frame = new Frame("AWT Window Test");
        frame.setSize(800, 600);
        frame.add(canvas);
        frame.setVisible(true);

        // by default, an AWT Frame doesn't do anything when you click
        // the close button; this bit of code will terminate the program when
        // the window is asked to close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }


}
