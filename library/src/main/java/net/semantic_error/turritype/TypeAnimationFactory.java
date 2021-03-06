/*
 * Copyright (C) 2015 Tomáš Valenta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.semantic_error.turritype;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * TypeAnimationFactory transforms WriteRequest in to Animator
 */
class TypeAnimationFactory {

    private static final String TAG = TypeAnimationFactory.class.getSimpleName();

    /**
     * @param wr WriteRequest for which we want to create Animation
     * @param writable TextView where we want our animation to write
     * @return Animator based on WriteRequest blueprint
     */
    static Animator create(final TurriType.WriteRequest wr, final TurriType.Writable writable) {

        // create one animation for whole text with singe interpolator
        if (wr.interpolator != null) {

            return createAddTextAnimation(writable, wr.text, wr.text.length() * wr.avgTimePerChar, wr.interpolator);

        }
        // create set of animations for every word one with interpolator
        else if( wr.wordInterpolatorList != null && wr.wordInterpolatorList.size() > 0) {

            List<Animator> animatorList = createAddTextAnimationList(wr, writable);
            
            AnimatorSet animator = new AnimatorSet();
            if (wr.animatorListener != null) {
                animator.addListener(wr.animatorListener);
            }

            animator.playSequentially(animatorList);
            return animator;
        }

        throw new IllegalArgumentException("No interpolator or interpolator list specified");
    }

    @NonNull
    private static List<Animator> createAddTextAnimationList(TurriType.WriteRequest wr, final TurriType.Writable writable) {
        String wordBuffer = "";
        char prevCh = '|';
        long pauseAfterPrevPart = 0;
        List<Animator> animatorList = new ArrayList<>();

        for (int i = 0; i < wr.text.length(); i++) {

            // get current letter and add it to wordBuffer
            char ch = wr.text.charAt(i);
            wordBuffer = wordBuffer + ch;

            // if this is end of the word (and not just a long chain of spaces) we want to
            // create animation for this word
            if (ch == ' ' && prevCh != ' ') {

                ValueAnimator wordAnimation = createAddTextAnimation(
                        writable,
                        wordBuffer,
                        wordBuffer.length() * wr.avgTimePerChar,
                        wr.getRandomWordInterpolator());

                // add possible delays from the previous word or sentence
                wordAnimation.setStartDelay(pauseAfterPrevPart);
                animatorList.add(wordAnimation);

                // get relevant pause after this word. If it was the end of the sentence then prefer sentence pause
                // save this pause for next animation's setStartDelay
                if (prevCh == '.' || prevCh == ','){
                    pauseAfterPrevPart = wr.pauseStrategy.getPauseAfterSentence(wr.avgTimePerChar);
                } else {
                    String wordWithoutLastSpace = wordBuffer.substring(0, wordBuffer.length() - 1);
                    Log.d("TAG", wordWithoutLastSpace);
                    pauseAfterPrevPart = wr.pauseStrategy.getPauseAfterWord(wordWithoutLastSpace, wr.avgTimePerChar);
                }

                wordBuffer = ""; // prepare buffer for next word
            }

            prevCh = ch;
        }
        return animatorList;
    }


    private static ValueAnimator createAddTextAnimation(final TurriType.Writable writable, final String text, final long duration, final TimeInterpolator interpolator) {

        Log.d(TAG, "createAddTextAnimation() called with: " + "writable = [" + writable + "], text = [" + text + "], duration = [" + duration + "], interpolator = [" + interpolator + "]");

        ValueAnimator animator = ValueAnimator.ofInt(1, text.length());
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private int lastIndex = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int index = (int) animation.getAnimatedValue();
                if (index != lastIndex) {
                    String textToAdd = text.substring(lastIndex, index);
                    writable.append(textToAdd);
                }
                lastIndex = index;
            }
        });

        Log.d(TAG, "createAddTextAnimation() returned: " + animator);

        return animator;
    }

}
