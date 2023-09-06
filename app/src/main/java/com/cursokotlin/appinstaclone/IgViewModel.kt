package com.cursokotlin.appinstaclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cursokotlin.appinstaclone.data.Event
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"

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

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())


    init {
//        auth.signOut()
        // Get the current user's authentication status
        val currentUser = auth.currentUser

        // Check if a user is currently signed in and update the signedIn value accordingly
        signedIn.value = currentUser != null

        // If a user is signed in, retrieve their UID
        currentUser?.uid?.let { uid ->
            // Fetch user data based on the UID
            getUserData(uid)

            // Refresh the user's posts
            refreshPosts()
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
            userId = uid,
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

    /**
     * Uploads an image to Firebase Storage.
     *
     * This function takes a [uri] representing the image file to upload and a [onSuccess] callback
     * to be executed when the upload is successful.
     *
     * @param uri The URI of the image to upload.
     * @param onSuccess A callback to execute when the upload is successful, passing the download URI.
     */
    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        // Set inProgress to true to indicate the upload process has started
        inProgress.value = true

        // Get a reference to the Firebase Storage
        val storageRef = storage.reference

        // Generate a unique identifier (UUID) for the image
        val uuid = UUID.randomUUID()

        // Create a reference to the image in Firebase Storage
        val imageRef = storageRef.child("images/$uuid")

        // Upload the image using putFile method
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            // When upload is successful, get the download URL
            val result = it.metadata?.reference?.downloadUrl

            // Execute the onSuccess callback with the download URI
            result?.addOnSuccessListener(onSuccess)
        }
            .addOnFailureListener { exc ->
                // An error occurred during upload, handle the exception and reset inProgress
                handleException(exc)
                inProgress.value = false
            }
    }

    /**
     * Uploads a user's profile image.
     *
     * This function takes a [uri] representing the image file to upload as the user's profile image.
     * It calls the [uploadImage] function to perform the upload and, on success, updates the user's
     * profile with the uploaded image URL.
     *
     * @param uri The URI of the profile image to upload.
     */
    fun uploadProfileImage(uri: Uri) {
        // Call the uploadImage function to upload the profile image
        uploadImage(uri) { imageUrl ->
            // When the image upload is successful, update the user's profile with the image URL
            createOrUpdateProfile(imageUrl = imageUrl.toString())
            // Update user image data for their posts
            updatePostUserImageData(imageUrl.toString())
        }
    }

    /**
     * Updates the user image data for the posts created by the current user.
     *
     * This function retrieves all posts created by the current user, updates their user image
     * references with the provided [imageUrl], and refreshes the posts to reflect the changes.
     *
     * @param imageUrl The URL of the user's updated profile image.
     */
    private fun updatePostUserImageData(imageUrl: String) {
        // Get the current user's UID
        val currentUid = auth.currentUser?.uid

        // Query posts where the user ID matches the current user's UID
        db.collection(POSTS).whereEqualTo("userId", currentUid).get()
            .addOnSuccessListener { querySnapshot ->
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                // Convert query results to a list of PostData
                convertPosts(querySnapshot, posts)

                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }

                // If there are posts to update, perform a batch update to set the userImage field
                if (refs.isNotEmpty()) {
                    db.runBatch { batch ->
                        for (ref in refs) {
                            batch.update(ref, "userImage", imageUrl)
                        }
                    }
                        .addOnSuccessListener {
                            // Refresh the posts to reflect the updated user image
                            refreshPosts()
                        }
                }
            }
    }


    /**
     * Logs the user out of the application.
     *
     * This function signs the user out of their account, updates the authentication state,
     * clears the user data, and displays a logout notification.
     */
    fun onLogout() {
        // Sign the user out of their account
        auth.signOut()

        // Update the authentication state to indicate that the user is no longer signed in
        signedIn.value = false

        // Clear the user data
        userData.value = null

        // Display a logout notification using an event
        popupNotification.value = Event("Logged out")
    }

    /**
     * Handles the creation of a new post with an image and description.
     *
     * This function takes an [uri] representing the image file to be posted,
     * a [description] for the post, and a [onPostSuccess] callback to be executed when the post is successful.
     * It performs the following steps:
     *
     * 1. Uploads the image using the [uploadImage] function.
     * 2. Calls [onCreatePost] with the image URL, description, and [onPostSuccess] callback.
     *
     * @param uri The URI of the image to be posted.
     * @param description The description for the new post.
     * @param onPostSuccess A callback to execute when the post is successful.
     */
    fun onNewPost(uri: Uri, description: String, onPostSuccess: () -> Unit) {
        // Upload the image using the uploadImage function
        uploadImage(uri) {
            // Call onCreatePost with the image URL, description, and onPostSuccess callback
            onCreatePost(it, description, onPostSuccess)
        }
    }

    /**
     * Creates a new post with the provided image URL, description, and user information.
     *
     * This function performs the following steps:
     *
     * 1. Sets inProgress to true to indicate the post creation process has started.
     * 2. Retrieves the current user's UID, username, and user image from the authentication and user data.
     * 3. If the current user's UID is available, generates a unique post UUID.
     * 4. Creates a new PostData object with the provided information.
     * 5. Saves the post data to the Firestore database.
     * 6. Handles success by displaying a success notification, resetting inProgress, and invoking onPostSuccess callback.
     * 7. Handles failure by handling the exception, resetting inProgress, and displaying an error message.
     *
     * @param imageUri The URI of the image to be posted.
     * @param description The description for the new post.
     * @param onPostSuccess A callback to execute when the post is successful.
     */
    private fun onCreatePost(imageUri: Uri, description: String, onPostSuccess: () -> Unit) {
        // Set inProgress to true to indicate the post creation process has started
        inProgress.value = true

        // Retrieve current user information
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData?.value?.username
        val currentUserImage = userData?.value?.imageUrl

        // Check if the current user's UID is available
        if (currentUid != null) {
            // Generate a unique post UUID
            val postUuid = UUID.randomUUID().toString()

            val fillerWords = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")
            val searchTerms = description
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }


            // Create a new PostData object with the provided information
            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )

            // Save the post data to the Firestore database
            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    // Handle success by displaying a success notification, resetting inProgress, and invoking onPostSuccess
                    popupNotification.value = Event("Post successfully created")
                    inProgress.value = false
                    // Refresh the user's posts
                    refreshPosts()

                    // Invoke the onPostSuccess callback
                    onPostSuccess.invoke()
                }
                .addOnFailureListener { exc ->
                    // Handle failure by handling the exception, resetting inProgress, and displaying an error message
                    handleException(exc, "Unable to create post")
                    inProgress.value = false
                }
        } else {
            // Handle the case where the current user's UID is not available
            handleException(customMessage = "Error, username unavailable. Unable to create post")
            onLogout()
            inProgress.value = false
        }
    }

    /**
     * Refreshes the user's posts and updates the post list in the app's state.
     *
     * This function performs the following steps:
     *
     * 1. Retrieves the current user's UID from authentication.
     * 2. If the current UID is available, sets refreshPostsProgress to true to indicate the refresh process has started.
     * 3. Queries the Firestore database for posts where the "userId" field matches the current UID.
     * 4. On success, converts the retrieved documents into PostData objects and updates the post list.
     * 5. Sets refreshPostsProgress to false to indicate the refresh process is complete.
     * 6. On failure, handles the exception and sets refreshPostsProgress to false.
     * 7. If the current UID is not available, handles the case where the username is unavailable and logs the user out.
     */
    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            // Set refreshPostsProgress to true to indicate the refresh process has started
            refreshPostsProgress.value = true

            // Query Firestore for posts where the "userId" field matches the current UID
            db.collection(POSTS).whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    // Convert the retrieved documents into PostData objects and update the post list
                    convertPosts(documents, posts)

                    // Set refreshPostsProgress to false to indicate the refresh process is complete
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    // Handle failure by displaying an error message, and set refreshPostsProgress to false
                    handleException(exc, "Cannot fetch posts")
                    refreshPostsProgress.value = false
                }
        } else {
            // Handle the case where the current UID is not available
            handleException(customMessage = "Error, username unavailable. Unable to refresh posts")

            // Log the user out
            onLogout()
        }
    }

    /**
     * Converts Firestore query results into a list of PostData objects and sorts them by time.
     *
     * @param documents The Firestore query results.
     * @param outState A mutable state that holds the list of posts to be updated.
     */
    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val newPosts = mutableListOf<PostData>()
        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }

        // Sort the new posts by descending time
        val sortedPosts = newPosts.sortedByDescending { it.time }

        // Update the post list in the app's state
        outState.value = sortedPosts
    }

    /**
     * Retrieves a post with a specified [postId] from the list of posts.
     *
     * @param postId The unique identifier of the post to retrieve.
     * @return The [PostData] object with the matching [postId], or null if not found.
     */
    fun getPostById(postId: String): PostData? {
        // Use the 'firstOrNull' function to find the first post with a matching 'postId'
        // in the list of posts ('posts.value').
        // If a matching post is found, it is returned; otherwise, 'null' is returned.
        return posts.value.firstOrNull { it.postId == postId }
    }



}