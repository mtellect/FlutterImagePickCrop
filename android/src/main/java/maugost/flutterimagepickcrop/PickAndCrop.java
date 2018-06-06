package maugost.flutterimagepickcrop;

import android.content.Intent;

import io.flutter.plugin.common.PluginRegistry;

/**
 * Created by Maugost Okore , CEO MTR Limited on 6/5/2018.
 */
public class PickAndCrop implements PluginRegistry.ActivityResultListener,
        PluginRegistry.RequestPermissionsResultListener {
    @Override
    public boolean onActivityResult(int i, int i1, Intent intent) {
        return false;
    }

    @Override
    public boolean onRequestPermissionsResult(int i, String[] strings, int[] ints) {
        return false;
    }
}
