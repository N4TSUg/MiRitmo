package com.cean.miritmo.firebase

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    companion object {
        const val USERS_COLLECTION = "users"
        const val HABITS_COLLECTION = "habits"
        const val RECORDS_COLLECTION = "records"
    }
}
