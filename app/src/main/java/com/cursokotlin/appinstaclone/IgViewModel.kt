package com.cursokotlin.appinstaclone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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
    fun onSignUp(username: String, email: String, pass: String) {
        inProgress.value = true

        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if(documents.size() > 0){
                    handleExeption(customMessage = "Username already exists")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email,pass)
                        .addOnCompleteListener{ task ->
                            if (task.isSuccessful){
                                signedIn.value = true
                                //Create profile
                            } else {
                                handleExeption(customMessage = "Signup failed")
                            }
                            inProgress.value = false
                        }
                }

            }
            .addOnFailureListener{  }

    }

    private fun handleExeption(exception: Exception? = null, customMessage: String? = "" ){

    }

}