package com.cursokotlin.appinstaclone

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cursokotlin.appinstaclone.data.CommentData
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
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val USERS = "users"
const val POSTS = "posts"
const val COMMENTS = "comments"


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

    val searchedPosts = mutableStateOf<List<PostData>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)

    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)

    val comments = mutableStateOf<List<CommentData>>(listOf())
    val commentsProgress = mutableStateOf(false)


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

                /* Refresh userData posts */
                refreshPosts()

                getPersonalizedFeed()

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

        //Clear the searched list
        searchedPosts.value = listOf()

        //Clear postsFeed value
        postsFeed.value = listOf()

        // Clear the comments data
        comments.value = listOf()

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
     *
     * Additionally, this function performs keyword extraction from the post description:
     *  - Splits the description into words and removes common filler words in both English and Spanish.
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

            val fillerWords = listOf(
                // English Filler Words
                "the",
                "be",
                "to",
                "is",
                "of",
                "and",
                "or",
                "a",
                "in",
                "it",
                "I",
                "you",
                "he",
                "she",
                "we",
                "they",
                "my",
                "your",
                "his",
                "her",
                "its",
                "our",
                "their",
                "mine",
                "yours",
                "hers",
                "ours",
                "theirs",
                "this",
                "that",
                "these",
                "those",
                "am",
                "are",
                "was",
                "were",
                "have",
                "has",
                "had",
                "do",
                "does",
                "did",
                "can",
                "could",
                "will",
                "would",
                "shall",
                "should",
                "may",
                "might",
                "must",
                "of",
                "off",
                "by",
                "for",
                "with",
                "about",
                "against",
                "between",
                "into",
                "through",
                "during",
                "before",
                "after",
                "above",
                "below",
                "under",
                "over",
                "around",
                "throughout",
                "up",
                "down",
                "upon",
                "toward",
                "against",
                "aboard",
                "along",
                "amid",
                "among",
                "beside",
                "between",
                "beyond",
                "concerning",
                "considering",
                "despite",
                "except",
                "inside",
                "outside",
                "regarding",
                "respecting",
                "towards",
                "beneath",
                "betwixt",
                "past",
                "except",
                "pending",
                "till",
                "via",
                "worth",
                "-",

                // Spanish Filler Words
                "en",
                "el",
                "la",
                "las",
                "a",
                "es",
                "de",
                "y",
                "o",
                "lo",
                "los",
                "yo",
                "tú",
                "él",
                "ella",
                "nosotros",
                "vosotros",
                "ellos",
                "ellas",
                "mi",
                "tu",
                "su",
                "nuestro",
                "vuestro",
                "suyo",
                "mío",
                "tuyo",
                "nuestro",
                "vuestro",
                "suyo",
                "este",
                "ese",
                "aquel",
                "esta",
                "esa",
                "aquella",
                "estos",
                "esos",
                "aquellos",
                "estas",
                "esas",
                "aquellas",
                "soy",
                "eres",
                "es",
                "somos",
                "sois",
                "son",
                "fui",
                "fuiste",
                "fue",
                "fuimos",
                "fuisteis",
                "fueron",
                "soy",
                "eres",
                "es",
                "sois",
                "era",
                "eras",
                "éramos",
                "erais",
                "eran",
                "he",
                "has",
                "ha",
                "hemos",
                "habéis",
                "han",
                "hago",
                "haces",
                "hace",
                "hacemos",
                "hacéis",
                "hacen",
                "haré",
                "harás",
                "hará",
                "haremos",
                "haréis",
                "harán",
                "puedo",
                "puedes",
                "puede",
                "podemos",
                "podéis",
                "pueden",
                "puede",
                "podrás",
                "podrá",
                "podremos",
                "podréis",
                "podrán",
                "debo",
                "debes",
                "debe",
                "debemos",
                "debéis",
                "deben",
                "debe",
                "deberás",
                "deberá",
                "deberemos",
                "deberéis",
                "deberán",
                "puede",
                "podría",
                "podrías",
                "podríamos",
                "podríais",
                "podrían",
                "mis",
                "para",
                "un",
                "s"
                /* Note: You can customize the list further with additional filler words */
            )


            // Extract relevant search terms from the description
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
    private fun convertPosts(querySnapshot: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val newPosts = mutableListOf<PostData>()

        querySnapshot.forEach { document ->
            val post = document.toObject<PostData>()
            newPosts.add(post)
        }

        // Get the current list of posts
        val currentPosts = outState.value.toMutableList()

        // Update the existing posts with the new posts
        newPosts.forEach { newPost ->
            val index = currentPosts.indexOfFirst { it.postId == newPost.postId }
            if (index != -1) {
                currentPosts[index] = newPost
            } else {
                currentPosts.add(newPost)
            }
        }

        // Sort the posts by descending time
        val sortedPosts = currentPosts.sortedByDescending { it.time }

        // Update the post list in the app's state
        outState.value = sortedPosts
    }


    /**
     * Retrieves a specific post by its postId.
     *
     * This function performs the following steps:
     *
     * 1. Fetches all posts using a separate coroutine.
     * 2. Filters the list to find the post with the specified postId.
     *
     * @param postId The unique identifier of the post to retrieve.
     * @return The PostData object with the specified postId, or null if not found.
     */
    suspend fun getPostById(postId: String): PostData? {
        // Fetch all posts using a separate coroutine
        val allPosts = getAllPosts()

        // Filter the list to find the post with the specified postId
        return allPosts.firstOrNull { it.postId == postId }
    }

    /**
     * Retrieves a list of all posts from Firestore.
     *
     * This function performs the following steps:
     *
     * 1. Initiates a Firestore query to get all posts.
     * 2. Converts the query result into a list of PostData objects using the `convertPosts` function.
     *
     * @return A list of all PostData objects retrieved from Firestore.
     */
    private suspend fun getAllPosts(): List<PostData> = suspendCoroutine { continuation ->
        db.collection("posts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val postsList = querySnapshot.documents.mapNotNull { it.toObject<PostData>() }
                continuation.resume(postsList)
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Error getting all posts")
                continuation.resumeWithException(exc)
            }
    }




    /**
     * Searches for posts that match the provided search term.
     *
     * This function performs the following steps:
     *
     * 1. Checks if the provided search term is not empty.
     * 2. Sets `searchedPostsProgress` to true to indicate the search process has started.
     * 3. Queries the Firestore database to find posts containing the provided search term.
     * 4. Converts the query result into a list of PostData objects using the `convertPosts` function.
     * 5. Sets `searchedPostsProgress` to false to indicate the search process is complete.
     *
     * @param searchTerm The search term to match against post descriptions.
     */
    fun searchPosts(searchTerm: String) {
        // Check if the provided search term is not empty
        if (searchTerm.isNotEmpty()) {
            // Set `searchedPostsProgress` to true to indicate the search process has started
            searchedPostsProgress.value = true

            // Query the Firestore database to find posts containing the provided search term
            db.collection(POSTS)
                .whereArrayContains("searchTerms", searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Convert the query result into a list of PostData objects using the `convertPosts` function
                    convertPosts(querySnapshot, searchedPosts)

                    // Set `searchedPostsProgress` to false to indicate the search process is complete
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener { exc ->
                    // Handle failure by handling the exception, resetting `searchedPostsProgress`, and displaying an error message
                    handleException(exc, "Cannot search posts")
                    searchedPostsProgress.value = false
                }
        }
    }

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if (following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }
            db.collection(USERS).document(currentUser).update("following", following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    /**
     * Retrieves a personalized feed of posts based on the users that the current user is following.
     * If the user is not following anyone or no posts are found, it falls back to retrieving a general feed.
     */
    private fun getPersonalizedFeed() {
        // Set `postsFeedProgress` to true to indicate the feed retrieval process has started
        postsFeedProgress.value = true

        // Retrieve the list of users that the current user is following
        val following = userData.value?.following

        if (!following.isNullOrEmpty()) {
            // Query the Firestore database to find posts by users the current user is following
            db.collection(POSTS).whereIn("userId", following)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Convert the query result into a list of PostData objects using the `convertPosts` function
                    convertPosts(querySnapshot, postsFeed)

                    if (postsFeed.value.isEmpty()) {
                        // If no posts are found, fall back to retrieving a general feed
                        getGeneralFeed()
                    } else {
                        // Set `postsFeedProgress` to false to indicate the feed retrieval process is complete
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener { exc ->
                    // Handle failure by handling the exception, resetting `postsFeedProgress`, and displaying an error message
                    handleException(exc, "Cannot get personalized feed")
                    postsFeedProgress.value = false
                }
        } else {
            // If the user is not following anyone, fall back to retrieving a general feed
            getGeneralFeed()
        }
    }

    /**
     * Retrieves a general feed of posts based on the posts created within the last 24 hours.
     */
    private fun getGeneralFeed() {
        // Set `postsFeedProgress` to true to indicate the feed retrieval process has started
        postsFeedProgress.value = true

        // Calculate the current time in milliseconds
        val currentTime = System.currentTimeMillis()

        // Define the time difference to consider (1 day in milliseconds)
        val difference = 24 * 60 * 60 * 1000

        // Query the Firestore database to find posts created within the last 24 hours
        db.collection(POSTS).whereGreaterThan("time", currentTime - difference)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Convert the query result into a list of PostData objects using the `convertPosts` function
                convertPosts(querySnapshot, postsFeed)

                // Set `postsFeedProgress` to false to indicate the feed retrieval process is complete
                postsFeedProgress.value = false
            }
            .addOnFailureListener { exc ->
                // Handle failure by handling the exception, resetting `postsFeedProgress`, and displaying an error message
                handleException(exc, "Cannot get feed")
                postsFeedProgress.value = false
            }
    }

    fun onLikePost(postData: PostData) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)) {
                    newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { postId ->
                    db.collection(POSTS).document(postId).update("likes", newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, "Unable to like post")
                        }
                }
            }
        }
    }

    fun createComment(postId: String, text: String) {
        userData.value?.username?.let { username ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    getComments(postId)
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Cannot create comment")
                }

        }
    }

    fun getComments(postId: String?) {
        commentsProgress.value = true
        db.collection(COMMENTS).whereEqualTo("postId", postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentData>()
                documents.forEach { doc ->
                    val comment = doc.toObject<CommentData>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.timestamp }
                comments.value = sortedComments
                commentsProgress.value = false
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot retrieve comments")
                commentsProgress.value = false
            }
    }

}

