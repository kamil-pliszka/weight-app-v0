package com.pl.myweightapp.feature.settings

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.R
import com.pl.myweightapp.core.Constants.PROFILE_PHOTO_FILENAME
import com.pl.myweightapp.core.util.kgToLbs
import com.pl.myweightapp.core.util.lbsToKg
import com.pl.myweightapp.domain.Gender
import com.pl.myweightapp.domain.HeightUnit
import com.pl.myweightapp.domain.StorageSupport
import com.pl.myweightapp.domain.UserProfile
import com.pl.myweightapp.domain.UserProfileRepository
import com.pl.myweightapp.domain.WeightUnit
import com.pl.myweightapp.feature.common.DefaultUiEventOwner
import com.pl.myweightapp.feature.common.UiEventOwner
import com.pl.myweightapp.feature.common.launchWithErrorHandling
import com.pl.myweightapp.feature.common.sendInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.math.BigDecimal
import javax.inject.Inject


@Immutable
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,

    val name: String = "",
    val age: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val photoPath: String? = null,
    val tmpPhotoPath: String? = null,   // robocze
    //val photoBitmap: ImageBitmap? = null,
    //val photoImagePath: String? = null,

    val height: String = "",
    val targetWeight: String = "",
    val heightUnit: HeightUnit = HeightUnit.CM,
    val weightUnit: WeightUnit = WeightUnit.KG,
//    val period : DisplayPeriod? = null,
//    val movingAverage1: Int? = null,
//    val movingAverage2: Int? = null,
//    val lang: String? = null,
)

sealed interface ProfileAction {
    data class NameChanged(val value: String) : ProfileAction
    data class AgeChanged(val value: String) : ProfileAction
    data class HeightChanged(val value: String) : ProfileAction
    data class WeightChanged(val value: String) : ProfileAction

    data class GenderChanged(val gender: Gender) : ProfileAction
    data class HeightUnitChanged(val unit: HeightUnit) : ProfileAction
    data class WeightUnitChanged(val unit: WeightUnit) : ProfileAction

    //data class PhotoChanged(val path: String) : ProfileAction
    object SaveClicked : ProfileAction

    data class PhotoPicked(val input: InputStream) : ProfileAction
    data class PhotoCaptured(val capturedFilePath: String) : ProfileAction
    object ToggleWeightUnit : ProfileAction
    object ToggleHeightUnit : ProfileAction
    object ToggleGender : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val storageSupport: StorageSupport,
) : ViewModel(), UiEventOwner by DefaultUiEventOwner() {
    companion object {
        private val TAG = object {}.javaClass.enclosingClass?.simpleName
    }

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        observeProfile()
        viewModelScope.launch {
            storageSupport.logStorage()
        }
    }

    private suspend inline fun <T> withSaving(
        crossinline block: suspend () -> T
    ): T {
        return try {
            _state.update { it.copy(isSaving = true) }
            block()
        } finally {
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun observeProfile() {
        repository.observeProfile()
            .onEach { profile ->
                if (profile == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@onEach
                }
                val validPath = profile.photoPath?.let {
                    if (storageSupport.exists(it)) {
                        it
                    } else {
                        Log.d(TAG, "Photo file not found: ${profile.photoPath}")
                        null
                    }
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        name = profile.name.orEmpty(),
                        age = profile.age?.toString() ?: "",
                        height = profile.height?.toString() ?: "",
                        targetWeight = profile.targetWeight?.toPlainString() ?: "",

                        heightUnit = profile.heightUnit ?: HeightUnit.CM,
                        weightUnit = profile.weightUnit ?: WeightUnit.KG,
                        //period = profile.displayPeriod,
                        //movingAverage1 = profile.movingAverage1,
                        //movingAverage2 = profile.movingAverage2,
                        //lang = profile.lang,

                        gender = profile.gender ?: Gender.UNSPECIFIED,
                        photoPath = validPath, //profile.photoPath,
                        tmpPhotoPath = null,
                        isDirty = false
                    )
                }
            }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ProfileAction) {
        Log.d(TAG, "Action -> $action")
        when (action) {
            is ProfileAction.NameChanged -> {
                update { copy(name = action.value) }
            }

            is ProfileAction.AgeChanged -> {
                update { copy(age = action.value) }
            }

            is ProfileAction.HeightChanged -> {
                update { copy(height = action.value) }
            }

            is ProfileAction.WeightChanged -> {
                update { copy(targetWeight = action.value) }
            }

            is ProfileAction.GenderChanged -> {
                update { copy(gender = action.gender) }
            }

            is ProfileAction.HeightUnitChanged -> {
                update { copy(heightUnit = action.unit) }
            }

            is ProfileAction.WeightUnitChanged -> {
                onWeightUnitChange(action.unit)
            }
            //is ProfileAction.PhotoChanged -> update { copy(photoPath = action.path) }
            ProfileAction.SaveClicked -> {
                save()
            }

            is ProfileAction.PhotoPicked -> {
                launchWithErrorHandling {
                    val savedPath = storageSupport.saveProfileImage(action.input)
                    //_state.update { it.copy(photoPath = savedPath) }
                    //deleteOldPhotoIfExists(state.value.photoPath)
                    update { copy(tmpPhotoPath = savedPath) }
                }
            }

            is ProfileAction.PhotoCaptured -> {
                // uri już zapisane w pliku, wystarczy przypisać path do state
                //_state.update { it.copy(photoPath = action.capturedFilePath) }
                //deleteOldPhotoIfExists(state.value.photoPath)
                update { copy(tmpPhotoPath = action.capturedFilePath) }
            }

            ProfileAction.ToggleWeightUnit -> {
                val newWeightUnit =
                    if (state.value.weightUnit == WeightUnit.KG) WeightUnit.LB else WeightUnit.KG
                onWeightUnitChange(newWeightUnit)
            }

            ProfileAction.ToggleHeightUnit -> {
                //TODO - zrobić przełączanie jednostek
                update { copy(heightUnit = if (heightUnit == HeightUnit.CM) HeightUnit.IN else HeightUnit.CM) }
            }

            ProfileAction.ToggleGender -> {
                update {
                    copy(
                        gender =
                            when (gender) {
                                Gender.MALE -> Gender.FEMALE
                                Gender.FEMALE -> Gender.UNSPECIFIED
                                Gender.UNSPECIFIED -> Gender.MALE
                            }
                    )
                }
            }
        }
    }

    private inline fun update(
        crossinline block: ProfileUiState.() -> ProfileUiState
    ) {
        _state.update { it.block().copy(isDirty = true) }
    }

    private fun convertWeight(currentWeight: String, newUnit: WeightUnit): String? {
        val weight = currentWeight.toBigDecimalOrNull() ?: return null
        val newValue = when (newUnit) {
            WeightUnit.KG -> weight.toFloat().lbsToKg()
            WeightUnit.LB -> weight.toFloat().kgToLbs()
        }
        return "%.1f".format(newValue).replace(",", ".").replace(".0", "")
    }

    private fun onWeightUnitChange(newWeightUnit: WeightUnit) {
        if (state.value.weightUnit == newWeightUnit) return
        update {
            copy(
                weightUnit = newWeightUnit,
                targetWeight = convertWeight(targetWeight, newWeightUnit) ?: targetWeight
            )
        }
    }

    private fun save() = launchWithErrorHandling {
        withSaving {
            val s = state.value
            val age = s.age.toIntOrNull()
            val height = s.height.toIntOrNull()
            val weight = s.targetWeight.toBigDecimalOrNull()
            if (age != null && age !in 5..120) {
                sendInfo(R.string.profile_incorrect_age)
                return@withSaving
            }
            if (height != null && height !in 50..500) {
                sendInfo(R.string.profile_incorrect_height)
                return@withSaving
            }
            if (weight != null && weight <= BigDecimal.ZERO) {
                sendInfo(R.string.profile_incorrect_weight)
                return@withSaving
            }

            val finalPhoto = if (s.tmpPhotoPath != null) {
                storageSupport.copyTmpToFinal(
                    fromPath = s.tmpPhotoPath,
                    toFilename = PROFILE_PHOTO_FILENAME
                )
            } else {
                s.photoPath
            }
            //_state.update { it.copy(photoPath = finalPhoto, tmpPhotoPath = null) }
            Log.d(TAG, "Final photo: $finalPhoto")
            val userProfile = UserProfile(
                name = s.name,
                age = age,
                height = height,
                heightUnit = s.heightUnit,
                targetWeight = weight,
                weightUnit = s.weightUnit,
                gender = s.gender,
                photoPath = finalPhoto,
                //displayPeriod = s.period,
                //movingAverage1 = s.movingAverage1,
                //movingAverage2 = s.movingAverage2,
                //lang = s.lang
            )

            repository.save(userProfile)
            _state.update {
                it.copy(isDirty = false)
            }
            sendInfo(R.string.profile_saved)
        }
    }


}