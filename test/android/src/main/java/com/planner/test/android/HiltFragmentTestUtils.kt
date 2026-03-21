package com.planner.test.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry

inline fun <reified F : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    navController: TestNavHostController? = null,
    crossinline instantiate: () -> F = {
        F::class.java.getDeclaredConstructor().newInstance()
    },
    crossinline action: F.() -> Unit = {},
): ActivityScenario<HiltTestActivity> {
    val intent = Intent(ApplicationProvider.getApplicationContext(), HiltTestActivity::class.java)

    return ActivityScenario.launch<HiltTestActivity>(intent).also { scenario ->
        scenario.onActivity { activity ->
            val fragment = instantiate()
            if (fragment.arguments == null && fragmentArgs != null) {
                fragment.arguments = fragmentArgs
            }

            activity.supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, fragment, F::class.java.name)
                .commitNow()

            navController?.let { controller ->
                Navigation.setViewNavController(fragment.requireView(), controller)
            }

            fragment.action()
        }
    }
}

fun createTestNavController(
    context: Context,
    graphId: Int,
    currentDestination: Int,
): TestNavHostController {
    return TestNavHostController(context).also { controller ->
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            controller.setViewModelStore(ViewModelStore())
            controller.setGraph(graphId)
            controller.setCurrentDestination(currentDestination)
        }
    }
}

fun waitUntil(
    timeoutMillis: Long = 5_000,
    condition: () -> Boolean,
) {
    val deadline = SystemClock.elapsedRealtime() + timeoutMillis

    while (SystemClock.elapsedRealtime() < deadline) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        if (condition()) {
            return
        }
        SystemClock.sleep(50)
    }

    throw AssertionError("Condition was not met within $timeoutMillis ms")
}

fun createNavController(
    graphId: Int,
    currentDestination: Int,
) = createTestNavController(
    context = ApplicationProvider.getApplicationContext(),
    graphId = graphId,
    currentDestination = currentDestination,
)