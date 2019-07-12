package com.eyedog.aftereffect.camera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyedog.aftereffect.R;
import com.eyedog.aftereffect.player.CameraGLSurfaceView;
import com.eyedog.basic.fragments.BaseThreadHandlerFragment;

public class CameraFragment extends BaseThreadHandlerFragment {

    private CameraGLSurfaceView mCameraView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_glplayer, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraView = view.findViewById(R.id.camera_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCameraView.release();
    }
}
