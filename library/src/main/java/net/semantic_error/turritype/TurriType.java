package net.semantic_error.turritype;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import net.semantic_error.turritype.pausestrategy.NaturalPauseStrategy;
import net.semantic_error.turritype.pausestrategy.NoPauseStrategy;
import net.semantic_error.turritype.pausestrategy.PauseStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by semanticer on 10. 11. 2015.
 */
public class TurriType {

    public static final int VERY_SLOW_SPEED = 300;
    public static final int SLOW_SPEED = 200;
    public static final int NORMAL_SPEED = 100;
    public static final int FAST_SPEED = 50;
    public static final int VERY_FAST_SPEED = 25;

    public static WriteRequest write(String text) {
        List<TimeInterpolator> defaultWordInterpolatorList = new ArrayList<>();
        defaultWordInterpolatorList.add(new LinearInterpolator());

        return new WriteRequest(text, NORMAL_SPEED, null, defaultWordInterpolatorList, null, new NoPauseStrategy());
    }


    public static class WriteRequest {

        final String text;
        final long avgTimePerChar;
        final TimeInterpolator interpolator;
        final List<TimeInterpolator> wordInterpolatorList;
        final Animator.AnimatorListener animatorListener;
        final PauseStrategy pauseStrategy;


        private WriteRequest(String text, long avgTimePerChar, TimeInterpolator interpolator, List<TimeInterpolator> wordInterpolatorList, Animator.AnimatorListener animatorListener, PauseStrategy pauseStrategy) {

            this.text = text;
            this.avgTimePerChar = avgTimePerChar;
            this.interpolator = interpolator;
            this.wordInterpolatorList = wordInterpolatorList;
            this.animatorListener = animatorListener;
            this.pauseStrategy = pauseStrategy;
        }

        // TODO rename to reflect that it is millisecond per char not total animation time
        public WriteRequest during(long millis) {
            return new WriteRequest(text, millis, interpolator, wordInterpolatorList, animatorListener, pauseStrategy);
        }

        public WriteRequest withListener(Animator.AnimatorListener animatorListener) {
            return new WriteRequest(text, avgTimePerChar, interpolator, wordInterpolatorList, animatorListener, pauseStrategy);
        }

        public WriteRequest withInterpolator(TimeInterpolator interpolator) {
            return new WriteRequest(text, avgTimePerChar, interpolator, new ArrayList<TimeInterpolator>(), animatorListener, pauseStrategy);
        }

        public WriteRequest withWordInterpolator(TimeInterpolator wordInterpolator) {
            List<TimeInterpolator> wordInterpolatorList = new ArrayList<>();
            wordInterpolatorList.add(wordInterpolator);
            return new WriteRequest(text, avgTimePerChar, null, wordInterpolatorList, animatorListener, pauseStrategy);
        }

        public WriteRequest withWordInterpolator(List<TimeInterpolator> wordInterpolatorList) {
            return new WriteRequest(text, avgTimePerChar, null, wordInterpolatorList, animatorListener, pauseStrategy);
        }

        public Animator into(TextView tv) {
            return TypeAnimationFactory.create(this, tv);
        }


        public WriteRequest naturally() {
            ArrayList<TimeInterpolator> naturalWordInterpolatorList = new ArrayList<>();

            naturalWordInterpolatorList.add(new AccelerateDecelerateInterpolator()); // This one works best so far
            naturalWordInterpolatorList.add(new DecelerateInterpolator());
            naturalWordInterpolatorList.add(new FastOutSlowInInterpolator());
            naturalWordInterpolatorList.add(new FastOutLinearInInterpolator());
            naturalWordInterpolatorList.add(new LinearInterpolator());

            return this.setPauseStrategy(new NaturalPauseStrategy()).withWordInterpolator(naturalWordInterpolatorList);
        }

        public WriteRequest setPauseStrategy(PauseStrategy pauseStrategy) {
            return new WriteRequest(text, avgTimePerChar, interpolator, wordInterpolatorList, animatorListener, pauseStrategy);
        }

        TimeInterpolator getRandomWordInterpolator() {
            if (wordInterpolatorList == null || wordInterpolatorList.size() == 0) {
                throw new IllegalStateException("No word interpolator available");

            } else if (wordInterpolatorList.size() == 1) {
                return wordInterpolatorList.get(0);

            } else {
                Random rand = new Random();
                int randomIndex = rand.nextInt(wordInterpolatorList.size());
                return wordInterpolatorList.get(randomIndex);
            }
        }

    }
}