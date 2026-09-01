package com.rikky.blankct.ui.ifeed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.rikky.blankct.R

class IFeedFragment : Fragment(R.layout.fragment_ifeed) {

    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.et_i_search)
        webView = view.findViewById(R.id.webview_i)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = userAgentString?.replace("; wv", "")
        }

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cookieManager.flush()
                // Best-effort: hide Instagram's splash/login-nag overlay if present
                view?.evaluateJavascript(
                    """
                    (function() {
                        try {
                            var dialogs = document.querySelectorAll('div[role="dialog"]');
                            dialogs.forEach(function(d) { d.style.display = 'none'; });
                        } catch (e) {}
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }

        webView?.loadUrl("https://www.instagram.com/reels/")

        etSearch.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val query = textView.text.toString().trim()
                if (query.isNotEmpty()) {
                    val url = "https://www.instagram.com/explore/search/keyword/?q=" +
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
