package ru.dudin.diploma;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class FragmentAuthUiTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkUi() {

        // Facebook Login Button присутствует на экране
        onView(withId(R.id.view_facebook_login_button))
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkClickFlb() {

        // Клик по Facebook Login Button
        onView(withId(R.id.view_facebook_login_button))
                .perform(click());
    }
}