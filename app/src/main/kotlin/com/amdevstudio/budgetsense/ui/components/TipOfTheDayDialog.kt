package com.amdevstudio.budgetsense.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val TipBubbleTextColor = Color(0xFF1E3A5F)

/**
 * Insets as fractions of the artwork size so tip text sits in the white callout only
 * (clears the character, tail, and purple bubble border). Tweak if the asset changes.
 */
private object TipBubbleInsets {
    /** Left: start past character + bubble tail into the white body (tune when asset layout changes). */
    const val startFraction = 0.37f
    /** Right: margin inside the bubble frame */
    const val endFraction = 0.074f
    /** Top/bottom: keep copy inside rounded corners; extra bottom clears deck icons under the bubble */
    const val topFraction = 0.27f
    const val bottomFraction = 0.28f
}

private fun resolveTipArtworkId(context: Context): Int {
    val pkg = context.packageName
    val res = context.resources
    for (name in listOf("tip_of_the_day", "tipoftheday")) {
        val id = res.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id
    }
    return 0
}

@Composable
fun TipOfTheDayDialog(
    description: String,
    onDismiss: () -> Unit,
    footnote: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val artworkId = remember(context) { resolveTipArtworkId(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .wrapContentHeight(),
        ) {
            if (artworkId != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Top)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White),
                ) {
                    Image(
                        painter = painterResource(artworkId),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.Top),
                        contentScale = ContentScale.FillWidth,
                    )

                    Box(
                        modifier = Modifier.matchParentSize(),
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = maxWidth * TipBubbleInsets.startFraction,
                                        top = maxHeight * TipBubbleInsets.topFraction,
                                        end = maxWidth * TipBubbleInsets.endFraction,
                                        bottom = maxHeight * TipBubbleInsets.bottomFraction,
                                    )
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = description,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                    ),
                                    color = TipBubbleTextColor,
                                    textAlign = TextAlign.Center,
                                )
                                if (footnote != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = footnote,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp,
                                        ),
                                        color = TipBubbleTextColor.copy(alpha = 0.88f),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                        .padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Text(description, style = MaterialTheme.typography.bodyLarge)
                    footnote?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}
