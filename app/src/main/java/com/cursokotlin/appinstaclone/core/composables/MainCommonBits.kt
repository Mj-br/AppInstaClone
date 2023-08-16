package com.cursokotlin.appinstaclone.core.composables

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cursokotlin.appinstaclone.IgViewModel

/**
 * This composable function displays a notification message as a Toast in the user interface.
 *
 * @param vm The view model from which to retrieve the notification message state.
 */
@Composable
fun NotificationMessage(vm: IgViewModel) {
    // Get the notification state and its content from the view model
    val notificationState = vm.popupNotification.value
    val notificationMessage = notificationState?.getContentOrNull()

    // Check if a notification message is available
    if (notificationMessage != null) {
        // Display the notification message as a Toast with a long duration
        Toast.makeText(LocalContext.current, notificationMessage, Toast.LENGTH_LONG).show()
    }
}