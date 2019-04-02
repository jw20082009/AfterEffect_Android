//
// Created by jw200 on 2019/4/2.
//

#ifndef AFTEREFFECT_ANDROID_BASEPROGRESSER_H
#define AFTEREFFECT_ANDROID_BASEPROGRESSER_H

#include "JniBase.h"

class BaseProgresser {
protected:
    int64_t progress = -1;
    int64_t totalProgress = -1;

public:
    int64_t getProgress();

    int64_t getTotalProgress();

    void setProgress(int64_t progress);

    void setTotalProgress(int64_t totalProgress);
};


#endif //AFTEREFFECT_ANDROID_BASEPROGRESSER_H
