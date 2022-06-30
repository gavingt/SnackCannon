package com.gavinsappcreations.snackcannon.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// this is where we model the JSON data

// Represents the root-level data returned from the API.
@JsonClass(generateAdapter = true)
data class NetworkSnackContainer(
    val count: Int,
    val page: Int,
    val page_count: Int,
    val products: List<NetworkSnack>
)

// Represents a single snack's data returned from the API.
@JsonClass(generateAdapter = true)
data class NetworkSnack(
    val brands: String?,
    val categories: String?,
    val code: Long,
    val generic_name: String?,
    val image_small_url: String?,
    //val image_url: String?,
    val product_name: String?,
    val quantity: String?
)

// TODO: create just a Snack class that transforms the data by formatting the text to fit my needs and adding a price.
//       Then create a method to convert from NetworkSnack to Snack.