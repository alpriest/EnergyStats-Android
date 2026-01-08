package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.R

sealed class LoadState {
    data object Inactive : LoadState()

    data class Error(
        val ex: Exception?,
        val reason: String,
        val allowRetry: Boolean = true
    ) : LoadState()

    sealed class Active : LoadState() {
        abstract val titleResId: Int
        abstract val longOperationTitleResId: Int

        data object Loading : Active() {
            override val titleResId = R.string.loading
            override val longOperationTitleResId = R.string.still_loading
        }

        data object Saving : Active() {
            override val titleResId = R.string.saving
            override val longOperationTitleResId = R.string.still_saving
        }

        data object Activating : Active() {
            override val titleResId = R.string.activating
            override val longOperationTitleResId = R.string.still_activating
        }

        data object Deactivating : Active() {
            override val titleResId = R.string.deactivating
            override val longOperationTitleResId = R.string.still_deactivating
        }
    }
}

fun LoadState.isActive(): Boolean {
    return this is LoadState.Active
}
