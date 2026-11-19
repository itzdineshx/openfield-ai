package com.openfield.app.feature.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.MarkdownText
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.BubbleShapeUser
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun LearnScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val topic by repository.currentTopic.collectAsState()
    var followUpPrompt by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Learn & Explain",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = { }
            )
        },
        bottomBar = {
            ChatBottomComposer(
                value = followUpPrompt,
                onValueChange = { followUpPrompt = it },
                onSend = { prompt ->
                    if (prompt.isNotBlank()) {
                        repository.setTopic(prompt)
                        followUpPrompt = ""
                    }
                },
                onAttach = { },
                onVoiceClick = { },
                placeholder = "Ask a concept to explain...",
                modifier = Modifier.imePadding()
            )
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val currentTopic = topic
            if (currentTopic == null) {
                Text(
                    text = "Ask any concept below to get a real, on-device explanation.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = OpenFieldNavyText
                    )
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = BubbleShapeUser,
                        color = OpenFieldPrimaryBlueLight,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = currentTopic.prompt,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                color = OpenFieldNavyText
                            ),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExplanationModeContent(topic = currentTopic)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExplanationModeContent(topic: com.openfield.app.core.model.LearningTopic) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                OpenFieldLogo(size = 28.dp)
                Spacer(modifier = Modifier.width(10.dp))
                MarkdownText(
                    text = topic.definition.ifBlank { "Generating explanation…" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = OpenFieldNavyText
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (topic.inShortPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "In short:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OpenFieldNavyText
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                topic.inShortPoints.forEach { point ->
                    Text(
                        text = "• $point",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = OpenFieldNavyText
                        ),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}
