package com.gavinsappcreations.snackcannon.data.network

import com.gavinsappcreations.snackcannon.data.domain.SnackCategory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * A retrofit service to fetch snack data.
 */
interface SnackService {

    // Fetches a list of snacks based on the search term provided.
    @GET("search.pl?&action=process&json=true&search_simple=true&sort_by=unique_scans_n&page_size=40&fields=code,product_name,generic_name,quantity,brands,categories,image_small_url&tagtype_0=categories_lc&tag_contains_0=contains&tag_0=en&tagtype_1=categories&tag_contains_1=contains&tag_1=snacks")
    suspend fun searchSnackList(
        @Query("page") pageNumber: Int,
        @Query("search_terms") searchTerm: String
    ): Response<NetworkSnackContainer>


    // Fetches a list of snacks based on the category provided.
    @GET("search.pl?action=process&json=true&search_simple=true&sort_by=unique_scans_n&page_size=40&fields=code,product_name,generic_name,quantity,brands,categories,image_small_url&tagtype_0=categories_lc&tag_contains_0=contains&tag_0=en&tagtype_1=categories&tag_contains_1=contains")
    suspend fun getSnackListByCategory(
        @Query("page") pageNumber: Int,
        @Query("tag_1") categoryName: String
    ): Response<NetworkSnackContainer>

}



/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Main entry point for network access. Call like `SnackNetwork.snackData.getSnackData()`
 */
object SnackNetwork {

    // Add user-agent header, as requested by OpenFoodFacts API.
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("UA", "User-Agent:SnackCannon-Android-gavingt.github.io")
                    .build()
            )
        }
        .build()


    // Configure Retrofit to parse JSON using Moshi.
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://us.openfoodfacts.org/cgi/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val snackData: SnackService = retrofit.create(SnackService::class.java)
}
