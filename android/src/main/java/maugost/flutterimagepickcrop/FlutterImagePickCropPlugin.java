package maugost.flutterimagepickcrop;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Toast;

import com.myhexaville.smartimagepicker.ImagePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_OK;

/**
 * FlutterImagePickCropPlugin
 */
public class FlutterImagePickCropPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
    private static final String PICK_OPTIONS = "Options";
    private static final String REQUEST_FOR_GALLERY = "fromGalleryCropImage";
    private static final String REQUEST_FOR_CAMERA = "fromCameraCropImage";
    private static final int PICK_FROM_CAMERA = 1000;
    private static final int PICK_FROM_GALLARY = 2000;
    private static final String PICK_AND_CROP = "pickAndCropImage";
    private final MethodChannel channel;
    private ImagePicker imagePicker;
    private Activity activity;
    private Context context;
    private Uri destinationUri;
    private Uri mCropImageUri;
    private Result pendingResult;
    private MethodCall methodCall;


    private FlutterImagePickCropPlugin(Activity activity, Context context, MethodChannel channel) {
        this.activity = activity;
        this.context = context;
        this.channel = channel;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_image_pick_crop");

        FlutterImagePickCropPlugin instance = new FlutterImagePickCropPlugin(registrar.activity(), registrar.context(), channel);

        registrar.addRequestPermissionsResultListener(instance);
        registrar.addActivityResultListener(instance);
        channel.setMethodCallHandler(instance);

    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        /*if ("getPlatformVersion".equals(call.method)) {
            pendingResult.success("Android " + Build.VERSION.RELEASE);
        } else*/

        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithAlreadyActiveError();
            return;
        }

        if (PICK_AND_CROP.equals(call.method)) {
            checkRequestResult(call);
        } else {
            pendingResult.notImplemented();
        }
    }

    private void checkRequestResult(MethodCall call) {
        if (call.hasArgument(PICK_OPTIONS)) {
            String options = call.argument(PICK_OPTIONS).toString();

            Toast.makeText(activity, "Option Received", Toast.LENGTH_SHORT).show();

            if (options.equals(REQUEST_FOR_CAMERA)) {
                openPickupFromCamera();
            } else if (options.equals(REQUEST_FOR_GALLERY)) {
                openPickupFromGallery();
            }
        }
    }

    private void openPickupFromCamera() {
        Toast.makeText(activity, REQUEST_FOR_CAMERA, Toast.LENGTH_SHORT).show();

        /* For Image capture from camera */
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, PICK_FROM_CAMERA);


    }

    private void openPickupFromGallery() {
        Toast.makeText(activity, REQUEST_FOR_GALLERY, Toast.LENGTH_SHORT).show();

        // For Image capture from Gallary
   /*     activity.startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                PICK_FROM_GALLARY);*/
        CropImage.startPickImageActivity(activity);

    }


    private void startCropImageActivity() {
        CropImage.activity()
                .start(activity);
    }


    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(activity);
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                .start(activity);


    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                finishWithSuccess(resultUri.getPath());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                finishWithError("Failed Permission",error.getMessage());
            }

            return true;
        }

    if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(context, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(context, imageUri)) {
                mCropImageUri = imageUri;
                //Toast.makeText(activity, "check: " + mCropImageUri.toString(), Toast.LENGTH_SHORT).show();
                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                startCropImageActivity(imageUri);
            }


            return true;
        }
//
//        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
//            Uri imageUri = CropImage.getPickImageResultUri(context, data);
//            if (imageUri != null) {
//                finishWithSuccess(imageUri.getPath());
//            }else {
//                finishWithSuccess(null);
//            }
//            return true;
//        }

        return false;
    }


    @Override
    public boolean onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                finishWithError("Failed Permission","Cancelling, required permissions are not granted");
                //Toast.makeText(activity, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }

            return true;
        }

        return false;
    }

    private void finishWithSuccess(String imagePath) {
        pendingResult.success(imagePath);
        clearMethodCallAndResult();
    }

    private void finishWithAlreadyActiveError() {
        finishWithError("already_active", "Image picker is already active");
    }

    private void finishWithError(String errorCode, String errorMessage) {
        pendingResult.error(errorCode, errorMessage, null);
        clearMethodCallAndResult();
    }

    private void clearMethodCallAndResult() {
        methodCall = null;
        pendingResult = null;
    }

    private boolean setPendingMethodCallAndResult(
            MethodCall methodCall, MethodChannel.Result result) {
        if (pendingResult != null) {
            return false;
        }

        this.methodCall = methodCall;
        pendingResult = result;
        return true;
    }
}
