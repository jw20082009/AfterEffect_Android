package com.eyedog.aftereffect.utils;

import android.hardware.Camera;
import android.util.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * created by jw200 at 2018/6/11 16:38
 **/
public class CameraSizeUtils {

    private static final String TAG = "CameraSizeUtils";

    public static Camera.Size getPictureSize(List<Camera.Size> list, int picwidth, int picheight) {
        if (list == null || list.size() == 0) {
            return null;
        }
        if (picwidth > picheight) {
            int temp = picwidth;
            picwidth = picheight;
            picheight = temp;
        }
        float ratio = 1.0f * picheight / picwidth;
        Camera.Size result = null;
        for (Camera.Size size : list) {
            int width = size.width;
            int height = size.height;
            if (width > height) {
                int temp = width;
                width = height;
                height = temp;
            }
            if ((1.0f * height / width) == ratio) {
                if (result != null) {
                    int result_w = result.width;
                    int result_h = result.height;
                    if (result_w > result_h) {
                        int temp = result_w;
                        result_w = result_h;
                        result_h = temp;
                    }
                    if (result_w < width) {
                        result = size;
                    }
                } else {
                    result = size;
                }
            }
        }
        if (result == null) {
            result = list.get(0);
        }
        return result;
    }

    public static Camera.Size getLargeSize(List<Camera.Size> list, int width, int height,
        boolean isPreview) {
        if (list == null || list.size() == 0) {
            return null;
        }
        if (width > height) {
            int tempwidth = width;
            width = height;
            height = tempwidth;
        }
        // 存放宽高与屏幕宽高相同的size
        Camera.Size size = null;
        // 存放比率相同的最大size
        Camera.Size largeSameRatioSize = null;
        // 存放比率差距0.1的最大size
        Camera.Size largeRatioSize = null;
        float scrwhRatio = 1.0f * width / height;
        for (Camera.Size preSize : list) {
            float tempRatio = 1.0f * preSize.width / preSize.height;
            if (preSize.width < preSize.height) {
                if (preSize.width == width && preSize.height == height) {
                    size = preSize;
                    break;
                }
            } else if (preSize.width > preSize.height) {
                tempRatio = 1.0f * preSize.height / preSize.width;
                if (preSize.height == width && preSize.width == height) {
                    size = preSize;
                    break;
                }
            }

            if (tempRatio == scrwhRatio) {
                if (largeSameRatioSize == null) {
                    largeSameRatioSize = preSize;
                } else {
                    int largeSameRatioWidth = largeSameRatioSize.width;
                    if (largeSameRatioSize.width > largeSameRatioSize.height) {
                        largeSameRatioWidth = largeSameRatioSize.height;
                    }
                    int preSizeWidth = preSize.width;
                    if (preSize.width > preSize.height) {
                        preSizeWidth = preSize.height;
                    }
                    if (Math.abs(largeSameRatioWidth - width) > Math
                        .abs(preSizeWidth - width)) {
                        int minSize = 540;
                        if (isPreview) {
                            minSize = 405;
                        }
                        if (preSizeWidth <= width && preSizeWidth >= minSize) {
                            largeSameRatioSize = preSize;
                        } else if (preSizeWidth >= width) {
                            if (isPreview) {
                                if (preSizeWidth < width * 2) {
                                    largeSameRatioSize = preSize;
                                }
                            } else {
                                largeSameRatioSize = preSize;
                            }
                        }
                    } else if (Math.abs(largeSameRatioWidth - width) == Math
                        .abs(preSizeWidth - width)) {
                        largeSameRatioSize =
                            largeSameRatioWidth > preSizeWidth ? largeSameRatioSize : preSize;
                    }
                }
            }

            float ratioDistance = Math.abs(tempRatio - scrwhRatio);
            if (ratioDistance < 0.1) {
                if (largeRatioSize == null) {
                    largeRatioSize = preSize;
                } else {
                    int largeRatioWidth = largeRatioSize.width;
                    if (largeRatioSize.width > largeRatioSize.height) {
                        largeRatioWidth = largeRatioSize.height;
                    }
                    int preSizeWidth = preSize.width;
                    if (preSize.width > preSize.height) {
                        preSizeWidth = preSize.height;
                    }
                    if (Math.abs(largeRatioWidth - width) > Math.abs(preSizeWidth - width)) {
                        largeRatioSize = preSize;
                    }
                }
            }
        }

        if (size != null) {
            return size;
        } else if (largeSameRatioSize != null) {
            int largeSameRatioWidth = largeSameRatioSize.width;
            if (largeSameRatioSize.width > largeSameRatioSize.height) {
                largeSameRatioWidth = largeSameRatioSize.height;
            }
            if (Math.abs(largeSameRatioWidth - width) <= (width * 1.0f / 3.0f)) {
                return largeSameRatioSize;
            } else if (largeRatioSize != null) {
                if (!isPreview) {
                    return largeRatioSize;
                }
                int largeRatioWidth = largeRatioSize.width;
                if (largeRatioSize.width > largeRatioSize.height) {
                    largeRatioWidth = largeRatioSize.height;
                }
                if (Math.abs(largeRatioWidth - width) < (width * 1.0f / 3.0f)) {
                    return largeRatioSize;
                } else {
                    return getPictureSize(list, width, height);
                }
            } else {
                return getPictureSize(list, width, height);
            }
        } else if (largeRatioSize != null) {
            int largeRatioWidth = largeRatioSize.width;
            if (largeRatioSize.width > largeRatioSize.height) {
                largeRatioWidth = largeRatioSize.height;
            }
            if (Math.abs(largeRatioWidth - width) <= (width * 1.0f / 3.0f)) {
                return largeRatioSize;
            } else {
                return getPictureSize(list, width, height);
            }
        } else {
            return getPictureSize(list, width, height);
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    public static Size chooseVideoSize(Size[] choices) {
        for (Size option : choices) {
            int optionWidth = option.getWidth();
            int optionHeight = option.getHeight();
            if (option.getWidth() < option.getHeight()) {
                optionWidth = option.getHeight();
                optionHeight = option.getWidth();
            }
            if (optionWidth == optionHeight * 16 / 9 && optionWidth <= 1280) {
                return option;
            }
        }
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width The minimum desired width
     * @param height The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        List<Size> sameRatioS = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            int optionWidth = option.getWidth();
            int optionHeight = option.getHeight();
            if ((width > height && option.getWidth() < option.getHeight()) || (width < height
                && option.getWidth() > option.getHeight())) {
                optionWidth = option.getHeight();
                optionHeight = option.getWidth();
            }
            if (option.getHeight() == option.getWidth() * h / w) {
                if (optionWidth >= width && optionHeight >= height) {
                    bigEnough.add(option);
                } else {
                    sameRatioS.add(option);
                }
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            if (sameRatioS.size() > 0) {
                return Collections.max(sameRatioS, new CompareSizesByArea());
            } else {
                return choices[0];
            }
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}
