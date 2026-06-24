package com.oxi.calc.wear

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.wear.compose.material3.*
import androidx.wear.compose.foundation.*
import androidx.wear.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oxi.calc.shared.CalculatorViewModel
import com.oxi.calc.shared.ButtonType
import com.oxi.calc.shared.handleAction
import com.oxi.calc.shared.R

enum class WearScreenState { Calculator, Scientific, History, Menu }

@Composable
fun WearCalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val colorScheme = MaterialTheme.colorScheme
    var screenState by remember { mutableStateOf(WearScreenState.Calculator) }
    val configuration = LocalConfiguration.current
    val isRound = configuration.isScreenRound

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // 1. DYNAMIC CONTENT LAYER
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val safeScale = if (isRound) 0.707f else 0.95f
            val safeWidth = maxWidth * safeScale
            val safeHeight = maxHeight * safeScale

            Column(
                modifier = Modifier
                    .size(safeWidth, safeHeight)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Display (Shown only in Calculator mode)
                if (screenState == WearScreenState.Calculator) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.25f)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = viewModel.historyTextFieldValue.text,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 6.sp,
                                textAlign = TextAlign.End,
                                maxLines = 1
                            )
                            
                            val textLength = viewModel.textFieldValue.text.length
                            val fontSize = when {
                                textLength > 15 -> 8.sp
                                textLength > 10 -> 11.sp
                                else -> 16.sp
                            }
                            
                            BasicTextField(
                                value = viewModel.textFieldValue,
                                onValueChange = { viewModel.updateTextFieldValue(it) },
                                readOnly = true,
                                textStyle = TextStyle(
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = fontSize,
                                    textAlign = TextAlign.End
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Grid Content based on state
                Box(modifier = Modifier.weight(0.75f)) {
                    when (screenState) {
                        WearScreenState.Calculator -> WearKeypad(viewModel)
                        WearScreenState.Scientific -> WearScientificPanel(viewModel)
                        WearScreenState.History -> WearHistoryPanel(viewModel, onSelect = { screenState = WearScreenState.Calculator })
                        WearScreenState.Menu -> WearUtilityMenu(onNavigate = { screenState = it })
                    }
                }
            }
        }

        // 2. UNIFIED UNIVERSAL SIDE TAB (Material You style)
        // This tab handles opening Menu and closing ALL sub-panels
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            val isBackAction = screenState != WearScreenState.Calculator
            
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(colorScheme.primaryContainer.copy(alpha = 0.85f))
                    .clickable { 
                        if (isBackAction) {
                            screenState = WearScreenState.Calculator
                        } else {
                            screenState = WearScreenState.Menu
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isBackAction) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = if (isBackAction) stringResource(R.string.back) else stringResource(R.string.menu), 
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(10.dp).graphicsLayer { 
                        // If it's Menu arrow (Calculator state), it points inwards
                        if (!isBackAction) rotationZ = 180f 
                    }
                )
            }
        }
    }
}

@Composable
fun WearKeypad(viewModel: CalculatorViewModel) {
    val buttons = listOf(
        listOf("AC", "DEL", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("+/-", "0", ".", "=")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                row.forEach { btn ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        WearButton(
                            text = btn, 
                            modifier = Modifier.fillMaxSize(0.94f),
                            type = when {
                                btn in listOf("÷", "×", "-", "+", "=") -> ButtonType.Operation
                                btn in listOf("AC", "DEL", "%", "+/-") -> ButtonType.Special
                                else -> ButtonType.Number
                            }
                        ) {
                            handleAction(btn, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WearUtilityMenu(
    onNavigate: (WearScreenState) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.labelSmall, color = colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        MenuButton(stringResource(R.string.scientific), Icons.Default.Science) { onNavigate(WearScreenState.Scientific) }
        MenuButton(stringResource(R.string.history), Icons.Default.History) { onNavigate(WearScreenState.History) }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.back_to_calculator), fontSize = 6.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, isPrimary: Boolean = false, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPrimary) colorScheme.primary else colorScheme.surfaceContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(10.dp), tint = if (isPrimary) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
            Text(text, fontSize = 8.sp, color = if (isPrimary) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WearScientificPanel(viewModel: CalculatorViewModel) {
    val sciButtons = listOf(
        listOf("sin", "cos", "tan"),
        listOf("log", "ln", "sqrt"),
        listOf("sq", "pow", "pi"),
        listOf("e", "(", ")")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        sciButtons.forEach { row ->
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                row.forEach { btn ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        WearButton(
                            text = btn, 
                            modifier = Modifier.fillMaxSize(0.94f),
                            type = ButtonType.Scientific
                        ) {
                            if (btn == "pow") viewModel.onOperationClick("pow") else viewModel.onScientificClick(btn)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WearHistoryPanel(viewModel: CalculatorViewModel, onSelect: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize()) {
        Text(stringResource(R.string.history), fontSize = 10.sp, color = colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
        
        ScalingLazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(viewModel.calculationHistory) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.surfaceContainer.copy(alpha = 0.5f))
                        .clickable {
                            viewModel.onHistoryItemClick(item)
                            onSelect()
                        }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(item.expression, fontSize = 6.sp, color = colorScheme.onSurfaceVariant)
                    Text(item.result, fontSize = 8.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        if (viewModel.calculationHistory.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clickable { viewModel.clearHistory() },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.clear), fontSize = 8.sp, color = colorScheme.error)
            }
        }
    }
}

@Composable
fun WearButton(
    text: String, 
    type: ButtonType, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.90f else 1f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "scale")
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when (type) {
        ButtonType.Number -> colorScheme.surfaceContainer
        ButtonType.Operation -> colorScheme.primary
        ButtonType.Special -> colorScheme.secondaryContainer
        ButtonType.Scientific -> colorScheme.tertiaryContainer.copy(alpha = 0.8f)
    }
    val contentColor = when (type) {
        ButtonType.Number -> colorScheme.onSurfaceVariant
        ButtonType.Operation -> colorScheme.onPrimary
        ButtonType.Special -> colorScheme.onSecondaryContainer
        ButtonType.Scientific -> colorScheme.onTertiaryContainer
    }
    Box(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clip(RoundedCornerShape(6.dp)).background(containerColor).clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = if (text == "DEL") "⌫" else text, style = TextStyle(color = contentColor, fontWeight = FontWeight.Medium, fontSize = if (text.length > 2) 5.sp else 9.sp))
    }
}
