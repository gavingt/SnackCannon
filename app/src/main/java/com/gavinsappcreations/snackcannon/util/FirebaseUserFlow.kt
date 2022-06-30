package com.gavinsappcreations.snackcannon.util

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Creates a flow that emits true when Firebase detects user is logged in and false if not logged in.
open class FirebaseUserFlow {

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getLoginStatusChanges(): Flow<Boolean> =
        callbackFlow {
            val authStateListener = FirebaseAuth.AuthStateListener {
                trySend(it.currentUser != null)
            }
            firebaseAuth.addAuthStateListener(authStateListener)
            awaitClose {
                firebaseAuth.removeAuthStateListener(authStateListener)
            }
        }
}