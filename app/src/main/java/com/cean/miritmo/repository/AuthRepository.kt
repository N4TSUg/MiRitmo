package com.cean.miritmo.repository

import com.cean.miritmo.datastore.PreferencesManager
import com.cean.miritmo.firebase.AuthManager
import com.cean.miritmo.firebase.FirestoreManager
import com.cean.miritmo.model.User
import com.google.firebase.firestore.SetOptions
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

    val isDarkModeFlow = preferencesManager.isDarkModeFlow
    val isNotificationsEnabledFlow = preferencesManager.isNotificationsEnabledFlow
    val notificationSoundUriFlow = preferencesManager.notificationSoundUriFlow

    suspend fun setDarkMode(isDark: Boolean) {
        preferencesManager.setDarkMode(isDark)
    }

    suspend fun setNotificationsEnabled(isEnabled: Boolean) {
        preferencesManager.setNotificationsEnabled(isEnabled)
    }

    suspend fun setNotificationSoundUri(uri: String?) {
        preferencesManager.setNotificationSoundUri(uri)
    }

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

    suspend fun register(name: String, apodo: String?, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Error al registrarse")
            
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                apodo = apodo
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

    suspend fun updateProfile(newName: String,apodo: String?): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("No user logged in")
            val updates = mapOf(
                "name" to newName,
                "apodo" to apodo
            )
            db.collection(FirestoreManager.USERS_COLLECTION).document(userId)
                .update(updates).await()
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
                .set(mapOf("photoUrl" to avatarId), SetOptions.merge()).await()
                
            Result.success(avatarId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val currentUserId = getCurrentUserId()
            val queryLower = query.lowercase()
            
            // For a production app we'd use Algolia or Firebase extensions.
            // Here we do a simple client-side filter after fetching users.
            val snapshot = db.collection(FirestoreManager.USERS_COLLECTION).get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                .filter { it.id != currentUserId } // Don't show ourselves
                .filter { 
                    it.name.lowercase().contains(queryLower) || 
                    (it.apodo != null && it.apodo.lowercase().contains(queryLower)) 
                }
                
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followUser(targetUserId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: throw Exception("No user logged in")
            
            db.runTransaction { transaction ->
                val currentUserRef = db.collection(FirestoreManager.USERS_COLLECTION).document(currentUserId)
                val targetUserRef = db.collection(FirestoreManager.USERS_COLLECTION).document(targetUserId)

                val currentUserSnapshot = transaction.get(currentUserRef)
                val targetUserSnapshot = transaction.get(targetUserRef)

                val currentUser = currentUserSnapshot.toObject(User::class.java)
                val targetUser = targetUserSnapshot.toObject(User::class.java)

                if (currentUser != null && targetUser != null) {
                    val newFollowing = (currentUser.following + targetUserId).distinct()
                    val newFollowers = (targetUser.followers + currentUserId).distinct()

                    transaction.update(currentUserRef, "following", newFollowing)
                    transaction.update(targetUserRef, "followers", newFollowers)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: throw Exception("No user logged in")
            
            db.runTransaction { transaction ->
                val currentUserRef = db.collection(FirestoreManager.USERS_COLLECTION).document(currentUserId)
                val targetUserRef = db.collection(FirestoreManager.USERS_COLLECTION).document(targetUserId)

                val currentUserSnapshot = transaction.get(currentUserRef)
                val targetUserSnapshot = transaction.get(targetUserRef)

                val currentUser = currentUserSnapshot.toObject(User::class.java)
                val targetUser = targetUserSnapshot.toObject(User::class.java)

                if (currentUser != null && targetUser != null) {
                    val newFollowing = currentUser.following.filter { it != targetUserId }
                    val newFollowers = targetUser.followers.filter { it != currentUserId }

                    transaction.update(currentUserRef, "following", newFollowing)
                    transaction.update(targetUserRef, "followers", newFollowers)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> {
        return try {
            if (userIds.isEmpty()) return Result.success(emptyList())
            
            // Firestore 'in' query supports up to 10 items. For a robust app, we'd chunk this.
            // For this prototype, we'll chunk it just in case.
            val users = mutableListOf<User>()
            val chunks = userIds.chunked(10)
            
            for (chunk in chunks) {
                val snapshot = db.collection(FirestoreManager.USERS_COLLECTION)
                    .whereIn("id", chunk)
                    .get()
                    .await()
                users.addAll(snapshot.documents.mapNotNull { it.toObject(User::class.java) })
            }
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
