package com.jamali.eparenting.data.repository

import com.jamali.eparenting.Utility
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
}