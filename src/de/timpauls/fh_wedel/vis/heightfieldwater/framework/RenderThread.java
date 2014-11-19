package de.timpauls.fh_wedel.vis.heightfieldwater.framework;

import de.timpauls.fh_wedel.vis.heighfieldwater.model.Column;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

/**
 * Created by tim on 06.11.2014.
 */
public class RenderThread implements GLEventListener{

    public static final int DIMENSION = 200;
    public static final int COLUMN_HEIGHT = DIMENSION / 4;
    public static final double COLUMN_WIDTH = 1.0;
    public static final int COLUMN_VELOCITY = 0;
    private static final double SPEED = 7;

    private GLU mGlu;
    private long mLastMillis = 0;

    private Column[][] mColumns;
    private Column[][] mNewColumns;

    public RenderThread() {
        initColumns();
    }

    private void initColumns() {
        mColumns = new Column[DIMENSION][DIMENSION];
        mNewColumns = new Column[DIMENSION][DIMENSION];

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                float y = Math.max(COLUMN_HEIGHT - 0.01f * (i*i + j*j), 0);

                mColumns[i][j] = new Column(COLUMN_HEIGHT + y, COLUMN_VELOCITY);
                mNewColumns[i][j] = new Column(COLUMN_HEIGHT + y, COLUMN_VELOCITY);
            }
        }
    }

    private void updateColumns(long deltaT) {
        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column center = getColumn(mColumns, i, j);
                Column left = getColumn(mColumns, i-1, j);
                Column right = getColumn(mColumns, i+1, j);
                Column top = getColumn(mColumns, i, j-1);
                Column bottom = getColumn(mColumns, i, j+1);

                double f = SPEED * SPEED * (left.height + right.height + top.height + bottom.height - 4*center.height) / (COLUMN_WIDTH * COLUMN_WIDTH);
//                System.out.println(String.format("%f + %f + %f + %f - 4*%f = %f", left.height, right.height, top.height, bottom.height, center.height, f));


                center.velocity += f * deltaT / 1000f;
//                System.out.println(String.format("%f * %f = %f", f, deltaT / 1000f, f * deltaT / 1000f));
                Column newColumn = getColumn(mNewColumns, i, j);
                newColumn.height = center.height + center.velocity * deltaT / 1000f;

//                if (i == 0 && j == 0)
//                   System.out.println(String.format("f: %f; v: %f, h: %f", f, center.velocity, newColumn.height));
            }
        }

        for (int j = 0; j < DIMENSION; j++) {
            for (int i = 0; i < DIMENSION; i++) {
                Column column = getColumn(mColumns, i, j);
                Column newColumn = getColumn(mNewColumns, i, j);

                column.height = newColumn.height;
            }
        }
    }

    private Column getColumn(Column[][] columnArray, int i, int j) {
        i = Math.max(i, 0);
        i = Math.min(i, DIMENSION - 1);
        j = Math.max(j, 0);
        j = Math.min(j, DIMENSION - 1);

        return columnArray[i][j];
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        mLastMillis = System.currentTimeMillis();

        mGlu = new GLU();                         // get GL Utilities
        GL2 gl = drawable.getGL().getGL2();
        gl.setSwapInterval(1);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL2.GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL2.GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction
        gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting

    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        long currentMillis = System.currentTimeMillis();
        long deltaT = currentMillis - mLastMillis;
        mLastMillis = currentMillis;

        update(deltaT);
        render(drawable);
    }

    private void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glColor3f(0, 0, 0.7f);

        gl.glTranslatef(-DIMENSION / 2, 0, DIMENSION / 2); // translate into the screen
        for (int j = 0; j < DIMENSION - 1; j++) {
            gl.glBegin(GL2.GL_TRIANGLE_STRIP);

            for (int i = 0; i < DIMENSION; i++) {
                gl.glVertex3d(i * COLUMN_WIDTH,  mColumns[i][j].height, j * COLUMN_WIDTH);
                gl.glVertex3d(i * COLUMN_WIDTH,  mColumns[i][j+1].height, (j+1) * COLUMN_WIDTH);
            }

            gl.glEnd();
        }

//        gl.glTranslatef(0.0f, 0.0f, -6.0f); // translate into the screen
//        gl.glBegin(GL2.GL_TRIANGLES); // draw using triangles
//            gl.glVertex3f(0.0f, 1.0f, 0.0f);
//            gl.glVertex3f(-1.0f, -1.0f, 0.0f);
//            gl.glVertex3f(1.0f, -1.0f, 0.0f);
//        gl.glEnd();
    }

    private void update(long deltaT) {
        updateColumns(deltaT);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Get the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();

        height = (height == 0) ? 1 : height;  // Prevent divide by zero
        float aspect = (float)width / height; // Compute aspect ratio

        // Set view port to cover full screen
        gl.glViewport(0, 0, width, height);

        // Set up the projection matrix - choose perspective view
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();    // reset
        // Angle of view (fovy) is 45 degrees (in the up y-direction). Based on this
        // canvas's aspect ratio. Clipping z-near is 0.1f and z-near is 100.0f.
        mGlu.gluPerspective(75f, aspect, 0.1f, 100.0f); // fovy, aspect, zNear, zFar
        mGlu.gluLookAt(0, DIMENSION/2, 300, 0, 0, 230, 0, 1, 0);

        // Switch to the model-view transform
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();    // reset
    }
}
