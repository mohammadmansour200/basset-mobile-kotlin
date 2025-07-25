package com.basset.operations.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.basset.R
import com.basset.core.domain.CoreConstants.AUDIO_CONVERT_OPTIONS_MIME_TYPES
import com.basset.core.domain.CoreConstants.IMAGE_MIME_TYPES
import com.basset.core.domain.CoreConstants.VIDEO_MIME_TYPES
import com.basset.core.domain.model.MediaType
import com.basset.core.navigation.OperationRoute
import com.basset.core.presentation.modifier.ContainerShapeDefaults
import com.basset.core.presentation.modifier.container
import com.basset.core.utils.MimeTypeMap
import com.basset.core.utils.isAudio
import com.basset.core.utils.isImage
import com.basset.core.utils.isVideo
import com.basset.operations.presentation.OperationScreenAction
import com.basset.operations.presentation.OperationScreenState
import com.basset.operations.utils.getUriExtension
import kotlinx.coroutines.launch

@Composable
fun ConvertOperation(
    modifier: Modifier = Modifier,
    onAction: (OperationScreenAction) -> Unit,
    operationScreenState: OperationScreenState,
    snackbarHostState: SnackbarHostState,
    pickedFile: OperationRoute
) {
    val snackScope = rememberCoroutineScope()
    val context = LocalContext.current

    val formats = arrayOf(*IMAGE_MIME_TYPES, *AUDIO_CONVERT_OPTIONS_MIME_TYPES, *VIDEO_MIME_TYPES)

    var selectedFormat by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isAudioToVideo =
        selectedFormat?.isVideo() == true && pickedFile.mediaType == MediaType.AUDIO
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.format_card_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )

        AnimatedContent(
            targetState = isAudioToVideo,
            label = "bg_remove_operation_animation",
            transitionSpec = {
                fadeIn(animationSpec = tween(220)).togetherWith(
                    fadeOut(
                        animationSpec = tween(90)
                    )
                )
            }
        ) { target ->
            Column {
                val containerModifier = @Composable { i: Int ->
                    if (target) Modifier.container(
                        shape = ContainerShapeDefaults.shapeForIndex(i, 2),
                        color = MaterialTheme.colorScheme.surface
                    ) else Modifier
                }
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .then(containerModifier(0))
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    val options = formats.filter {
                        val isImage = it.isImage()
                        val isCorrectMediaType =
                            if (pickedFile.mediaType == MediaType.IMAGE) isImage else !isImage

                        val pickedFileMimeType = MimeTypeMap.getMimeTypeFromExtension(
                            context.getUriExtension(pickedFile.uri.toUri()).toString()
                        )
                        val isDifferentMimeType = it != pickedFileMimeType

                        val isAudioToWebm =
                            it == "video/webm" && pickedFile.mediaType == MediaType.AUDIO

                        isCorrectMediaType && isDifferentMimeType && !isAudioToWebm
                    }
                    CompositionLocalProvider(
                        LocalLayoutDirection provides LayoutDirection.Ltr,
                    ) {
                        options.forEach {
                            val format = MimeTypeMap.getExtensionFromMimeType(it)
                                .toString()
                            EnhancedChip(
                                onClick = {
                                    selectedFormat = format
                                },
                                selected = selectedFormat == format,
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val icon =
                                            if (it.isImage()) ImageVector.vectorResource(R.drawable.image)
                                            else if (it.isAudio()) ImageVector.vectorResource(
                                                R.drawable.music_note
                                            )
                                            else ImageVector.vectorResource(R.drawable.movie)
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null
                                        )
                                        Text(
                                            text = format
                                        )
                                    }
                                },
                                selectedColor = MaterialTheme.colorScheme.tertiary,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                if (target) {
                    Spacer(Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .then(containerModifier(1))
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) {
                            if (it == null) return@rememberLauncherForActivityResult

                            if (context.contentResolver.getType(it)
                                    ?.contains("gif") == true
                            ) snackScope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.gif_not_supported_err))
                            } else selectedImageUri = it
                        }
                        Button(onClick = {
                            launcher.launch("image/*")
                        }) {
                            Text(
                                text = if (selectedImageUri == null) stringResource(R.string.choose_image_label) else stringResource(
                                    R.string.change_image_label
                                )
                            )
                        }

                        selectedImageUri?.let { uriString ->
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = uriString,
                                contentDescription = stringResource(R.string.selected_stillimage_content_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(120.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    ExecuteOperationBtn(
        onAction = {
            if (selectedFormat == null) {
                snackScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.no_format_selected_err))
                }
                return@ExecuteOperationBtn
            }

            if (isAudioToVideo && selectedImageUri == null) {
                snackScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.please_select_an_image_err))
                }
                return@ExecuteOperationBtn
            }

            if (isAudioToVideo) onAction(
                OperationScreenAction.OnAudioToVideoConvert(
                    selectedImageUri!!, selectedFormat!!
                )
            ) else onAction(
                OperationScreenAction.OnConvert(
                    selectedFormat!!
                )
            )
        },
        isCancellable = pickedFile.mediaType != MediaType.IMAGE,
        operationScreenState = operationScreenState,
        buttonLabel = stringResource(R.string.operation_convert_btn)
    )
}
