package org.medicmobile.webapp.mobile;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.hasElementWithId;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.clearElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.web.webdriver.DriverAtoms;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Locale;


@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SettingsDialogActivityTest {

	private static final String WEBAPP_URL = "CHT server URL";
	private static final String SERVER_ONE = "https://medic.github.io/atp";
	private static final String SERVER_TWO = "https://gamma-cht.dev.medicmobile.org";
	private static final String SERVER_THREE = "https://gamma.dev.medicmobile.org";
	private static final String ERROR_INCORRECT = "Incorrect user name or password. Please try again.";

	@Rule
	public ActivityScenarioRule<SettingsDialogActivity> mActivityTestRule =
			new ActivityScenarioRule<>(SettingsDialogActivity.class);

	@Test
	public void serverSelectionScreenIsDisplayed() {
		onView(withText("CHT Android")).check(matches(isDisplayed()));
		onView(withId(R.id.instanceSearchBox)).check(matches(isDisplayed()));
		onView(withText("Custom")).check(matches(isDisplayed()));
		onView(withId(R.id.lstServers)).check(matches(isDisplayed()));

		onView(withText(SERVER_ONE)).check(matches(isDisplayed()));
		onView(withText(SERVER_TWO)).check(matches(isDisplayed()));
		onView(withText(SERVER_THREE)).check(matches(isDisplayed()));

		onView(withText("Custom")).perform(click());
		ViewInteraction textAppUrl = onView(withId(R.id.txtAppUrl));
		textAppUrl.check(matches(withHint(WEBAPP_URL)));

		textAppUrl.perform(replaceText("something"), closeSoftKeyboard());
		onView(withId(R.id.btnSaveSettings)).perform(click());
		textAppUrl.check(matches(hasErrorText("Must be a valid URL")));
		pressBack();

	}

	@Test
	public void testLoginScreen() throws Exception {
		onData(anything())
			.inAdapterView(withId(R.id.lstServers))
			.atPosition(1)
			.perform(click());

		onView(withText("Login to Gamma Dev?"))
			.inRoot(isDialog())
			.check(matches(isDisplayed()));
		onView(withText("Cancel"))
			.inRoot(isDialog())
			.check(matches(isDisplayed()));

		onView(withText("Continue"))
			.inRoot(isDialog())
			.perform(click());

		Thread.sleep(28000);	//TODO: use better ways to handle delays

		ViewInteraction webView = onView(
				allOf(withId(R.id.wbvMain),
						withParent(allOf(withId(R.id.lytWebView),
								withParent(withId(android.R.id.content)))),
						isDisplayed()));
		webView.check(matches(isDisplayed()));
		onWebView()
				.withNoTimeout()
				.check(webContent(hasElementWithId("form")))
				.withElement(findElement(Locator.ID, "locale"))
				.check(webMatches(getText(), containsString("English")));
		String[] codes = {"es", "en", "fr", "sw"};
		for (String code : codes) {
			onWebView()
					.withNoTimeout()
					.withElement(findElement(Locator.NAME, code))
					.check(webMatches(getText(), containsString(getLanguage(code))));
		}

		// Ensure language set is English
		onWebView()
				.withNoTimeout()
				.withElement(findElement(Locator.NAME, "en"))
				.perform(webClick());

		//login form and errors
		onWebView()
				.withNoTimeout()
				.withElement(findElement(Locator.ID, "user"))
				.perform(clearElement())
				.perform(DriverAtoms.webKeys("fakename"))    //to be created first
				.withElement(findElement(Locator.ID, "password"))
				.perform(clearElement())
				.perform(DriverAtoms.webKeys("fake_password"))
				.withElement(findElement(Locator.ID, "login"))
				.perform(webClick());
		Thread.sleep(4000);//TODO: use better ways to handle delays - takes longer with emulators
		onWebView()
				.withNoTimeout()
				.withElement(findElement(Locator.CSS_SELECTOR, "p.error.incorrect"))
				.check(webMatches(getText(), containsString(ERROR_INCORRECT)));
	}

	@Test
	public void testCancelSelectedServer() {
		onData(anything())
			.inAdapterView(withId(R.id.lstServers))
			.atPosition(2)
			.perform(click());

		onView(withText(R.string.btnCancel))
			.inRoot(isDialog())
			.perform(click());

		onView(withText("CHT Android")).check(matches(isDisplayed()));
		onView(withText("Custom")).check(matches(isDisplayed()));
		onView(withId(R.id.lstServers)).check(matches(isDisplayed()));
	}

	@Test
	public void testFilterServers() throws InterruptedException {
		onView(withId(R.id.lstServers)).check(matches(isDisplayed()));
		onView(withText("Custom")).check(matches(isDisplayed()));
		onView(withText(SERVER_ONE)).check(matches(isDisplayed()));
		onView(withText(SERVER_TWO)).check(matches(isDisplayed()));
		onView(withText(SERVER_THREE)).check(matches(isDisplayed()));

		ViewInteraction textFilter = onView(withId(R.id.instanceSearchBox));
		textFilter.check(matches(withHint("Search Instances")));
		textFilter.perform(replaceText("github"), closeSoftKeyboard());
		Thread.sleep(500);

		onView(withId(R.id.lstServers)).check(matches(isDisplayed()));
		onView(withText("Custom")).check(doesNotExist());
		onView(withText(SERVER_ONE)).check(matches(isDisplayed()));
		onView(withText(SERVER_TWO)).check(doesNotExist());
		onView(withText(SERVER_THREE)).check(doesNotExist());

		textFilter.perform(replaceText(""), closeSoftKeyboard());
		Thread.sleep(500);

		onView(withId(R.id.lstServers)).check(matches(isDisplayed()));
		onView(withText("Custom")).check(matches(isDisplayed()));
		onView(withText(SERVER_ONE)).check(matches(isDisplayed()));
		onView(withText(SERVER_TWO)).check(matches(isDisplayed()));
		onView(withText(SERVER_THREE)).check(matches(isDisplayed()));

		textFilter.perform(replaceText("gamma"), closeSoftKeyboard());
		Thread.sleep(500);

		onView(withId(R.id.lstServers)).check(matches(isDisplayed()));
		onView(withText("Custom")).check(doesNotExist());
		onView(withText(SERVER_ONE)).check(doesNotExist());
		onView(withText(SERVER_TWO)).check(matches(isDisplayed()));
		onView(withText(SERVER_THREE)).check(matches(isDisplayed()));

		onData(anything())
			.inAdapterView(withId(R.id.lstServers))
			.atPosition(0)
			.perform(click());

		onView(withText("Login to Gamma Dev?"))
			.inRoot(isDialog())
			.check(matches(isDisplayed()));
	}

	private String getLanguage(String code) {
		Locale aLocale = new Locale(code);
		return aLocale.getDisplayName();
	}
}
