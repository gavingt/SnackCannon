package com.gavinsappcreations.snackcannon.data.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.paging.*
import com.gavinsappcreations.snackcannon.data.domain.SnackCategory
import com.gavinsappcreations.snackcannon.data.network.NetworkSnack
import com.gavinsappcreations.snackcannon.data.network.NetworkSnackContainer
import com.gavinsappcreations.snackcannon.data.network.SnackNetwork
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.ceil

class SnackRepository private constructor(appContext: Context) {

    private val db = Firebase.firestore


    suspend fun getSnackListByCategory(pageNumber: Int, category: SnackCategory): NetworkSnackContainer {
        val networkSnackContainer = SnackNetwork.snackData.getSnackListByCategory(
            pageNumber, category.apiName
        ).body()!!
        return networkSnackContainer
    }



    suspend fun searchSnackList(pageNumber: Int, searchTerm: String): NetworkSnackContainer {
        val networkSnackContainer = SnackNetwork.snackData.searchSnackList(
            pageNumber, searchTerm
        ).body()!!
        return networkSnackContainer
    }


    fun getSnackList(snackCategory: SnackCategory, searchTerm: String? = null): Flow<PagingData<NetworkSnack>> = Pager(
        config = PagingConfig(
            /**
             * A good page size is a value that fills at least a few screens worth of content on a
             * large device so the User is unlikely to see a null item.
             * You can play with this constant to observe the paging behavior.
             *
             * It's possible to vary this with list device size, but often unnecessary, unless a
             * user scrolling on a large device is expected to scroll through items more quickly
             * than a small device, such as when the large device uses a grid layout of items.
             */
            pageSize = 40,

            /**
             * If placeholders are enabled, PagedList will report the full size but some items might
             * be null in onBind method (PagedListAdapter triggers a rebind when data is loaded).
             *
             * If placeholders are disabled, onBind will never receive null but as more pages are
             * loaded, the scrollbars will jitter as new pages are loaded. You should probably
             * disable scrollbars if you disable placeholders.
             */

            enablePlaceholders = true,

            /**
             * Maximum number of items a PagedList should hold in memory at once.
             *
             * This number triggers the PagedList to start dropping distant pages as more are loaded.
             */
            maxSize = 300,

            /**
             * Prefetch distance which defines how far from the edge of loaded content an access
             * must be to trigger further loading. Typically should be set several times the number
             * of visible items onscreen.
             * E.g., If this value is set to 50, a PagingData will attempt to load 50 items in
             * advance of data that's already been accessed.
             * A value of 0 indicates that no list items will be loaded until they are specifically
             * requested. This is generally not recommended, so that users don't observe a placeholder
             * item (with placeholders) or end of list (without) while scrolling.
             */
            prefetchDistance = 80
        )
    ) {
        object : PagingSource<Int, NetworkSnack>() {

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NetworkSnack> {

                // Retrofit calls that return the body type throw either IOException for network
                // failures, or HttpException for any non-2xx HTTP status codes. This code reports all
                // errors to the UI, but you can inspect/wrap the exceptions to provide more context.
                return try {
                    // Key may be null during a refresh, if no explicit key is passed into Pager
                    // construction. Use 1 as default, because our API is indexed started at index 1
                    val pageNumber = params.key ?: 1

                    // Suspending network load via Retrofit. This doesn't need to be wrapped in a
                    // withContext(Dispatcher.IO) { ... } block since Retrofit's Coroutine
                    // CallAdapter dispatches on a worker thread.

                    // If category is NONE, that signifies user is doing a custom search.
                    val networkSnackContainer = when (snackCategory) {
                        SnackCategory.NONE -> searchSnackList(pageNumber, searchTerm!!)
                        else -> getSnackListByCategory(pageNumber, snackCategory)
                    }

                    // Since 1 is the lowest page number, return null to signify no more pages should
                    // be loaded before it.
                    val prevKey = if (pageNumber > 1) pageNumber - 1 else null

                    // Here we calculate totalNumberOfPages, so we know when to stop loading data.
                    val itemsPerPage = 40
                    val totalNumberOfPages: Double = ceil(networkSnackContainer.count.toDouble()/itemsPerPage)

                    // Increment the page number by 1, but if we're on the last page set it to null instead.
                    val nextKey = if (networkSnackContainer.page < totalNumberOfPages) pageNumber + 1 else null

                    LoadResult.Page(
                        data = networkSnackContainer.products,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                } catch (e: IOException) {
                    // Network timeouts are caught here
                    LoadResult.Error(e)
                } catch (e: HttpException) {
                    LoadResult.Error(e)
                }
            }


            override fun getRefreshKey(state: PagingState<Int, NetworkSnack>): Int? {
                return state.anchorPosition?.let {
                    state.closestPageToPosition(it)?.prevKey?.plus(1)
                        ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
                }
            }
        }
    }.flow
    // TODO: translate NetworkSnacks into Snacks using a map {}
/*        .map { pagingData ->
            pagingData
                // Map cheeses to common UI model.
                .map { cheese -> CheeseListItem.Item(cheese) }
                .insertSeparators { before: CheeseListItem?, after: CheeseListItem? ->
                    if (before == null && after == null) {
                        // List is empty after fully loaded; return null to skip adding separator.
                        null
                    } else if (after == null) {
                        // Footer; return null here to skip adding a footer.
                        null
                    } else if (before == null) {
                        // Header
                        CheeseListItem.Separator(after.name.first())
                    } else if (!before.name.first().equals(after.name.first(), ignoreCase = true)){
                        // Between two items that start with different letters.
                        CheeseListItem.Separator(after.name.first())
                    } else {
                        // Between two items that start with the same letter.
                        null
                    }
                }
        }

*/


    companion object {
        // For singleton instantiation of this repository.
        @Volatile
        private var instance: SnackRepository? = null

        fun getInstance(application: Application) =
            instance ?: synchronized(this) {
                instance ?: SnackRepository(application).also { instance = it }
            }
    }

}
