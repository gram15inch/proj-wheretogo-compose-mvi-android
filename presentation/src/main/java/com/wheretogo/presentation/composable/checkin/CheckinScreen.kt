package com.wheretogo.presentation.composable.checkin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.Tier
import com.wheretogo.presentation.viewmodel.CheckinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(
    onGalleyClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: CheckinViewModel = hiltViewModel(),
) {

    val stamps by viewModel.stamps.collectAsState()
    BackHandler {
        onBackClick()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkin_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            item {
                TierCard(certifiedCount = stamps.size)
            }

            item {
                RecentGallery(
                    photos = stamps,
                    onViewAllClick = onGalleyClick,
                    onPhotosClick = onGalleyClick
                )
            }

            item {
                Text(
                    text = stringResource(R.string.tier_roadmap),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(Tier.default) { tier ->
                if (tier.requiredCount > 0)
                    TierBar(
                        tier = tier,
                        achieved = stamps.size >= tier.requiredCount,
                    )
            }
        }
    }
}
