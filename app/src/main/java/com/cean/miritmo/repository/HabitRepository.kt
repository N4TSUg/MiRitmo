package com.cean.miritmo.repository

import com.cean.miritmo.firebase.FirestoreManager
import com.cean.miritmo.model.Habit
import com.cean.miritmo.model.HabitRecord
import com.cean.miritmo.model.Routine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitRepository(private val firestoreManager: FirestoreManager) {

    private val db = firestoreManager.db

    suspend fun createHabit(habit: Habit): Result<String> {
        return try {
            val docRef = db.collection(FirestoreManager.HABITS_COLLECTION).document()
            val newHabit = habit.copy(id = docRef.id)
            docRef.set(newHabit).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHabit(habit: Habit): Result<Unit> {
        return try {
            db.collection(FirestoreManager.HABITS_COLLECTION).document(habit.id).set(habit).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            db.collection(FirestoreManager.HABITS_COLLECTION).document(habitId).delete().await()
            // Also optionally delete associated records, simplified here
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabitsForUser(userId: String): Result<List<Habit>> {
        return try {
            val snapshot = db.collection(FirestoreManager.HABITS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val habits = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Habit::class.java)
                } catch (e: Exception) {
                    android.util.Log.e("HabitRepository", "Error parsing habit ${doc.id}", e)
                    null
                }
            }
            Result.success(habits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabitById(habitId: String): Result<Habit?> {
        return try {
            val doc = db.collection(FirestoreManager.HABITS_COLLECTION).document(habitId).get().await()
            val habit = doc.toObject(Habit::class.java)
            Result.success(habit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Records management
    suspend fun markHabitCompleted(habitId: String, date: String): Result<Unit> {
        return try {
            val docRef = db.collection(FirestoreManager.RECORDS_COLLECTION).document()
            val record = HabitRecord(
                id = docRef.id,
                habitId = habitId,
                date = date,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            docRef.set(record).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unmarkHabitCompleted(habitId: String, date: String): Result<Unit> {
        return try {
            val snapshot = db.collection(FirestoreManager.RECORDS_COLLECTION)
                .whereEqualTo("habitId", habitId)
                .whereEqualTo("date", date)
                .get()
                .await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecordsForUser(userId: String): Result<List<HabitRecord>> {
        // En un caso real, la busqueda de records requiere el ID del usuario en el record 
        // o buscar en los habitos. Por simplicidad, asumimos que obtenemos los habitos y luego sus records
        return try {
            val habitsResult = getHabitsForUser(userId)
            if (habitsResult.isFailure) throw habitsResult.exceptionOrNull()!!
            val habitIds = habitsResult.getOrNull()?.map { it.id } ?: emptyList()

            if (habitIds.isEmpty()) return Result.success(emptyList())

            // Firestore 'in' query supports up to 10 items. For larger lists, batching is required.
            // Using a simplified approach here by fetching all records and filtering client side for MVP.
            // In production, records should store userId to avoid this.
            val snapshot = db.collection(FirestoreManager.RECORDS_COLLECTION).get().await()
            val records = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(HabitRecord::class.java)
                } catch (e: Exception) {
                    null
                }
            }.filter { it.habitId in habitIds }
            
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    suspend fun createRoutine(routine: Routine): Result<String> {
        return try {
            val docRef = db.collection(FirestoreManager.ROUTINES_COLLECTION).document()
            val newRoutine = routine.copy(id = docRef.id)
            docRef.set(newRoutine).await()
            Result.success(newRoutine.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRoutine(routine: Routine): Result<Unit> {
        return try {
            db.collection(FirestoreManager.ROUTINES_COLLECTION).document(routine.id).set(routine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> {
        return try {
            db.collection(FirestoreManager.ROUTINES_COLLECTION).document(routineId).update("isDeleted", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun permanentlyDeleteRoutine(routineId: String): Result<Unit> {
        return try {
            db.collection(FirestoreManager.ROUTINES_COLLECTION).document(routineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoutinesForUser(userId: String): Result<List<Routine>> {
        return try {
            val snapshot = db.collection(FirestoreManager.ROUTINES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val routines = snapshot.documents.mapNotNull { it.toObject(Routine::class.java) }
            Result.success(routines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHabitsToRoutine(routineId: String, habitIds: List<String>): Result<Unit> {
        return try {
            // Firestore transactions support limited operations. We can do it in a batch for efficiency
            val batch = db.batch()
            
            // 1. Update the routine's habitIds
            val routineRef = db.collection(FirestoreManager.ROUTINES_COLLECTION).document(routineId)
            
            // Note: In a real app we might want to get the old habitIds and append. For now we assume we fetch it before calling this.
            // But we don't have atomic arrayUnion unless we use FieldValue. Let's keep it simple: the caller sends the updated full list or we just update the habits.
            // Wait, the easiest way is to let `updateRoutine` handle the `Routine` object updates, and here we just update the habits to point to the routine.
            
            habitIds.forEach { hId ->
                val habitRef = db.collection(FirestoreManager.HABITS_COLLECTION).document(hId)
                batch.update(habitRef, "routineId", routineId)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeHabitsFromRoutine(habitIds: List<String>): Result<Unit> {
        return try {
            val batch = db.batch()
            habitIds.forEach { hId ->
                val habitRef = db.collection(FirestoreManager.HABITS_COLLECTION).document(hId)
                batch.update(habitRef, "routineId", null)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
