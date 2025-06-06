package com.basset.operations.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.basset.core.domain.model.OperationType
import com.basset.core.navigation.OperationRoute
import com.basset.operations.presentation.cut_operation.components.CutOperation

@Composable
fun OperationScreenContent(
    pickedFile: OperationRoute, accentColor: Color
) {
    when (pickedFile.operationType) {
        OperationType.COMPRESS -> TODO()
        OperationType.CONVERT -> TODO()
        OperationType.BG_REMOVE -> TODO()
        OperationType.CUT -> CutOperation(pickedFile = pickedFile, accentColor = accentColor)
    }
}