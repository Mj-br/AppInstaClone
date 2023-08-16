package com.cursokotlin.appinstaclone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cursokotlin.appinstaclone.data.Event
import com.cursokotlin.appinstaclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

const val USERS = "users"

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    fun onSignUp(username: String, email: String, pass: String) {
        inProgress.value = true

        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    handleExeption(customMessage = "Username already exists")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                handleExeption(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }

            }
            .addOnFailureListener { }

    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = null,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

    }

    /**
     * This function handles exception scenarios and displays popup notifications in the user interface.
     *
     * @param exception The exception to be handled. If provided, its stack trace will be printed to the console.
     * @param customMessage An optional custom message that can be added to the exception's error message.
     */
    private fun handleExeption(exception: Exception? = null, customMessage: String = "") {
        // Check if an exception is provided and print its stack trace to the console (for debugging)
        exception?.printStackTrace()

        // Get the error message from the exception or set an empty string if no exception
        val errorMessage = exception?.localizedMessage ?: ""

        // Combine the exception's error message with a custom message if provided
        val message = if (customMessage.isEmpty()) errorMessage else "$customMessage: $errorMessage"

        // Display a popup notification in the user interface with the combined message
        popupNotification.value = Event(message)

    }

}