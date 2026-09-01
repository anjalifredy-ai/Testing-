package com.rikky.blankct.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

object AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun ensureSignedIn(onReady: (String) -> Unit) {
        val current = auth.currentUser
        if (current != null) {
            fetchOrCreateShortId(current.uid, onReady)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                fetchOrCreateShortId(uid, onReady)
            }
    }

    private fun fetchOrCreateShortId(uid: String, onReady: (String) -> Unit) {
        val userRef = db.child("users").child(uid)
        userRef.child("shortId").get().addOnSuccessListener { snapshot ->
            val existing = snapshot.getValue(String::class.java)
            if (existing != null) {
                onReady(existing)
            } else {
                val newCode = generateShortCode()
                userRef.child("shortId").setValue(newCode)
                userRef.child("uid").setValue(uid)
                onReady(newCode)
            }
        }
    }

    private fun generateShortCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    fun currentUid(): String? = auth.currentUser?.uid
}
