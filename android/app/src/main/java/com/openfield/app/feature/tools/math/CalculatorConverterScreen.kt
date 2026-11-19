package com.openfield.app.feature.tools.math

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText

@Composable
fun CalculatorConverterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Expression Calc", "Unit Converter")

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Offline Calculator & Units",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
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
        ) {
            Surface(
                color = OpenFieldBackground,
                border = BorderStroke(0.5.dp, OpenFieldCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Button(
                            onClick = { selectedTab = index },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                contentColor = if (isSelected) Color.White else OpenFieldNavyText
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, OpenFieldCardBorder) else null
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                ExpressionCalculatorView()
            } else {
                UnitConverterView()
            }
        }
    }
}

@Composable
private fun ExpressionCalculatorView() {
    var exprInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        if (exprInput.isBlank()) return
        when (val res = DeterministicMathEngine.evaluate(exprInput)) {
            is MathResult.Success -> {
                resultText = "= ${res.formatted}"
                errorMessage = null
            }
            is MathResult.Error -> {
                errorMessage = res.message
                resultText = ""
            }
        }
    }

    fun append(char: String) {
        exprInput += char
        errorMessage = null
    }

    fun clear() {
        exprInput = ""
        resultText = ""
        errorMessage = null
    }

    fun backspace() {
        if (exprInput.isNotEmpty()) {
            exprInput = exprInput.dropLast(1)
        }
        errorMessage = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Display Screen
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = OpenFieldSecondaryBackground,
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = exprInput.ifBlank { "0" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = OpenFieldNavyText,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (resultText.isNotBlank()) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldPrimaryBlue,
                            textAlign = TextAlign.End
                        )
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OpenFieldError,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Keypad Grid
        val buttonRows = listOf(
            listOf("C", "(", ")", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "^", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        val isOp = label in listOf("/", "*", "-", "+", "^")
                        val isEquals = label == "="
                        val isClear = label == "C"

                        Button(
                            onClick = {
                                when (label) {
                                    "=" -> calculate()
                                    "C" -> clear()
                                    else -> append(label)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isEquals -> OpenFieldPrimaryBlue
                                    isOp -> OpenFieldPrimaryBlueLight
                                    isClear -> OpenFieldSecondaryBackground
                                    else -> Color.White
                                },
                                contentColor = when {
                                    isEquals -> Color.White
                                    isOp -> OpenFieldPrimaryBlue
                                    else -> OpenFieldNavyText
                                }
                            ),
                            border = BorderStroke(1.dp, OpenFieldCardBorder)
                        ) {
                            Text(
                                text = label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick scientific helper row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("sqrt(", "abs(", "%", "⌫").forEach { fn ->
                    Button(
                        onClick = {
                            if (fn == "⌫") backspace() else append(fn)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OpenFieldSecondaryBackground,
                            contentColor = OpenFieldNavyText
                        ),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Text(text = fn, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitConverterView() {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var inputValue by remember { mutableStateOf("1") }

    val units = remember(selectedCategory) { UnitConverter.getUnits(selectedCategory) }
    var fromUnit by remember(selectedCategory) { mutableStateOf(units.first().id) }
    var toUnit by remember(selectedCategory) { mutableStateOf(units.getOrNull(1)?.id ?: units.first().id) }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val convertedResult = remember(inputValue, fromUnit, toUnit, selectedCategory) {
        val num = inputValue.toDoubleOrNull()
        if (num == null) ""
        else {
            try {
                val res = UnitConverter.convert(num, fromUnit, toUnit, selectedCategory)
                val formatted = UnitConverter.formatConverted(res)
                val symbol = units.find { it.id == toUnit }?.symbol ?: ""
                "$formatted $symbol"
            } catch (e: Exception) {
                "Error"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category Chips
        ScrollableTabRow(
            selectedTabIndex = UnitCategory.entries.indexOf(selectedCategory),
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent
        ) {
            UnitCategory.entries.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = cat
                        val newUnits = UnitConverter.getUnits(cat)
                        fromUnit = newUnits.first().id
                        toUnit = newUnits.getOrNull(1)?.id ?: newUnits.first().id
                    },
                    text = {
                        Text(
                            text = cat.displayName,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategory == cat) OpenFieldPrimaryBlue else OpenFieldSecondaryText
                        )
                    }
                )
            }
        }

        // Input Value Field
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Input Value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OpenFieldPrimaryBlue,
                unfocusedBorderColor = OpenFieldCardBorder
            )
        )

        // From Unit Selector
        ExposedDropdownMenuBox(
            expanded = fromExpanded,
            onExpandedChange = { fromExpanded = !fromExpanded }
        ) {
            val fromName = units.find { it.id == fromUnit }?.name ?: fromUnit
            OutlinedTextField(
                value = fromName,
                onValueChange = {},
                readOnly = true,
                label = { Text("From Unit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = fromExpanded,
                onDismissRequest = { fromExpanded = false }
            ) {
                units.forEach { u ->
                    DropdownMenuItem(
                        text = { Text("${u.name} (${u.symbol})") },
                        onClick = {
                            fromUnit = u.id
                            fromExpanded = false
                        }
                    )
                }
            }
        }

        // Swap button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                }
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap units",
                    tint = OpenFieldPrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // To Unit Selector
        ExposedDropdownMenuBox(
            expanded = toExpanded,
            onExpandedChange = { toExpanded = !toExpanded }
        ) {
            val toName = units.find { it.id == toUnit }?.name ?: toUnit
            OutlinedTextField(
                value = toName,
                onValueChange = {},
                readOnly = true,
                label = { Text("To Unit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = toExpanded,
                onDismissRequest = { toExpanded = false }
            ) {
                units.forEach { u ->
                    DropdownMenuItem(
                        text = { Text("${u.name} (${u.symbol})") },
                        onClick = {
                            toUnit = u.id
                            toExpanded = false
                        }
                    )
                }
            }
        }

        // Converted Result Display
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = OpenFieldPrimaryBlueLight.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Converted Result",
                    style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = convertedResult.ifBlank { "0" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = OpenFieldNavyText
                    )
                )
            }
        }
    }
}
