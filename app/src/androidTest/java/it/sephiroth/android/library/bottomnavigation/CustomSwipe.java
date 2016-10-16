package it.sephiroth.android.library.bottomnavigation;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.action.Swiper;
import android.support.test.espresso.core.deps.guava.base.Preconditions;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Executes different swipe types to given positions.
 */
public enum CustomSwipe implements Swiper {

    /** Swipes quickly between the co-ordinates. */
    MEDIUM {
        @Override
        public Swiper.Status sendSwipe(
            UiController uiController, float[] startCoordinates,
            float[] endCoordinates, float[] precision) {
            return sendLinearSwipe(uiController, startCoordinates, endCoordinates, precision,
                SWIPE_MEDIUM_DURATION_MS
            );
        }
    };

    private static final String TAG = CustomSwipe.class.getSimpleName();

    /** The number of motion events to send for each swipe. */
    private static final int SWIPE_EVENT_COUNT = 10;

    /** Length of time a "fast" swipe should last for, in milliseconds. */
    private static final int SWIPE_MEDIUM_DURATION_MS = 400;

    private static float[][] interpolate(float[] start, float[] end, int steps) {
        Preconditions.checkElementIndex(1, start.length);
        Preconditions.checkElementIndex(1, end.length);

        float[][] res = new float[steps][2];

        for (int i = 1; i < steps + 1; i++) {
            res[i - 1][0] = start[0] + (end[0] - start[0]) * i / (steps + 2f);
            res[i - 1][1] = start[1] + (end[1] - start[1]) * i / (steps + 2f);
        }

        return res;
    }

    private static Swiper.Status sendLinearSwipe(
        UiController uiController, float[] startCoordinates,
        float[] endCoordinates, float[] precision, int duration) {
        Preconditions.checkNotNull(uiController);
        Preconditions.checkNotNull(startCoordinates);
        Preconditions.checkNotNull(endCoordinates);
        Preconditions.checkNotNull(precision);

        float[][] steps = interpolate(startCoordinates, endCoordinates, SWIPE_EVENT_COUNT);
        final int delayBetweenMovements = duration / steps.length;

        MotionEvent downEvent = MotionEvents.sendDown(uiController, startCoordinates, precision).down;
        try {
            for (int i = 0; i < steps.length; i++) {
                if (!MotionEvents.sendMovement(uiController, downEvent, steps[i])) {
                    Log.e(TAG, "Injection of move event as part of the swipe failed. Sending cancel event.");
                    MotionEvents.sendCancel(uiController, downEvent);
                    return Swiper.Status.FAILURE;
                }

                long desiredTime = downEvent.getDownTime() + delayBetweenMovements * i;
                long timeUntilDesired = desiredTime - SystemClock.uptimeMillis();
                if (timeUntilDesired > 10) {
                    uiController.loopMainThreadForAtLeast(timeUntilDesired);
                }
            }

            if (!MotionEvents.sendUp(uiController, downEvent, endCoordinates)) {
                Log.e(TAG, "Injection of up event as part of the swipe failed. Sending cancel event.");
                MotionEvents.sendCancel(uiController, downEvent);
                return Swiper.Status.FAILURE;
            }
        } finally {
            downEvent.recycle();
        }
        return Swiper.Status.SUCCESS;
    }

    }
