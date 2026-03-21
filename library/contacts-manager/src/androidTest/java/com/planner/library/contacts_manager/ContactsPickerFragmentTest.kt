package com.planner.library.contacts_manager

import android.os.Bundle
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.Navigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.widget.Button
import android.widget.TextView
import com.planner.library.contacts_manager.api.ContactPickerContract
import com.planner.library.contacts_manager.api.ContactSelectionResult
import com.planner.library.contacts_manager.api.PickerContact
import com.planner.library.contacts_manager.permission.ContactsPermissionState
import com.planner.library.contacts_manager.testing.FakeContactsPermissionManager
import com.planner.library.contacts_manager.testing.FakeContactsRepository
import com.planner.library.contacts_manager.ui.ContactsPickerFragment
import com.planner.test.android.createTestNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ContactsPickerFragmentTest {
    private companion object {
        const val TEST_CALLER_DESTINATION = 1
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
        FakeContactsRepository.reset()
        FakeContactsPermissionManager.reset()
    }

    @Test
    fun confirmSelection_writesResultToSavedStateHandle() {
        FakeContactsRepository.contacts =
            listOf(
                PickerContact(1L, "Alex", "111"),
                PickerContact(2L, "Beth", "222"),
            )
        FakeContactsPermissionManager.currentState = ContactsPermissionState.GRANTED

        val resultKey = "contacts_result"
        val args =
            ContactPickerContract.createArgs(
                preselectedContactIds = longArrayOf(2L),
                resultKey = resultKey,
            )
        val navController = navController(args)

        val scenario =
            launchFragmentInHiltContainer<ContactsPickerFragment>(
                fragmentArgs = args,
                navController = navController,
            )

        waitUntil {
            var buttonText = ""
            scenario.onActivity { activity ->
                buttonText = activity.findViewById<Button>(R.id.done_button).text.toString()
            }
            buttonText == "Done (1)"
        }

        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.done_button).performClick()
        }

        waitUntil {
            currentSelectionResult(navController, resultKey) != null
        }

        val result = currentSelectionResult(navController, resultKey)

        requireNotNull(result)
        assertEquals(listOf("Beth"), result.contacts.map { it.name })

        scenario.close()
    }

    @Test
    fun backPress_doesNotWriteResult() {
        FakeContactsRepository.contacts =
            listOf(
                PickerContact(1L, "Alex", "111"),
            )
        FakeContactsPermissionManager.currentState = ContactsPermissionState.GRANTED

        val resultKey = "contacts_result"
        val args = ContactPickerContract.createArgs(resultKey = resultKey)
        val navController = navController(args)

        val scenario =
            launchFragmentInHiltContainer<ContactsPickerFragment>(
                fragmentArgs = args,
                navController = navController,
            )

        scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        assertNull(currentSelectionResult(navController, resultKey))

        scenario.close()
    }

    @Test
    fun rationaleState_showsExplanationAndGrantAction() {
        FakeContactsPermissionManager.currentState = ContactsPermissionState.SHOW_RATIONALE

        val scenario =
            launchFragmentInHiltContainer<ContactsPickerFragment>(
                fragmentArgs = ContactPickerContract.createArgs(resultKey = "contacts_result"),
                navController = navController(ContactPickerContract.createArgs(resultKey = "contacts_result")),
            )

        waitUntil {
            var emptyStateText = ""
            var actionText = ""
            scenario.onActivity { activity ->
                emptyStateText = activity.findViewById<TextView>(R.id.empty_state).text.toString()
                actionText = activity.findViewById<Button>(R.id.retry_button).text.toString()
            }
            emptyStateText == activityString(R.string.contacts_permission_rationale) &&
                actionText == activityString(R.string.grant_contacts_permission)
        }

        scenario.close()
    }

    @Test
    fun openSettingsState_showsSettingsAction() {
        FakeContactsPermissionManager.currentState = ContactsPermissionState.OPEN_SETTINGS

        val scenario =
            launchFragmentInHiltContainer<ContactsPickerFragment>(
                fragmentArgs = ContactPickerContract.createArgs(resultKey = "contacts_result"),
                navController = navController(ContactPickerContract.createArgs(resultKey = "contacts_result")),
            )

        waitUntil {
            var emptyStateText = ""
            var actionText = ""
            scenario.onActivity { activity ->
                emptyStateText = activity.findViewById<TextView>(R.id.empty_state).text.toString()
                actionText = activity.findViewById<Button>(R.id.retry_button).text.toString()
            }
            emptyStateText == activityString(R.string.contacts_permission_open_settings) &&
                actionText == activityString(R.string.open_settings)
        }

        scenario.close()
    }

    private fun activityString(resId: Int): String =
        ApplicationProvider.getApplicationContext<android.content.Context>().getString(resId)

    private fun navController(
        pickerArgs: Bundle,
    ): TestNavHostController =
        createTestNavController(
            context = ApplicationProvider.getApplicationContext(),
            graphId = R.navigation.contacts_manager_navigation_graph,
            currentDestination = R.id.contactsPickerFragment,
        ).also { controller ->
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val graphNavigator = controller.navigatorProvider.getNavigator(NavGraphNavigator::class.java)
                val testNavigator =
                    controller.navigatorProvider.getNavigator<Navigator<NavDestination>>("test")

                controller.graph =
                    NavGraph(graphNavigator).apply {
                        addDestination(
                            testNavigator.createDestination().apply {
                                id = TEST_CALLER_DESTINATION
                            },
                        )
                        addDestination(
                            testNavigator.createDestination().apply {
                                id = R.id.contactsPickerFragment
                            },
                        )
                        setStartDestination(TEST_CALLER_DESTINATION)
                    }
                controller.navigate(R.id.contactsPickerFragment, pickerArgs)
            }
        }

    private fun currentSelectionResult(
        navController: TestNavHostController,
        resultKey: String,
    ): ContactSelectionResult? {
        var result: ContactSelectionResult? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            result = navController.currentBackStackEntry?.savedStateHandle?.get(resultKey)
        }
        return result
    }
}
