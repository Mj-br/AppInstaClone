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
    /**
     * Handles the sign-up process for a new user.
     *
     * @param username The desired username for the new user.
     * @param email The email address for the new user.
     * @param pass The password for the new user.
     */
    fun onSignUp(username: String, email: String, pass: String) {
        // Indicate that an operation is in progress
        inProgress.value = true

        // Check if the provided username already exists in the database
        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    // Handle the case where the username already exists
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                } else {
                    // Create a new user with the provided email and password
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Mark the user as signed in and create/update the user's profile
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                // Handle the case where sign-up failed
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener { /* Handle any potential errors */ }
    }

    /**
     * Creates or updates the user's profile data in the database.
     *
     * @param name The new name to set for the user.
     * @param username The new username to set for the user.
     * @param bio The new bio to set for the user.
     * @param imageUrl The new image URL to set for the user's profile picture.
     */
    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        // Get the current user's UID
        val uid = auth.currentUser?.uid

        // Create a UserData object with the provided or existing values
        val userData = UserData(
            userId = null,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        // If the UID is available, proceed
        uid?.let { uid ->
            // Indicate that an operation is in progress
            inProgress.value = true

            // Retrieve the user's document from the database
            db.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        // Update the existing user data
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                                inProgress.value = false
                            }
                    } else {
                        // Create a new user document with the provided data
                        db.collection(USERS).document(uid).set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure to retrieve user document
                    handleException(exception, "Cannot create user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        TODO("Not yet implemented")
    }

    /**
     * This function handles exception scenarios and displays popup notifications in the user interface.
     *
     * @param exception The exception to be handled. If provided, its stack trace will be printed to the console.
     * @param customMessage An optional custom message that can be added to the exception's error message.
     */
    private fun handleException(exception: Exception? = null, customMessage: String = "") {
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