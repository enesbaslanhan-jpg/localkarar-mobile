package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineStrong
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfacePanel
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.LkSpacing

@Composable
fun LkMetricCard(
    label: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary,
                modifier = Modifier.weight(1f)
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LkTextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        
        Text(
            text = value,
            style = LkTypography.getMetric(),
            color = LkTextPrimary
        )
    }
}
