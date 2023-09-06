package com.cursokotlin.appinstaclone.data

import android.os.Parcel
import android.os.Parcelable

data class PostData (
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val userImage: String? = null,
    val postImage: String? = null,
    val postDescription: String? = null,
    val time: Long? = null,
    val likes: List<String>? = null
)