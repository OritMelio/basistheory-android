package com.basistheory.android.example

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.basistheory.android.example.util.waitUntilVisible
import com.basistheory.android.example.view.MainActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

class ProxyRevealTests {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Before
    fun before() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.nav_reveal)).perform(click())
    }

    @Test
    fun canProxyAndReveal() {
        val cardNumber = "4242 4242 4242 4242"
        val expMonth = "11"
        val expYear = (LocalDate.now().year + 1).toString()
        val cvc = "123"

        onView(withId(R.id.card_number)).perform(scrollTo(), typeText(cardNumber))
        onView(withId(R.id.expiration_date)).perform(
            scrollTo(),
            typeText("$expMonth/${expYear.takeLast(2)}")
        )
        onView(withId(R.id.cvc)).perform(scrollTo(), typeText(cvc))

        onView(withId(R.id.tokenize_button)).perform(closeSoftKeyboard(), click())

        onView(withId(R.id.result)).perform(waitUntilVisible())

        onView(withId(R.id.reveal_button)).perform(click())

        onView(withId(R.id.result)).perform(waitUntilVisible())
        onView(withId(R.id.proxy_result)).perform(waitUntilVisible())

        // assertions on read only elements
        onView(allOf(
            withHint("Revealed Card Number"),
            withText(cardNumber)
        )).check(matches(isDisplayed()))

        onView(allOf(
            withHint("Revealed Expiration Date"),
            withText("11/${expYear.takeLast(2)}")
        )).check(matches(isDisplayed()))

        onView(allOf(
            withHint("Revealed CVC"),
            withText(cvc)
        )).check(matches(isDisplayed()))
    }

    @Test
    fun revealedElementsAreReadOnly() {
        onView(withId(R.id.revealedCardNumber))
            .perform(scrollTo())
            .check(matches(allOf(isDisplayed(), not(isEnabled()))))

        onView(withId(R.id.revealedExpirationDate))
            .perform(scrollTo())
            .check(matches(allOf(isDisplayed(), not(isEnabled()))))

        onView(withId(R.id.revealedCvc))
            .perform(scrollTo())
            .check(matches(allOf(isDisplayed(), not(isEnabled()))))
    }
}
