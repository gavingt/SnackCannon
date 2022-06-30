package com.gavinsappcreations.snackcannon.data.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// Stores profile data for a single user. Null default values create a no-argument
// default constructor, which is needed for deserialization from a DataSnapshot.
@Parcelize
data class Profile(
    var deliveryAddress: String? = null,
    var addressLatitude: Double? = null,
    var addressLongitude: Double? = null,
    var unitNumber: String? = null,
    var deliveryNotes: String? = null,
    var fullName: String? = null,
    var emailAddress: String? = null
) : Parcelable


// Used for encapsulating loading state when doing asynchronous calls.
sealed class State<T> {
    class Loading<T> : State<T>()
    data class Success<T>(val data: T) : State<T>()
    data class Failed<T>(val throwable: Throwable) : State<T>()
}


// All the possible snack categories in ExploreFragment.
// apiName is the name the OpenFoodFacts API uses to refer to these various categories.
enum class SnackCategory(val apiName: String) {
    BAKED ("biscuits-and-cakes"),
    SALTY ("salty-snacks"),
    NUTS("nuts"),
    CANDY("confectioneries"),
    FROZEN("frozen-desserts"),
    DRINKS("sweetened-beverages"),
    NONE("not-used")
}