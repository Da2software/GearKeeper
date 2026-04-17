package com.example.gearkeeper.ui.screens.history

// Search field is visual-only until history is loaded from SQLite; then use repository + SqlSearch like other lists.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gearkeeper.R
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionEmptyState
import com.example.gearkeeper.ui.patterns.SectionListHeader

@Composable
fun MaintenanceHistoryScreen(
    modifier: Modifier = Modifier,
): Unit {
    var searchQuery: String by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = ListScreenHorizontalPadding,
                vertical = ListScreenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(ListScreenSectionSpacing),
    ) {
        item {
            SectionListHeader(
                title = null,
                searchQuery = searchQuery,
                onSearchQueryChange = { value: String -> searchQuery = value },
                onSearchSubmit = { },
                searchPlaceholder = stringResource(id = R.string.history_search_placeholder),
            )
        }
        item {
            SectionEmptyState(
                message = stringResource(id = R.string.history_empty_message),
                hint = stringResource(id = R.string.history_empty_hint),
            )
        }
    }
}
