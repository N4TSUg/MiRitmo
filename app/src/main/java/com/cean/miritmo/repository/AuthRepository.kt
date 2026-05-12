package com.cean.miritmo.repository

import com.cean.miritmo.datastore.PreferencesManager
import com.cean.miritmo.firebase.AuthManager
import com.cean.miritmo.firebase.FirestoreManager
import com.cean.miritmo.model.User
import kotlinx.coroutines.tasks.await
import android.net.Uri
import java.util.UUID

class AuthRepository(
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager,
    private val preferencesManager: PreferencesManager
) {
    private val auth = authManager.auth
    private val db = firestoreManager.db

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Error al iniciar sesión")
            
            // Get user data from Firestore
            val doc = db.collection(FirestoreManager.USERS_COLLECTION).document(firebaseUser.uid).get().await()
            val user = doc.toObject(User::class.java) ?: User(id = firebaseUser.uid, email = email)
            
            // Save session
            preferencesManager.setUserId(firebaseUser.uid)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Error al registrarse")
            
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email
            )
            
            // Save to Firestore
            db.collection(FirestoreManager.USERS_COLLECTION).document(user.id).set(user).await()
            
            // Save session
            preferencesManager.setUserId(firebaseUser.uid)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        authManager.signOut()
        preferencesManager.setUserId(null)
    }
    
    fun getCurrentUserId(): String? {
        return authManager.currentUser?.uid
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("No user logged in")
            val doc = db.collection(FirestoreManager.USERS_COLLECTION).document(userId).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("User not found in database")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateName(newName: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("No user logged in")
            db.collection(FirestoreManager.USERS_COLLECTION).document(userId)
                .update("name", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val firebaseUser = authManager.currentUser ?: throw Exception("No user logged in")
            val email = firebaseUser.email ?: throw Exception("No email found")
            
            // Re-authenticate
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
            firebaseUser.reauthenticate(credential).await()
            
            // Update password
            firebaseUser.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfilePicture(avatarId: String): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("No user logged in")
            db.collection(FirestoreManager.USERS_COLLECTION).document(userId)
                .update("photoUrl", avatarId).await()
                
            Result.success(avatarId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
