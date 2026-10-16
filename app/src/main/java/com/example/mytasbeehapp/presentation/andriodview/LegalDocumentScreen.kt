package com.example.mytasbeehapp.presentation.andriodview

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF0B6B4B)

@Composable
fun LegalDocumentScreen(
    title: String,
    effectiveDate: String,
    sections: List<LegalSection>,
    contactEmail: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        SelectionContainer {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Effective date: $effectiveDate",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(20.dp))

                sections.forEach { section ->
                    if (section.heading != null) {
                        Text(
                            text = section.heading,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (section.body != null) {
                        Text(
                            text = section.body,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                        )
                    }

                    section.bullets?.forEach { bullet ->
                        Row(modifier = Modifier.padding(top = 6.dp)) {
                            Text(
                                text = "•  ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Green
                            )
                            Text(
                                text = bullet,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Contact us",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Questions about this document? Reach us at:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = contactEmail,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green,
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$contactEmail")
                            }
                        )
                    }
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}