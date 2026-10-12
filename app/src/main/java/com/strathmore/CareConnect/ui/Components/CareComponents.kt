package com.strathmore.CareConnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.strathmore.CareConnect.ui.theme.Navy

/** White round icon button, used for Back and top-bar actions. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.size(44.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/** Replaces TopAppBar: optional round back button + logo, with optional actions on the right. */
@Composable
fun CareTopBar(
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            CircleIconButton(Icons.Default.ArrowBack, "Back", onBack)
            Spacer(modifier = Modifier.width(12.dp))
        }
        AppLogo()
        Spacer(modifier = Modifier.weight(1f))
        actions()
    }
}

/** Rounded white text field with a soft border. Drop-in replacement for OutlinedTextField. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        readOnly = readOnly,
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

/** Login / Register layout: navy header with title, then a rounded sheet holding the form. */
@Composable
fun AuthLayout(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 28.dp, end = 28.dp, top = 32.dp, bottom = 28.dp)
        ) {
            AppLogo(onDark = true)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp),
                content = content
            )
        }
    }
}

enum class NavTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    History("History", Icons.Default.History),
    Book("Book", Icons.Default.LocalHospital),
    Alerts("Alerts", Icons.Default.Notifications),
    Profile("Profile", Icons.Default.Person)
}

/** Floating pill bottom bar. The selected tab is a navy pill with its label. */
@Composable
fun CarePillNavBar(
    selected: NavTab,
    onSelect: (NavTab) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.values().forEach { tab ->
                if (tab == selected) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Navy)
                            .clickable { onSelect(tab) }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tab.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    IconButton(onClick = { onSelect(tab) }) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}