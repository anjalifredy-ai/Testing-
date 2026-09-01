package com.rikky.blankct.ui.yfeed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.rikky.blankct.R

class YFeedFragment : Fragment(R.layout.fragment_yfeed) {

    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.et_y_search)
        webView = view.findViewById(R.id.webview_y)

        webView?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = userAgentString?.replace("; wv", "")
        }

        webView?.webViewClient = WebViewClient()
        webView?.loadUrl("https://www.youtube.com/shorts")

        etSearch.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val query = textView.text.toString().trim()
                if (query.isNotEmpty()) {
                    val url = "https://www.youtube.com/results?search_query=" +
                        java.net.URLEncoder.encode(query, "UTF-8")
                    webView?.loadUrl(url)
                }
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView = null
    }
}
