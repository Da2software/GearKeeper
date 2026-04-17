package com.example.gearkeeper.ui.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.ServiceTypeCatalogItem
import com.example.gearkeeper.data.local.ServiceTypeRepository
import com.example.gearkeeper.ui.patterns.ListScreenHorizontalPadding
import com.example.gearkeeper.ui.patterns.ListScreenSectionSpacing
import com.example.gearkeeper.ui.patterns.ListScreenVerticalPadding
import com.example.gearkeeper.ui.patterns.SectionEmptyState
import com.example.gearkeeper.ui.patterns.SectionListHeader

@Composable
fun ServicesHomeScreen(
    serviceTypeRepository: ServiceTypeRepository,
    onAddCustomService: () -> Unit,
    onEditCustomService: (serviceTypeId: Long) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    var serviceTypes: List<ServiceTypeCatalogItem> by remember { mutableStateOf(emptyList()) }
    var refreshTick: Int by remember { mutableStateOf(0) }
    var searchDraft: String by remember { mutableStateOf("") }
    var appliedSearch: String by remember { mutableStateOf("") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTick += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(refreshTick) {
        serviceTypes = serviceTypeRepository.getAllCatalogItemsOrderedByName(searchQuery = appliedSearch)
    }

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
                searchQuery = searchDraft,
                onSearchQueryChange = { value: String -> searchDraft = value },
                onSearchSubmit = {
                    appliedSearch = searchDraft.trim()
                    serviceTypes =
                        serviceTypeRepository.getAllCatalogItemsOrderedByName(searchQuery = appliedSearch)
                },
                searchPlaceholder = stringResource(id = R.string.catalog_search_placeholder),
                primaryActionLabel = stringResource(id = R.string.catalog_add_custom_service),
                onPrimaryAction = onAddCustomService,
            )
        }
        items(
            items = serviceTypes,
            key = { item: ServiceTypeCatalogItem -> item.id },
        ) { item: ServiceTypeCatalogItem ->
            ServiceCatalogListRow(
                item = item,
                onEditCustomClick = {
                    onEditCustomService(item.id)
                },
            )
        }
        if (serviceTypes.isEmpty()) {
            item {
                if (appliedSearch.isBlank()) {
                    SectionEmptyState(
                        message = stringResource(id = R.string.catalog_empty_no_types),
                        hint = stringResource(id = R.string.catalog_empty_no_types_hint),
                    )
                } else {
                    SectionEmptyState(
                        message = stringResource(id = R.string.catalog_empty_no_match),
                        hint = stringResource(id = R.string.try_different_search),
                    )
                }
            }
        }
    }
}
