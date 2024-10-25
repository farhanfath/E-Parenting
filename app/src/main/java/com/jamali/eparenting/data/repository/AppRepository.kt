package com.jamali.eparenting.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jamali.eparenting.Utility
import com.jamali.eparenting.data.entity.CommunityPost
import com.jamali.eparenting.data.entity.PostType
import com.jamali.eparenting.data.entity.User
import kotlinx.coroutines.tasks.await

class AppRepository {

    suspend fun registerUser(username: String,email: String, password: String): Result<Unit> {
        return try {
            Utility.auth.createUserWithEmailAndPassword(email, password).await()
            val userId = Utility.auth.currentUser?.uid ?: throw Exception("User ID is null")
            val userData = User(username, email)
            Utility.database.child("users").child(userId).setValue(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            Utility.auth.signInWithEmailAndPassword(email, password).await()
            val userId = Utility.auth.currentUser?.uid
            if (userId != null) {
                val userSnapshot = Utility.database.child("users").child(userId).get().await()
                val user = userSnapshot.getValue(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception("User ID is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(): Result<User> {
        return try {
            val userId = Utility.auth.currentUser?.uid
            if (userId != null) {
                val userSnapshot = Utility.database.child("users").child(userId).get().await()
                val user = userSnapshot.getValue(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception("User is not logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPost(
        title: String,
        description: String,
        selectedImageUri: Uri,
        type: PostType
    ): Result<Unit> {
        return try {
            // Upload image to Firebase Storage
            val storageRef = Utility.storage.reference.child("thumbnails/${System.currentTimeMillis()}.jpg")
            val uploadTask = storageRef.putFile(selectedImageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            // Once image is uploaded, save the post details to Firebase Realtime Database
            savePostToDatabase(title, description, downloadUrl.toString(), type)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to save the post data into Firebase Realtime Database
    private suspend fun savePostToDatabase(
        title: String,
        description: String,
        thumbnailUrl: String,
        type: PostType
    ): Result<Unit> {
        return try {
            val postId = Utility.database.child("communityposts").push().key
                ?: throw Exception("Failed to generate post ID")
            val post = CommunityPost(title, description, thumbnailUrl, type)

            // Save post data
            Utility.database.child("communityposts").child(postId).setValue(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllCommunityData(callback: (List<CommunityPost>) -> Unit) {
        // Mengakses node "communityposts" dari database
        Utility.database.child("communityposts").get().addOnSuccessListener { snapshot ->
            val communityList = ArrayList<CommunityPost>()

            // Loop melalui setiap child node dari "communityposts"
            for (communitySnapshot in snapshot.children) {
                val community = communitySnapshot.getValue(CommunityPost::class.java)
                community?.let { communityList.add(it) }
            }

            // Mengembalikan daftar data melalui callback
            callback(communityList)
        }.addOnFailureListener {
            Log.e("getAllCommunityData", "Failed to fetch data", it)
        }
    }

    fun getCommunityDataByType(tipe: PostType, callback: (List<CommunityPost>) -> Unit) {
        Utility.database.orderByChild("tipe").equalTo(tipe.name).get().addOnSuccessListener { snapshot ->
            val communityList = ArrayList<CommunityPost>()
            for (communitySnapshot in snapshot.children) {
                val community = communitySnapshot.getValue(CommunityPost::class.java)
                community?.let { communityList.add(it) }
            }
            callback(communityList)
        }
    }
}