package com.halimjr11.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.halimjr11.domain.model.Review
import com.halimjr11.ui.components.RemoteImage

@Composable
internal fun ReviewCard(review: Review) {
    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
    val long = review.content.length > 240
    ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (review.avatarUrl.isNotBlank()) {
                    RemoteImage(review.avatarUrl, review.author, Modifier.size(40.dp).clip(CircleShape))
                } else {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        Alignment.Center
                    ) { Text(review.author.take(1).uppercase()) }
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(review.author, style = MaterialTheme.typography.titleSmall)
                    Text(review.date, style = MaterialTheme.typography.bodySmall)
                }
                if (review.rating.isNotBlank()) Text("★ ${review.rating}", style = MaterialTheme.typography.labelLarge)
            }
            Text(
                review.content,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
                modifier = if (long) Modifier.clickable { expanded = !expanded } else Modifier
            )
            if (long) {
                Text(
                    if (expanded) "Show less" else "Read more",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        }
    }
}
