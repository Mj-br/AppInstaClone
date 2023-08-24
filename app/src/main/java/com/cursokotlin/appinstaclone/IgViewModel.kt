package com.cursokotlin.appinstaclone

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cursokotlin.appinstaclone.data.Event
import com.cursokotlin.appinstaclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
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


    init {
//        auth.signOut()
        // Get the current user's authentication status
        val currentUser = auth.currentUser

        // Check if a user is currently signed in and update the signedIn value accordingly
        signedIn.value = currentUser != null

        // If a user is signed in, retrieve their UID and fetch user data
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    /**
     * Handles the login process.
     *
     * This function attempts to log in a user with the provided [email] and [pass].
     * It performs the following steps:
     *
     * 1. Check if the required fields (email and password) are not empty. If either is empty,
     *    it handles the exception and returns.
     * 2. Set the [inProgress] state to indicate that the login process is underway.
     * 3. Use Firebase Authentication's [signInWithEmailAndPassword] method to attempt the login.
     * 4. If the login is successful, it sets [signedIn] to true, resets the [inProgress] state,
     *    and fetches user data using the [getUserData] function.
     * 5. If the login fails, it handles the exception and resets the [inProgress] state.
     *
     * @param email The user's email address for login.
     * @param pass The user's password for login.
     */
    fun onLogin(email: String, pass: String) {
        // Ensure that the required fields are not empty
        if (email.isBlank() || pass.isBlank()) {
            // Handle the exception and return
            handleException(customMessage = "Please fill in all the required fields")
            return
        }

        // Set inProgress to true to indicate the login process has started
        inProgress.value = true

        // Attempt to sign in with the provided email and password
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful, update state and fetch user data
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        getUserData(uid)
                    }
                } else {
                    // Login failed, handle the exception and reset inProgress
                    handleException(task.exception, "Login failed")
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exc ->
                // An error occurred during login, handle the exception and reset inProgress
                handleException(exc, "Login failed")
                inProgress.value = false
            }
    }


    /**
     * Handles the sign-up process for a new user.
     *
     * @param username The desired username for the new user.
     * @param email The email address for the new user.
     * @param pass The password for the new user.
     */
    fun onSignUp(username: String, email: String, pass: String) {
        // Ensure that the required fields are not empty
        if (username.isBlank() || email.isBlank() || pass.isBlank()) {
            handleException(customMessage = "Please fill in all the required fields")
            return
        }

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
            .addOnFailureListener { exc ->
                // Handle any potential errors during the database query
                handleException(exc, "Login failed, please verify your data")
            }
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

    /**
     * Retrieves user data from the database based on the provided UID and updates the view model.
     *
     * @param uid The unique identifier of the user whose data is to be retrieved.
     */
    private fun getUserData(uid: String) {
        /* Indicate that an operation is in progress */
        inProgress.value = true

        /* Retrieve the user data document from the database */
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                /* Convert the retrieved document to a UserData object */
                val user = it.toObject<UserData>()

                /* Update the userData value with the retrieved user data */
                userData.value = user

                /* Indicate that the operation is complete */
                inProgress.value = false

                /* Show a notification to indicate that user data was successfully retrieved */
//                popupNotification.value = Event("User data retrieved successfully")
            }
            .addOnFailureListener { exc ->
                /* Handle failure to retrieve user data */
                handleException(exc, "Cannot retrieve userdata")

                /* Indicate that the operation is complete */
                inProgress.value = false
            }
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

    /**
     * Updates the user's profile data.
     *
     * This function takes the user's name, username, and bio as parameters and
     * calls the [createOrUpdateProfile] function to update the user's profile with
     * the provided information.
     *
     * @param name The user's full name.
     * @param username The user's username.
     * @param bio The user's bio or description.
     */
    fun updateProfileData(name: String, username: String, bio: String) {
        // Call the createOrUpdateProfile function to update the user's profile.
        createOrUpdateProfile(name, username, bio)
    }

   private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit){
        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener (onSuccess)
        }
            .addOnFailureListener{exc ->
                handleException(exc)
                inProgress.value = false
            }

    }

    fun uploadProfileImage(uri: Uri){
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())

        }

    }

}