package com.gavinsappcreations.snackcannon

import android.app.Application
import com.gavinsappcreations.snackcannon.data.datastore.dataStore
import com.gavinsappcreations.snackcannon.data.repository.ProfileRepository
import com.gavinsappcreations.snackcannon.data.repository.SnackRepository

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize repository objects here, so they're ready immediately for Fragments to use.
        _profileRepository = ProfileRepository.getInstance(dataStore, this)
        _snackRepository = SnackRepository.getInstance(this)
    }

    companion object {
        private lateinit var _profileRepository: ProfileRepository
        val profileRepository: ProfileRepository
            get() = _profileRepository

        private lateinit var _snackRepository: SnackRepository
        val snackRepository: SnackRepository
            get() = _snackRepository
    }
}