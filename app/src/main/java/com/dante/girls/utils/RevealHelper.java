package com.dante.girls.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import static android.view.ViewAnimationUtils.createCircularReveal;

/**
 * This helper makes startButton moves to center of revealView then reveal the view & hide the view before.
 */

public class RevealHelper {
    private static final int BUTTON_TRANSITION_DURATION = 250;
    private static final int REVEAL_DURATION = 350;
    private Activity activity;
    private View startButton;
    private View revealView;
    private View hideView;
    private float finalRadius;
    private float pixelDensity;
    private int revealX;
    private int revealY;
    private Animator.AnimatorListener onRevealEnd;
    private Animator.AnimatorListener onUnrevealEnd;
    private int hypotenuse;

    public RevealHelper(Activity activity) {
        this.activity = activity;
    }

    public RevealHelper(Activity activity, View revealView) {
        reveal(revealView);
        this.activity = activity;
        pixelDensity = activity.getResources().getDisplayMetrics().density;
    }

    public RevealHelper button(final View startButton) {
        this.startButton = startButton;
        return this;
    }

    public RevealHelper reveal(View revealView) {
        this.revealView = revealView;
        return this;
    }

    public RevealHelper build() {
        if (revealView == null) {
            throw new NullPointerException("Reveal view cannot be null, call reveal(View) first");
        }
        revealView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                revealX = revealView.getWidth() / 2;
                revealY = revealView.getHeight() / 2;
                hypotenuse = (int) Math.hypot(revealX, revealY);
            }
        });
        if (startButton == null) {
            //if no button, just reveal the revealView from center
            revealFromCenter();
            return this;
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
         /*
         MARGIN = 16dp
         FAB_BUTTON_RADIUS = 28 dp
         */
                startButton.animate()
                        .translationX(-(revealX - (16 + 28) * pixelDensity))
                        .translationY(-(revealY - (16 + 28) * pixelDensity))
//                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(BUTTON_TRANSITION_DURATION)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                revealFromCenter();
                            }
                        });

            }
        });
        return this;
    }

    public RevealHelper onRevealEnd(Animator.AnimatorListener onRevealEnd) {
        this.onRevealEnd = onRevealEnd;
        return this;
    }

    public RevealHelper onUnrevealEnd(Animator.AnimatorListener onUnrevealEnd) {
        this.onUnrevealEnd = onUnrevealEnd;
        return this;
    }

    public RevealHelper hide(View hideView) {
        this.hideView = hideView;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void revealFromCenter() {
        Animator reveal = createCircularReveal(revealView, revealX, revealY, 28 * pixelDensity, hypotenuse);
        reveal.setDuration(REVEAL_DURATION)
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        View.OnClickListener onClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                unreveal();
                            }
                        };
                        revealView.setOnClickListener(onClickListener);
                        if (onRevealEnd != null) {
                            onRevealEnd.onAnimationEnd(animation);
                        }
                    }
                });
        revealView.setVisibility(View.VISIBLE);
        if (startButton != null) {
            startButton.setVisibility(View.GONE);
        }
        if (hideView != null) {
            hideView.setVisibility(View.GONE);
        }
//        reveal.setInterpolator(new AccelerateInterpolator());
        reveal.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void unreveal() {
        if (revealView == null) {
            throw new NullPointerException("Reveal view cannot be null, call reveal(View) first");
        }

        Animator animator = ViewAnimationUtils.createCircularReveal(revealView, revealX, revealY,
                hypotenuse, 28 * pixelDensity);

        animator.setDuration(REVEAL_DURATION)
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        revealView.setVisibility(View.GONE);
                        if (startButton != null) {
                            startButton.setVisibility(View.VISIBLE);
                            startButton.animate()
                                    .translationX(0)
                                    .translationY(0)
                                    .setListener(null);
                        }
                        if (onUnrevealEnd != null) {
                            onUnrevealEnd.onAnimationEnd(animation);
                        }
                    }
                });
        animator.setInterpolator(new DecelerateInterpolator());
        if (hideView != null) {
            hideView.setVisibility(View.VISIBLE);
        }
        animator.start();

    }


}
