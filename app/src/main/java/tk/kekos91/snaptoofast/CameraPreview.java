package tk.kekos91.snaptoofast;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by kekos91 on 30/11/2016.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    public Camera mCamera;
    private Context mContext;
    private Camera.Size mPreviewSize;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<String> mSupportedFlashModes;
    private View mCameraView;

    public CameraPreview(Context context, Camera camera, View cameraView) {
        super(context);

        mCameraView = cameraView;
        mContext = context;
        setCamera(camera);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void startCameraPreview()
    {
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setCamera(Camera camera)
    {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        mCamera = camera;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();

        // Set the camera to Auto Flash mode.
        if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_OFF)){
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        }

        requestLayout();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null){
            mCamera.stopPreview();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            return;
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            if(mPreviewSize != null) {
                Camera.Size previewSize = mPreviewSize;
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }

            mCamera.setParameters(parameters);
            //mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null){
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            Log.e("PREVIIIIEW","aaaaah mPreviewSize = "+mPreviewSize);
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null){
                Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int rot = display.getRotation();
                switch (rot)
                {
                    case Surface.ROTATION_0:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        mCamera.setDisplayOrientation(90);
                        break;
                    case Surface.ROTATION_90:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        break;
                    case Surface.ROTATION_180:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        break;
                    case Surface.ROTATION_270:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        mCamera.setDisplayOrientation(180);
                        break;
                }
            }
            final int scaledChildHeight = previewHeight * width / previewWidth;
            mCameraView.layout(0, height - scaledChildHeight, width, height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height)
    {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;
        optimalSize = sizes.get(1);
        /*
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        double minDiff = Double.MAX_VALUE;
        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes){

            //if (size.height != width) continue;
            double ratio = (double) size.width / size.height;
            if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE){
                optimalSize = size;
            }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }
        */
        return optimalSize;
    }
}
