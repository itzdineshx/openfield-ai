package com.openfield.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText

@Composable
fun MermaidDiagramCard(
    rawCode: String,
    modifier: Modifier = Modifier
) {
    val steps = remember(rawCode) { MermaidParser.parseSteps(rawCode) }
    var showSource by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FLOWCHART DIAGRAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = OpenFieldPrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Surface(
                    onClick = { showSource = !showSource },
                    shape = RoundedCornerShape(6.dp),
                    color = OpenFieldPrimaryBlueLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (showSource) "View Flow" else "View Syntax",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OpenFieldPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (showSource || steps.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rawCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                // Interactive Flowchart Step Cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    steps.forEachIndexed { idx, step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // From Node
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = step.fromNode,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldNavyText,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            // Direction Arrow + Edge Condition
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                if (step.edgeLabel != null) {
                                    Text(
                                        text = step.edgeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = OpenFieldPrimaryBlue,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // To Node
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OpenFieldPrimaryBlueLight,
                                border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = step.toNode,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldPrimaryBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
