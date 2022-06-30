package com.gavinsappcreations.snackcannon.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// This identifies the datastore instance that we use.
const val DATASTORE_NAME = "snack_cannon_datastore"

// Extension property that initializes our datastore.
val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object DataStoreKeys {
    val DELIVERY_ADDRESS = stringPreferencesKey("deliveryAddress")
    val ADDRESS_LATITUDE = doublePreferencesKey("addressLatitude")
    val ADDRESS_LONGITUDE = doublePreferencesKey("addressLongitude")
    val UNIT_NUMBER = stringPreferencesKey("unitNumber")
    val DELIVERY_NOTES = stringPreferencesKey("deliveryNotes")
    val FULL_NAME = stringPreferencesKey("fullName")
    val EMAIL_ADDRESS = stringPreferencesKey("emailAddress")
}

