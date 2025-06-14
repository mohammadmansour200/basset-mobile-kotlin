package com.basset.operations.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.Log
import com.arthenica.ffmpegkit.LogCallback
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.StatisticsCallback
import com.basset.core.domain.model.MimeType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.data.android.getUriExtension
import com.basset.operations.domain.BackgroundRemover
import com.basset.operations.domain.MediaMetadataDataSource
import com.basset.operations.domain.MediaStoreManager
import com.basset.operations.domain.model.OutputFileInfo
import com.basset.operations.presentation.utils.progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OperationScreenViewModel(
    context: Context,
    val mediaStoreManager: MediaStoreManager,
    val metadataDataSource: MediaMetadataDataSource,
    val backgroundRemover: BackgroundRemover,
    val pickedFile: OperationRoute
) :
    ViewModel() {
    private val appContext: Context = context

    private val _state = MutableStateFlow(OperationScreenState())
    val state: StateFlow<OperationScreenState> = _state

    init {
        viewModelScope.launch {
            val metadata =
                metadataDataSource.loadMetadata(pickedFile.uri.toUri(), pickedFile.mimeType)
            _state.update { it.copy(metadata = metadata) }
        }
    }

    fun onAction(action: OperationScreenAction) {
        when (action) {
            is OperationScreenAction.OnCut -> handleCut(action.start, action.end)
            is OperationScreenAction.OnCompress -> handleCompress(
                action.compressionRate
            )

            is OperationScreenAction.OnConvert -> handleConvert(
                action.outputFormat
            )

            is OperationScreenAction.OnRemoveBackground -> {
                viewModelScope.launch {
                    handleBgRemove(
                        outputFileInfo = OutputFileInfo(
                            appContext.contentResolver.getUriExtension(pickedFile.uri.toUri())
                                .toString(),
                            null
                        )
                    )
                }
            }
        }
    }

    private fun handleCut(start: Double, end: Double) {
        val inputPath =
            FFmpegKitConfig.getSafParameterForRead(appContext, pickedFile.uri.toUri())
        viewModelScope.launch {
            runFFmpeg(
                command = "-ss $start -to $end -i $inputPath -c copy",
                outputFileInfo = OutputFileInfo(
                    appContext.contentResolver.getUriExtension(pickedFile.uri.toUri()).toString(),
                    null
                )
            )
        }
    }

    private fun handleCompress(compressionRate: Int) {
        when (pickedFile.mimeType) {
            MimeType.VIDEO, MimeType.AUDIO -> {

            }

            MimeType.IMAGE -> {

            }
        }
    }

    private fun handleConvert(outputFormat: String) {
        when (pickedFile.mimeType) {
            MimeType.VIDEO, MimeType.AUDIO -> {

            }

            MimeType.IMAGE -> {

            }
        }
    }

    private suspend fun runFFmpeg(
        command: String,
        outputFileInfo: OutputFileInfo
    ) = withContext(Dispatchers.IO) {
        val outputUri = mediaStoreManager.createMediaUri(pickedFile, outputFileInfo)
        _state.update { it.copy(isOperating = true) }

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(appContext, pickedFile.uri.toUri())
        val durationMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
        retriever.release()

        outputUri?.let {
            val outputPath = FFmpegKitConfig.getSafParameterForWrite(appContext, outputUri)
            FFmpegKit.executeAsync(
                "-y -protocol_whitelist saf,file,crypto $command $outputPath",
                object : FFmpegSessionCompleteCallback {
                    override fun apply(session: FFmpegSession) {
                        val state = session.getState()

                        when (session.returnCode.value) {
                            ReturnCode.CANCEL -> {
                                viewModelScope.launch {
                                    mediaStoreManager.deleteMedia(it)
                                }
                                _state.update { it.copy(isOperating = false) }
                            }

                            ReturnCode.SUCCESS -> {
                                viewModelScope.launch {
                                    mediaStoreManager.saveMedia(it, pickedFile)
                                }
                                _state.update { it.copy(isOperating = false) }
                            }
                        }

                        when (state) {
                            SessionState.FAILED -> {
                                viewModelScope.launch {
                                    mediaStoreManager.deleteMedia(it)
                                }
                                _state.update { it.copy(isOperating = false) }
                            }

                            SessionState.CREATED -> {
                                _state.update { it.copy(isOperating = true) }
                            }

                            else -> {}
                        }

                    }
                },
                object : LogCallback {
                    override fun apply(log: Log?) {
                        android.util.Log.d("ffmpeg-kit", log?.message.toString())
                        val progress = log?.progress(durationMs?.div(1000) ?: 0)
                        if (progress != null) _state.update { it.copy(progress = progress) }
                    }
                },
                object : StatisticsCallback {
                    override fun apply(statistics: Statistics?) {
                    }
                })
        }
    }

    private suspend fun handleBgRemove(outputFileInfo: OutputFileInfo) =
        withContext(Dispatchers.IO) {
            _state.update { it.copy(isOperating = true) }
            val outputUri = mediaStoreManager.createMediaUri(pickedFile, outputFileInfo)
            outputUri?.let { uri ->
                backgroundRemover.processImage(
                    uri = pickedFile.uri.toUri(),
                    onSuccess = { bitmap ->
                        val whiteBackgroundedBitmap = flattenTransparencyToWhite(bitmap)
                        viewModelScope.launch {
                            mediaStoreManager.writeBitmap(
                                uri = uri,
                                outputFileInfo = outputFileInfo,
                                bitmap = whiteBackgroundedBitmap
                            )
                            mediaStoreManager.saveMedia(
                                uri = uri,
                                pickedFile = pickedFile,
                            )
                            _state.update { it.copy(outputedFile = uri, isOperating = false) }
                        }
                    }, onFailure = { exception ->

                    }
                )
            }

        }

    private fun flattenTransparencyToWhite(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE) // Fill with white
        canvas.drawBitmap(source, 0f, 0f, null) // Draw original bitmap
        return result
    }
}