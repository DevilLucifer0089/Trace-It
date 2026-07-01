package com.lostandfound.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper methods for camera photo file creation and image URI management.
 */
public class ImageUtils {

    private ImageUtils() {}

    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "ITEM_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                "com.trinoka.traceit.fileprovider",
                file
        );
    }
}
