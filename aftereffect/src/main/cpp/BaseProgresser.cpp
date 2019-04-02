//
// Created by jw200 on 2019/4/2.
//

#include "BaseProgresser.h"

int64_t BaseProgresser::getProgress() {
    return progress;
}

int64_t BaseProgresser::getTotalProgress() {
    return totalProgress;
}

void BaseProgresser::setProgress(int64_t p) {
    progress = p;
}

void BaseProgresser::setTotalProgress(int64_t t) {
    totalProgress = t;
}