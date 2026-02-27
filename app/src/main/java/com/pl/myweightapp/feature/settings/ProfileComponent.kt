package com.pl.myweightapp.feature.settings

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.pl.myweightapp.R
import com.pl.myweightapp.domain.Gender
import java.io.File

private const val TAG = "ProfileComponent"

//@Composable
//fun ProfileComponent(
//    modifier: Modifier = Modifier,
//    snackbarHostState: SnackbarHostState,
//    viewModel: ProfileViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//
//    UiEventConsumer(
//        events = viewModel.events,
//        snackbarHostState = snackbarHostState
//    )
//
//    ProfileContent(
//        modifier = modifier,
//        state = state,
//        onAction = viewModel::onAction
//    )
//}

@Composable
fun ProfileComponent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
) {
    val context = LocalContext.current // <--- tutaj masz Context
    // Launcher do wyboru z galerii
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onAction(ProfileAction.PhotoPicked(uri))
        }
    }

    val capturedFile = remember {
        //val timestamp = System.currentTimeMillis()
        //val fileName = "profile_tmp_camera_${timestamp}.jpg"
        val fileName = "profile_tmp_camera.jpg"
        File(context.cacheDir, fileName)
    }
    //createTmpCameraFile(context)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        capturedFile
    )
    // Launcher do zrobienia zdjęcia aparatem
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            onAction(ProfileAction.PhotoCaptured(capturedFile.absolutePath))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Lewa kolumna z polami
        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .padding(horizontal = 0.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(ProfileAction.NameChanged(it)) },
                label = { Text(stringResource(R.string.profile_name)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .clickable { onAction(ProfileAction.ToggleGender) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        when(state.gender) {
                            Gender.MALE -> Image(painter = painterResource(R.drawable.ic_man), contentDescription = "")
                            Gender.FEMALE -> Image(painter = painterResource(R.drawable.ic_woman), contentDescription = "")
                            Gender.UNSPECIFIED -> Image(painter = painterResource(R.drawable.ic_remove), contentDescription = "")
                        }
                    }
                }
            )

            OutlinedTextField(
                value = state.age,
                onValueChange = { onAction(ProfileAction.AgeChanged(it)) },
                label = { Text(stringResource(R.string.profile_age)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.height,
                onValueChange = { onAction(ProfileAction.HeightChanged(it)) },
                label = { Text(stringResource(R.string.profile_height)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .clickable { onAction(ProfileAction.ToggleHeightUnit) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(state.heightUnit.name)
                    }
                }
            )

            OutlinedTextField(
                value = state.targetWeight,
                onValueChange = { onAction(ProfileAction.WeightChanged(it)) },
                label = { Text(stringResource(R.string.profile_target_weight)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .clickable { onAction(ProfileAction.ToggleWeightUnit) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(state.weightUnit.name)
                    }
                }
            )
        }

        // Prawa kolumna z obrazkiem
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()     // dopasowuje wysokość do Row (czyli lewej kolumny)
                    .aspectRatio(1f)     // wymusza kwadrat
                //.background(Color.Green)
                ,
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.photoBitmap != null -> {
                        Log.d(TAG,"Load avatar from bitmap")
                        Image(
                            painter = BitmapPainter(state.photoBitmap),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                //.aspectRatio(1f)
                                .border(3.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    state.isLoading -> {
                        Log.d(TAG,"CircularProgressIndicator")
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    state.photoPath == null -> {
                        Log.d(TAG,"Load avatar from resource")
                        Image(
                            painter = painterResource(R.drawable.ic_face3),
                            contentDescription = "Avatar",
                        )
                    }
                    else -> {
                        // photoPath != null, ale photoBitmap == null i isLoading == false
                        // czyli bitmapa jeszcze się nie załadowała albo problem przy ładowaniu bitmapy
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Row {
                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    contentPadding = PaddingValues(start = 4.dp)
                ) {
                    //Text("Wybierz z galerii")
                    Icon(
                        painter = painterResource(R.drawable.ic_gallery_thumbnail),
                        contentDescription = stringResource(R.string.profile_pick_from_gallery),
                        modifier = Modifier.size(28.dp), // rozmiar samej ikonki
                        //tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Button(
                    onClick = { takePhotoLauncher.launch(uri) },
                    contentPadding = PaddingValues(start = 4.dp)
                ) {
                    //Text("Zrób zdjęcie")
                    Icon(
                        painter = painterResource(R.drawable.ic_outline_add_a_photo), // tu możesz wrzucić inną ikonę aparatu
                        contentDescription = stringResource(R.string.profile_take_a_photo),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        Button(
            onClick = {
                onAction(ProfileAction.SaveClicked)
            },
            enabled = state.isDirty && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {

            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.profile_save))
            }
        }
    }
}

@Preview
@Composable
fun ProfilePreview() {
    //val snackbarHostState = remember { SnackbarHostState() }
    val fakeState = ProfileUiState(
        name = "Kamil",
        age = "50",
        height = "176",
        targetWeight = "82.5",
        gender = Gender.MALE,
        isDirty = true
    )
    ProfileComponent(
        state = fakeState,
        onAction = { action ->
            Log.d(TAG,"got action: $action")
        }
    )
}

@Preview(name = "Saving")
@Composable
fun ProfilePreviewSaving() {
    ProfileComponent(
        state = ProfileUiState(isSaving = true),
        onAction = { }
    )
}
