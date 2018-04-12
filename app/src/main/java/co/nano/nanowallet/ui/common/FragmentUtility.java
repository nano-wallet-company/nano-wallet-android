package co.nano.nanowallet.ui.common;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import co.nano.nanowallet.R;
import co.nano.nanowallet.util.ExceptionHandler;

/**
 * Utility methods for adding and replacing fragment transitions and animations
 */

public class FragmentUtility {
    private FragmentManager mFragmentManager;
    private int mContainerViewId;

    public FragmentUtility(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void addOnBackStackChangedListener(FragmentManager.OnBackStackChangedListener listener) {
        mFragmentManager.addOnBackStackChangedListener(listener);
    }

    public void removeOnBackStackChangedListener(FragmentManager.OnBackStackChangedListener listener) {
        mFragmentManager.removeOnBackStackChangedListener(listener);
    }


    /**
     * Adds a fragment to the container while adding the transaction to the backstack.
     */
    public void add(Fragment fragment) {
        add(fragment, Animation.NONE);
    }

    public void add(Fragment fragment, Animation animations) {
        add(fragment, animations, animations);
    }

    public void add(Fragment fragment, Animation pushAnimations, Animation popAnimations) {
        add(fragment, pushAnimations, popAnimations, null);
    }

    public void add(Fragment fragment,
                    Animation pushAnimations,
                    Animation popAnimations,
                    @Nullable String tag) {
        add(fragment, pushAnimations, popAnimations, tag, null);
    }

    public void add(Fragment fragment,
                    Animation pushAnimations,
                    Animation popAnimations,
                    @Nullable String tag,
                    @Nullable View sharedElement) {
        performAddTransaction(fragment, pushAnimations, popAnimations, tag, true, sharedElement);
    }

    /**
     * Replaces one fragment with another without adding the transaction to the backstack.
     */
    public void replace(Fragment fragment) {
        replace(fragment, Animation.NONE);
    }

    public void replace(Fragment fragment, Animation animations) {
        replace(fragment, animations, animations);
    }

    public void replace(Fragment fragment, Animation pushAnimations, Animation popAnimations) {
        replace(fragment, pushAnimations, popAnimations, null);
    }

    public void replace(Fragment fragment,
                        Animation pushAnimations,
                        Animation popAnimations,
                        @Nullable String tag) {
        performReplaceTransaction(fragment, pushAnimations, popAnimations, tag, false);
    }

    /**
     * Replaces one fragment with another while adding the transaction to the backstack.
     */
    public void push(Fragment fragment) {
        push(fragment, null);
    }

    public void push(Fragment fragment, @Nullable String tag) {
        push(fragment, Animation.NONE, tag);
    }

    public void push(Fragment fragment, Animation animations, @Nullable String tag) {
        push(fragment, animations, animations, tag);
    }

    public void push(Fragment fragment,
                     Animation pushAnimations,
                     Animation popAnimations,
                     @Nullable String tag) {
        replace(fragment, pushAnimations, popAnimations, tag);
    }

    /**
     * Pops the most recent transaction off the backstack.
     */
    public void pop() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack();
        }
    }

    /**
     * Pops the fragment matching the given tag off the backstack.
     */
    public void pop(String tag) {
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);

        if (fragment != null) {
            performRemoveTransaction(fragment);
        }
    }

    /**
     * Pops the given fragment off the backstack.
     */
    public void pop(Fragment fragment) {
        if (fragment != null) {
            performRemoveTransaction(fragment);
        }
    }

    /**
     * Clear the entire stack
     */
    public void clearStack() {
        //Here we are clearing back stack fragment entries
        int backStackEntry = mFragmentManager.getBackStackEntryCount();
        if (backStackEntry > 0) {
            for (int i = 0; i < backStackEntry; i++) {
                try {
                    mFragmentManager.popBackStackImmediate();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        //Here we are removing all the fragment that are shown here
        if (mFragmentManager.getFragments() != null && mFragmentManager.getFragments().size() > 0) {
            for (int i = 0; i < mFragmentManager.getFragments().size(); i++) {
                Fragment mFragment = mFragmentManager.getFragments().get(i);
                if (mFragment != null) {
                    try {
                        mFragmentManager.beginTransaction().remove(mFragment).commitAllowingStateLoss();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            }
        }
    }

    /**
     * Performs a fragment addition transaction with all of the necessary animations and tags.
     */
    public void performAddTransaction(Fragment fragment,
                                      Animation pushAnimations,
                                      Animation popAnimations,
                                      @Nullable String tag,
                                      boolean addToBackStack,
                                      View sharedElement) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction()
                .setCustomAnimations(
                        pushAnimations.getEnter(),
                        popAnimations.getExit(),
                        popAnimations.getEnter(),
                        pushAnimations.getExit()
                )
                .replace(mContainerViewId, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commitAllowingStateLoss();
    }

    /**
     * Performs a fragment replacement transaction with all of the necessary animations and tags.
     */
    public void performReplaceTransaction(Fragment fragment,
                                          Animation pushAnimations,
                                          Animation popAnimations,
                                          @Nullable String tag,
                                          boolean addToBackStack) {
        FragmentTransaction transaction;
        if (tag == null) {
            transaction = mFragmentManager.beginTransaction()
                    .setCustomAnimations(
                            pushAnimations.getEnter(),
                            popAnimations.getExit(),
                            popAnimations.getEnter(),
                            pushAnimations.getExit()
                    )
                    .replace(mContainerViewId, fragment);
        } else {
            transaction = mFragmentManager.beginTransaction()
                    .setCustomAnimations(
                            pushAnimations.getEnter(),
                            popAnimations.getExit(),
                            popAnimations.getEnter(),
                            pushAnimations.getExit()
                    )
                    .replace(mContainerViewId, fragment, tag);
        }

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commitAllowingStateLoss();
    }

    public void performRemoveTransaction(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss();
    }

    public int getBackStackEntryCount() {
        return mFragmentManager.getBackStackEntryCount();
    }

    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    public int getContainerViewId() {
        return mContainerViewId;
    }

    public void setContainerViewId(int containerViewId) {
        this.mContainerViewId = containerViewId;
    }

    public enum Animation {
        NONE(0, 0),
        CROSSFADE(android.R.animator.fade_in, android.R.animator.fade_out),
        ENTER_UP_EXIT_DOWN(R.anim.enter_slide_up, R.anim.exit_slide_down),
        ENTER_LEFT_EXIT_RIGHT(R.anim.enter_slide_left, R.anim.exit_slide_right),
        ENTER_RIGHT_EXIT_LEFT(R.anim.enter_slide_right, R.anim.exit_slide_left);

        final int mEnter;
        final int mExit;

        Animation(int enter, int exit) {
            mEnter = enter;
            mExit = exit;
        }

        public int getEnter() {
            return mEnter;
        }

        public int getExit() {
            return mExit;
        }
    }

    public void cleanUp() {
        mFragmentManager = null;
    }

}
