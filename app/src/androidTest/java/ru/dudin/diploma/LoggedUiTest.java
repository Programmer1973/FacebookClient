package ru.dudin.diploma;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoggedUiTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkUi() {

        // ToolBar присутствует на экране
        onView(withId(R.id.view_toolbar))
                .check(matches(isDisplayed()));

        // Floating Action Button присутствует на экране
        onView(withId(R.id.view_fab))
                .check(matches(isDisplayed()));

        // RecyclerView присутствует на экране
        onView(withId(R.id.view_list))
                .check(matches(isDisplayed()));
    }


    @Test
    public void checkCreateAndUpdatePosts() {

        /**
         *  Создание 10-ти постов
         */
        for(int i = 0; i < 10; i++) {
            // Клик по Floating Action Button
            onView(withId(R.id.view_fab))
                    .perform(click());

            // Отображение фрагмента пост диалога (AlertDialog) из CreatePostDialogFragment
            onView(withText(R.string.create_post))
                    .check(matches(isDisplayed()));

            // Заполнение поля ввода текста (TextInputEditText) во фрагменте поста диалога
            // (AlertDialog) из CreatePostDialogFragment
            onView(withId(R.id.view_post))
                    .perform(typeText(mActivityRule.getActivity().getString(R.string.post_number_for_test) + String.valueOf(i)));

            // Отображение поста в CardView
            onView(withText(R.string.create))
                    .perform(click());
        }

        /**
         * Обновление всех имеющихся постов
         */
        RecyclerView recyclerView = mActivityRule.getActivity().findViewById(R.id.view_list);
        int mItemCount = recyclerView.getAdapter().getItemCount();

        for(int i = 0; i < mItemCount; i++) {

            // Клик по CardView
            onView(ViewMatchers.withId(R.id.view_list))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));

            // Отображение фрагмента пост диалога (AlertDialog) из UpdatePostDialogFragment
            onView(withText(R.string.update_post))
                    .check(matches(isDisplayed()));

            // Заполнение поля ввода текста (TextInputEditText) во фрагменте поста диалога
            // (AlertDialog) из UpdatePostDialogFragment
            // (значение: порядковый номер ViewHolder-a умноженный на 100)
            onView(withId(R.id.view_post))
                    .perform(typeText(mActivityRule.getActivity().getString(R.string.edited_post_number_for_test) + String.format("%d", i * 100)));

            // Отображение обновлённого поста в CardView
            onView(withText(R.string.update))
                    .perform(click());
        }
    }
}