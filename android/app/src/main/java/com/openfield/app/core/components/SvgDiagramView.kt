package com.openfield.app.core.components

import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground

@Composable
fun SvgDiagramCard(
    rawCode: String,
    modifier: Modifier = Modifier
) {
    val svgXml = remember(rawCode) { SvgParser.extractSvg(rawCode) }
    var showSource by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "VECTOR GRAPHIC (SVG)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = OpenFieldPrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = {
                            clipboard.setText(AnnotatedString(svgXml))
                            Toast.makeText(context, "SVG code copied", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = OpenFieldPrimaryBlue, modifier = Modifier.size(12.dp))
                            Text("Copy", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = OpenFieldPrimaryBlue))
                        }
                    }

                    Surface(
                        onClick = { showSource = !showSource },
                        shape = RoundedCornerShape(6.dp),
                        color = OpenFieldPrimaryBlueLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(12.dp))
                            Text(
                                text = if (showSource) "View Vector" else "View XML",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (showSource) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = svgXml,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                // Interactive SVG Render Canvas via lightweight offline WebView
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, OpenFieldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = false
                                settings.allowFileAccess = false
                                setBackgroundColor(android.graphics.Color.WHITE)
                                val html = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                                    <style>
                                      body { margin: 0; padding: 12px; display: flex; justify-content: center; align-items: center; background: #FFFFFF; }
                                      svg { max-width: 100%; height: auto; max-height: 190px; }
                                    </style>
                                    </head>
                                    <body>
                                    $svgXml
                                    </body>
                                    </html>
                                """.trimIndent()
                                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
