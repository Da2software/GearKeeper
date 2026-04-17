package com.example.gearkeeper.ui.patterns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R

/** Case-insensitive substring match; blank query matches everything. */
fun matchesListSearch(query: String, vararg haystacks: String): Boolean {
    val needle: String = query.trim()
    if (needle.isEmpty()) {
        return true
    }
    return haystacks.any { stack: String -> stack.contains(other = needle, ignoreCase = true) }
}

/** Horizontal / outer padding for scrollable list screens (drawer destinations). */
val ListScreenHorizontalPadding: Dp = 24.dp

/** Vertical padding from top/bottom when a screen uses only [ListScreenHorizontalPadding] on the scroll container. */
val ListScreenVerticalPadding: Dp = 24.dp

/** Space between list cards / major blocks. */
val ListScreenSectionSpacing: Dp = 12.dp

/**
 * Standard header for list-first screens: title, optional search field **or** supporting line,
 * optional full-width primary action.
 *
 * Search fields only run a query when the user taps the trailing **search** icon ([onSearchSubmit]).
 * The text field updates [searchQuery] via [onSearchQueryChange] as the user types (no auto-query).
 */
@Composable
fun SectionListHeader(
    /** Omit when the top app bar already shows the same screen title (drawer destinations). */
    title: String? = null,
    /** Shown under [title] when present (e.g. current odometer). */
    subtitleBelowTitle: String? = null,
    supportingText: String? = null,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    onSearchSubmit: (() -> Unit)? = null,
    searchPlaceholder: String? = null,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
): Unit {
    val resolvedSearchPlaceholder: String = searchPlaceholder
        ?: stringResource(id = R.string.search_placeholder_default)
    val searchTopPadding: Dp = when {
        title != null -> 8.dp
        subtitleBelowTitle != null -> 8.dp
        else -> 0.dp
    }
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        if (subtitleBelowTitle != null) {
            Text(
                text = subtitleBelowTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
            )
        }
        if (onSearchQueryChange != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = searchTopPadding, bottom = 8.dp),
                placeholder = { Text(text = resolvedSearchPlaceholder) },
                trailingIcon = {
                    if (onSearchSubmit != null) {
                        IconButton(onClick = onSearchSubmit) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(id = R.string.cd_run_search),
                            )
                        }
                    }
                },
                singleLine = true,
            )
        } else if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (title != null) 4.dp else 0.dp, bottom = 8.dp),
            )
        } else if (primaryActionLabel != null && onPrimaryAction != null) {
            Spacer(modifier = Modifier.height(height = 8.dp))
        }
        if (primaryActionLabel != null && onPrimaryAction != null) {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                Text(text = primaryActionLabel)
            }
        }
    }
}

/**
 * Standard empty list message: short headline sentence + optional muted hint.
 * Always center-aligned, full width; use inside a [LazyColumn] item or below a header.
 */
@Composable
fun SectionEmptyState(
    message: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
): Unit {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}
