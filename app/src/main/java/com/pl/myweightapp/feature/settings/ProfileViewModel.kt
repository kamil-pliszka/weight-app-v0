package com.pl.myweightapp.feature.settings

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pl.myweightapp.app.di.AppModule
import com.pl.myweightapp.R
import com.pl.myweightapp.core.Constants.PROFILE_PHOTO_FILENAME
import com.pl.myweightapp.data.local.Gender
import com.pl.myweightapp.data.local.HeightUnit
import com.pl.myweightapp.data.local.UserProfileEntity
import com.pl.myweightapp.core.domain.WeightUnit
import com.pl.myweightapp.core.util.kgToLbs
import com.pl.myweightapp.core.util.lbsToKg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigDecimal
import kotlin.coroutines.cancellation.CancellationException


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
    val photoBitmap: ImageBitmap? = null,

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

    data class PhotoPicked(val uri: Uri) : ProfileAction
    data class PhotoCaptured(val capturedFilePath: String) : ProfileAction
    object ToggleWeightUnit : ProfileAction
    object ToggleHeightUnit : ProfileAction
    object ToggleGender : ProfileAction
}

sealed interface ProfileEvent {
    data class Error(val message: String) : ProfileEvent
    data class Saved(val message: String) : ProfileEvent
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "ProfileVM"
    }

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()
    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeProfile()
        val context = getApplication<Application>()
        (context.filesDir.listFiles() ?: emptyArray()).forEach { file ->
            Log.d(
                "FILES",
                "File[filesDir]: ${file.name}, size: ${file.length()}, exists: ${file.exists()}"
            )
        }
        (context.cacheDir.listFiles() ?: emptyArray()).forEach { file ->
            Log.d(
                "FILES",
                "File[cacheDir]: ${file.name}, size: ${file.length()}, exists: ${file.exists()}"
            )
        }
    }

    private fun observeProfile() {
        AppModule.provideUserProfileRepository().profile
            .onEach { profile ->
                _state.update { it.copy(isLoading = false) }
                if (profile == null) return@onEach
                val validPath = profile.photoPath?.takeIf {
                    File(it).exists()
                }.also {
                    if (it == null && profile.photoPath != null) {
                        Log.d(TAG,"Photo file not found: ${profile.photoPath}")
                    }
                }
                if (_state.value.photoPath != null && validPath == null) {
                    _state.update { it.copy(photoBitmap = null) }
                }
                _state.update {
                    it.copy(
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
                if (validPath != null && _state.value.photoBitmap == null) {
                    loadPhotoBitmap(validPath)
                }
            }
            .onStart { _state.update { it.copy(isLoading = true) } }
            .launchIn(viewModelScope)
    }

    private fun loadPhotoBitmap(path: String) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(File(path).absolutePath)?.asImageBitmap()
            }
            _state.update { it.copy(photoBitmap = bitmap) }
        }
    }

    fun onAction(action: ProfileAction) {
        Log.d(TAG,"Action -> $action")
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
                update { copy(weightUnit = action.unit) }
            }
            //is ProfileAction.PhotoChanged -> update { copy(photoPath = action.path) }
            ProfileAction.SaveClicked -> {
                save()
            }

            is ProfileAction.PhotoPicked -> {
                val savedPath = saveUriToCache(getApplication(), action.uri)
                //_state.update { it.copy(photoPath = savedPath) }
                //deleteOldPhotoIfExists(state.value.photoPath)
                update { copy(tmpPhotoPath = savedPath) }
                loadPhotoBitmap(savedPath)
            }

            is ProfileAction.PhotoCaptured -> {
                // uri już zapisane w pliku, wystarczy przypisać path do state
                //_state.update { it.copy(photoPath = action.capturedFilePath) }
                //deleteOldPhotoIfExists(state.value.photoPath)
                update { copy(tmpPhotoPath = action.capturedFilePath) }
                loadPhotoBitmap(action.capturedFilePath)
            }

            ProfileAction.ToggleWeightUnit -> {
                val newWeightUnit = if (state.value.weightUnit == WeightUnit.KG) WeightUnit.LB else WeightUnit.KG
                onWeightUnitChange(newWeightUnit)
                update { copy(weightUnit =  newWeightUnit) }
            }

            ProfileAction.ToggleHeightUnit -> {
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

    private fun onWeightUnitChange(newWeightUnit: WeightUnit) {
        if (state.value.weightUnit == newWeightUnit) return
        val weight = state.value.targetWeight.toBigDecimalOrNull() ?: return
        Log.d(TAG,"onWeightUnitChange: $newWeightUnit, weight: $weight")
        val newWeightValue = when(newWeightUnit) {
            WeightUnit.KG -> weight.toFloat().lbsToKg()
            WeightUnit.LB -> weight.toFloat().kgToLbs()
        }
        val newWeightStr = "%.1f".format(newWeightValue).replace(".0", "")
        Log.d(TAG,"newWeightStr: $newWeightStr")
        update { copy(targetWeight = newWeightStr) }
    }

    private fun save() {
        val s = state.value
        val age = s.age.toIntOrNull()
        val height = s.height.toIntOrNull()
        val weight = s.targetWeight.toBigDecimalOrNull()
        if (age != null && age !in 5..120) {
            sendError("Nieprawidłowy wiek")
            return
        }
        if (height != null && height !in 50..250) {
            sendError("Nieprawidłowy wzrost")
            return
        }
        if (weight != null && weight <= BigDecimal.ZERO) {
            sendError("Nieprawidłowa waga")
            return
        }

        val finalPhoto = if (s.tmpPhotoPath != null) {
            moveTmpToFinal(getApplication(), from = s.tmpPhotoPath, to = PROFILE_PHOTO_FILENAME)
        } else {
            s.photoPath
        }
        //_state.update { it.copy(photoPath = finalPhoto, tmpPhotoPath = null) }
        Log.d(TAG,"Final photo: $finalPhoto")
        val entity = UserProfileEntity(
            id = 0,
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

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                AppModule.provideUserProfileRepository().save(entity)
                _state.update {
                    it.copy(isSaving = false, isDirty = false)
                }
                sendEvent(ProfileEvent.Saved((getApplication() as Context).getString(R.string.profile_saved)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendError("Błąd zapisu profilu: ${e.message}")
                _state.update {
                    it.copy(isSaving = false)
                }
            }
        }
    }

    private fun sendError(msg: String) {
        viewModelScope.launch {
            _events.send(ProfileEvent.Error(msg))
        }
    }

    private fun sendEvent(event: ProfileEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    /*
    fun saveUriToInternalFile(uri: Uri): String {
        val context = getApplication() as Context
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File(context.filesDir, "profile_photo.jpg")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
    */

    fun saveUriToCache(
        context: Context,
        uri: Uri
    ): String {
        //val timestamp = System.currentTimeMillis()
        //val fileName = "profile_tmp_$timestamp.jpg"
        val fileName = "profile_tmp_gallery.jpg"
        val file = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open uri: $uri")
        return file.absolutePath
    }

    fun moveTmpToFinal(
        context: Context,
        from: String,
        to: String
    ): String {
        val finalFile = File(context.filesDir, to)
        val tmpFile = File(from)
        tmpFile.copyTo(finalFile, overwrite = true)
        Log.d(TAG,"Copied tmp file to: ${finalFile.absolutePath}, file size = ${finalFile.length()}")
        Log.d(TAG,"Delete tmp file: ${tmpFile.absolutePath}")
        return finalFile.absolutePath
    }
}