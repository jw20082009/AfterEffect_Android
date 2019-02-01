
package com.eyedog.aftereffect.gles;

/**
 * Created by walljiang on 2018/7/26.
 */

public interface ProgressUpdater {
    /**
     * Updates a progress meter.
     * 
     * @param percent Percent completed (0-100).
     */
    void updateProgress(int percent);
}
