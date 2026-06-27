/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.foundation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wujia.foundation.designsystem.modifier.velarisClickable
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.model.theme.VelarisThemePreset

@Preview
@Composable
private fun Preview_DefaultPlayerCard() {
    // Preview using default values
    LogoCard(
        modifier = Modifier,
    )
}

@Composable
fun LogoCard(
    modifier: Modifier = Modifier,
    minHeight: Dp = 38.dp,
    iconSize: Dp = 78.dp,
    onClick: (() -> Unit)? = null,
) {
    val spec = VelarisTheme.spec
    val logoResId = when (VelarisTheme.currentPreset) {
        VelarisThemePreset.Gold -> R.drawable.ic_logo_gold
        VelarisThemePreset.Ocean -> R.drawable.ic_logo_ocean
        VelarisThemePreset.Forest -> R.drawable.ic_logo_forest
        VelarisThemePreset.Twilight -> R.drawable.ic_logo_twilight
    }
    val cardModifier = if (onClick != null) {
        modifier.velarisClickable(onClick = onClick)
    } else {
        modifier
    }
    Row(
        modifier = cardModifier
            .defaultMinSize(minHeight = minHeight)
            .heightIn(min = minHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(logoResId),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit,
                alpha = 0.88f,
            )
        }

        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = spec.colors.textPrimary.copy(alpha = spec.alpha.textPrimary),
                fontSize = spec.typography.body,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = stringResource(R.string.app_tagline),
                color = spec.colors.textSecondary.copy(alpha = 0.84f),
                fontSize = spec.typography.caption,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.28f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 6f,
                    ),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
