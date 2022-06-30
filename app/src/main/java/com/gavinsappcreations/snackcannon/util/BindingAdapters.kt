package com.gavinsappcreations.snackcannon.util

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout


/**
 * Sets visibility of a View to VISIBLE if the argument is true, and GONE if the argument is false.
 */
@BindingAdapter("visibleIfTrue")
fun View.visibleIfTrue(isVisible: Boolean) {
    visibility = when (isVisible) {
        true -> View.VISIBLE
        false -> View.GONE
    }
}

/**
 * Sets the error text for a TextInputLayout to the String referenced at the provided resource.
 */
@BindingAdapter("errorTextResource")
fun TextInputLayout.errorTextResource(errorTextResource: Int?) {
    error = if (errorTextResource == null) {
        null
    } else {
        resources.getString(errorTextResource)
    }
}


/**
 * Sets a View to enabled if the String argument is not null and not empty.
 */
@BindingAdapter("enabledIfStringNotNullAndNotEmpty")
fun View.enabledIfStringNotNullAndNotEmpty(inputString: String?) {
    isEnabled = !inputString.isNullOrEmpty()
}


