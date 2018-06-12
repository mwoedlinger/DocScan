package at.ac.tuwien.caa.docscan.gallery;

/* Based on the example provided in:
 * https://developer.android.com/training/animation/screen-slide.html
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;

import at.ac.tuwien.caa.docscan.R;
import at.ac.tuwien.caa.docscan.camera.threads.crop.CropLogger;
import at.ac.tuwien.caa.docscan.camera.threads.crop.PageDetector;
import at.ac.tuwien.caa.docscan.logic.Helper;
import at.ac.tuwien.caa.docscan.logic.Page;

import static at.ac.tuwien.caa.docscan.camera.threads.crop.CropManager.INTENT_FILE_MAPPED;
import static at.ac.tuwien.caa.docscan.camera.threads.crop.CropManager.INTENT_FILE_NAME;
import static at.ac.tuwien.caa.docscan.camera.threads.crop.CropManager.INTENT_PAGE_DETECTED;


public class ImageViewerFragment extends Fragment {

    private Page mPage;
//    private SubsamplingScaleImageView mImageView;
    private static final String CLASS_NAME = "ImageViewerFragment";
    private PageImageView mImageView;
    private String mFileName;
    private View mLoadingView;

    public static ImageViewerFragment create() {

        ImageViewerFragment fragment = new ImageViewerFragment();

        return fragment;

    }

    public ImageViewerFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFileName = getArguments() != null ?
                getArguments().getString(getString(R.string.key_fragment_image_viewer_file_name)) : null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_image_viewer, container, false);

        mImageView = rootView.findViewById(R.id.image_viewer_image_view);
        mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);

        mLoadingView = rootView.findViewById(R.id.image_viewer_progress_layout);

        refreshImageView();

        return rootView;

    }

    public boolean isLoadingViewVisible() {

        return mLoadingView.getVisibility() == View.VISIBLE;

    }


    public void refreshImageView() {

        mImageView.setImage(ImageSource.uri(mFileName));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mFileName, options);

        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        File file = new File(mFileName);

        try {
            int orientation = Helper.getExifOrientation(file);
            if (orientation != -1) {
                int angle = Helper.getAngleFromExif(orientation);
                Log.d(CLASS_NAME, "refreshImageView: angle: " + angle);
                if (angle == 0 || angle == 180)  {
                    imageHeight = options.outWidth;
                    imageWidth = options.outHeight;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(CLASS_NAME, "refreshImageView: w: " + imageWidth + " h: " + imageHeight);

        if (CropLogger.isAwaitingPageDetection(file)) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
        else {
            mLoadingView.setVisibility(View.INVISIBLE);
            if (!PageDetector.isCropped(mFileName))
                mImageView.setPoints(PageDetector.getScaledCropPoints(mFileName, imageHeight, imageWidth));
            else
//            We do this because the image might have been cropped in the meantime:
                mImageView.setPoints(null);
        }
    }




}
