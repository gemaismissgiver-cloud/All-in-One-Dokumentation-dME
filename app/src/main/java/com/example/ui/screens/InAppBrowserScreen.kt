package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppBrowserScreen(
    onSaveWebClipToChapter: (url: String, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentUrl by remember { mutableStateOf("https://de.wikipedia.org") }
    var inputUrl by remember { mutableStateOf("https://de.wikipedia.org") }
    var pageTitle by remember { mutableStateOf("Freies Wissen") }
    var isLoading by remember { mutableStateOf(false) }

    var webView: WebView? by remember { mutableStateOf(null) }
    val focusManager = LocalFocusManager.current

    val bookmarks = remember {
        listOf(
            "Wikipedia" to "https://de.wikipedia.org",
            "Wissenschaft" to "https://www.nature.com",
            "DuckDuckGo" to "https://duckduckgo.com",
            "ArXiv Research" to "https://arxiv.org",
            "Gutenberg" to "https://www.gutenberg.org"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("in_app_browser_screen")
    ) {
        // Top URL Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { webView?.goBack() },
                        enabled = webView?.canGoBack() == true,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurück",
                            tint = if (webView?.canGoBack() == true) OffWhite else TextMuted
                        )
                    }

                    IconButton(
                        onClick = { webView?.goForward() },
                        enabled = webView?.canGoForward() == true,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Vorwärts",
                            tint = if (webView?.canGoForward() == true) OffWhite else TextMuted
                        )
                    }

                    IconButton(
                        onClick = { webView?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Neu Laden",
                            tint = ElectricViolet
                        )
                    }

                    // URL Input field
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("browser_url_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = OffWhite,
                            focusedBorderColor = NeonRedPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            var formatted = inputUrl.trim()
                            if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                                formatted = "https://$formatted"
                            }
                            currentUrl = formatted
                            inputUrl = formatted
                            webView?.loadUrl(formatted)
                            focusManager.clearFocus()
                        }),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            var formatted = inputUrl.trim()
                            if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                                formatted = "https://$formatted"
                            }
                            currentUrl = formatted
                            inputUrl = formatted
                            webView?.loadUrl(formatted)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Suchen / Laden",
                            tint = PureWhite
                        )
                    }
                }

                // Preset Bookmarks bar
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(bookmarks) { (label, url) ->
                        Surface(
                            onClick = {
                                currentUrl = url
                                inputUrl = url
                                webView?.loadUrl(url)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(0.5.dp, DarkCardBorder)
                        ) {
                            Text(
                                text = label,
                                color = OffWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = NeonRedPrimary,
                trackColor = DarkSurfaceVariant
            )
        }

        // Web Clipper Floating Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pageTitle.ifBlank { "Internet Explorer" },
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = currentUrl,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onSaveWebClipToChapter(currentUrl, pageTitle.ifBlank { currentUrl })
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_web_clip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("In Kapitel Speichern", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Android WebView view
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            url?.let {
                                currentUrl = it
                                inputUrl = it
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            url?.let {
                                currentUrl = it
                                inputUrl = it
                            }
                            view?.title?.let {
                                if (it.isNotBlank()) pageTitle = it
                            }
                        }
                    }
                    loadUrl(currentUrl)
                    webView = this
                }
            },
            update = { view ->
                webView = view
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
