package com.example.gearkeeper.ui.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gearkeeper.R
import com.example.gearkeeper.data.local.ServiceTypeCatalogItem
import com.example.gearkeeper.ui.util.localizedDisplayName
import com.example.gearkeeper.ui.util.rememberPreferSpanishLocale

@Composable
fun ServiceCatalogListRow(
    item: ServiceTypeCatalogItem,
    onEditCustomClick: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val preferSpanish: Boolean = rememberPreferSpanishLocale()
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
            ) {
                Text(
                    text = item.localizedDisplayName(preferSpanish = preferSpanish),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (item.isSeeded) {
                    Text(
                        text = stringResource(id = R.string.service_catalog_badge_built_in),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.service_catalog_badge_custom),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (!item.isSeeded) {
                IconButton(onClick = onEditCustomClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.cd_edit_service_type),
                    )
                }
            }
        }
    }
}
