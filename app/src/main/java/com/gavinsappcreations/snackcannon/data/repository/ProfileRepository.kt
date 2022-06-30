package com.gavinsappcreations.snackcannon.data.repository

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.gavinsappcreations.snackcannon.data.datastore.DataStoreKeys
import com.gavinsappcreations.snackcannon.data.domain.Profile
import com.gavinsappcreations.snackcannon.data.domain.State
import com.gavinsappcreations.snackcannon.util.FirebaseUserFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.io.IOException

class ProfileRepository private constructor(private val dataStore: DataStore<Preferences>, appContext: Context) {

    private val db = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Flow that emits when a user's login status changes.
    val userLoginStatusChangedFlow: Flow<Boolean?> = FirebaseUserFlow().getLoginStatusChanges()

    /**
     * Get profileFlow from the flow emitted by dataStore.
     */
    val profileFromDataStoreFlow: Flow<Profile> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered while reading data.
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val deliveryAddress: String? = preferences[DataStoreKeys.DELIVERY_ADDRESS]
            val addressLatitude: Double? = preferences[DataStoreKeys.ADDRESS_LATITUDE]
            val addressLongitude: Double? = preferences[DataStoreKeys.ADDRESS_LONGITUDE]
            val unitNumber: String? = preferences[DataStoreKeys.UNIT_NUMBER]
            val deliveryNotes: String? = preferences[DataStoreKeys.DELIVERY_NOTES]
            val fullName: String? = preferences[DataStoreKeys.FULL_NAME]
            val emailAddress: String? = preferences[DataStoreKeys.EMAIL_ADDRESS]

            return@map Profile(
                deliveryAddress, addressLatitude, addressLongitude, unitNumber,
                deliveryNotes, fullName, emailAddress
            )
        }


    // Writes all the provided Preferences to DataStore.
    private suspend fun writePreferencesToDataStore(preferences: Preferences) {
        dataStore.edit {
            it.plusAssign(preferences)
        }
    }


    // Reads profile from Firestore (encapsulated in a State object) and returns it.
    suspend fun readProfileFromFirestore(): State<Profile?> {
        return try {
            val profileReference = db.collection("users").document(getCurrentUserUID())
            val snapshot: DocumentSnapshot = profileReference.get().await()
            val profile = snapshot.toObject(Profile::class.java)
            profile?.let {
                writePreferencesToDataStore(createPreferencesFromProfile(profile))
            }
            State.Success(profile)
        } catch (throwable: Throwable) {
            State.Failed(throwable)
        }
    }


    private fun createPreferencesFromProfile(profile: Profile): Preferences {
        val prefs = mutablePreferencesOf()
        prefs[DataStoreKeys.DELIVERY_ADDRESS] = profile.deliveryAddress!!
        prefs[DataStoreKeys.ADDRESS_LATITUDE] = profile.addressLatitude!!
        prefs[DataStoreKeys.ADDRESS_LONGITUDE] = profile.addressLongitude!!
        prefs[DataStoreKeys.UNIT_NUMBER] = profile.unitNumber ?: ""
        prefs[DataStoreKeys.DELIVERY_NOTES] = profile.deliveryNotes ?: ""
        return prefs
    }


    /**
     * Writes profile data to Firestore and returns either success or failure in the form of a State object.
     * We use a HashMap because this allows us to only update the properties included in the HashMap
     * rather than updating all properties in the Profile class.
     */
    suspend fun writeProfileToFirestore(preferences: Preferences): State<Unit> {
        return try {
            val profileReference = db.collection("users").document(getCurrentUserUID())
            val preferencesHashMap = createPreferencesHashMap(preferences.asMap())
            profileReference.set(preferencesHashMap, SetOptions.merge()).await()
            writePreferencesToDataStore(preferences)
            State.Success(Unit)
        } catch (throwable: Throwable) {
            State.Failed(throwable)
        }
    }


    // Creates a HashMap from our preferences data, allowing us to save it to Firestore.
    private fun createPreferencesHashMap(preferencesMap: Map<Preferences.Key<*>, Any>): HashMap<String, Any> {
        val preferencesHashMap = HashMap<String, Any>()
        for (item in preferencesMap) {
            preferencesHashMap[item.key.name] = item.value
        }
        return preferencesHashMap
    }


    fun isProfileComplete(profile: Profile): Boolean {
        return !profile.deliveryAddress.isNullOrEmpty()
                && profile.addressLatitude != null
                && profile.addressLongitude != null
                && !profile.emailAddress.isNullOrEmpty()
                && !profile.fullName.isNullOrEmpty()
    }


    // Gets current user's Firebase UID, or throws an Exception if user isn't logged in.
    private fun getCurrentUserUID(): String {
        val currentUser = firebaseAuth.currentUser ?: throw Exception("Not logged into Firebase")
        return currentUser.uid
    }


    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): State<Unit> {
        return try {
            firebaseAuth.signInWithCredential(credential).await()
            State.Success(Unit)

        } catch (throwable: Throwable) {
            State.Failed(throwable)
        }
    }


    companion object {
        // For singleton instantiation of this repository.
        @Volatile
        private var instance: ProfileRepository? = null

        fun getInstance(dataStore: DataStore<Preferences>, application: Application) =
            instance ?: synchronized(this) {
                instance ?: ProfileRepository(dataStore, application).also { instance = it }
            }
    }

}
