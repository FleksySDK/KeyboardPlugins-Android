package co.thingthing.fleksyapps.mediashare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse

@SuppressLint("SetJavaScriptEnabled")
class AdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : WebView(context, attrs, defStyle) {

    private lateinit var adContent: MediaShareResponse.Advertisement

    fun setContent(adContent: MediaShareResponse.Advertisement) {
        this.adContent = adContent
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loadContentTest()
    }

    init {
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.domStorageEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        setWebContentsDebuggingEnabled(true)

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.let {
                    val intent = Intent(Intent.ACTION_VIEW, it)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
                return false
            }
        }
    }

    private fun loadContentTest() {
        adContent.let {
            val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <style>
                            body {
                                margin: 0;
                                padding: 0;
                                overflow: hidden;
                                width: 100%;
                                height: 100%;
                                background: transparent;
                                align-items: top left;
                                justify-content: top left;
                                box-sizing: border-box;
                            }
                            .content {
                                margin: 0;
                                padding: 0;
                                box-sizing: border-box;
                                display: flex;
                                align-items: top left;
                                justify-content: top left;
                                text-align: top left;
                                font-size: 16px;
                                transform-origin: top left;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="content">${it.content}</div>
                        <script>
                            function resizeContent() {
                                var container = document.body;
                                var content = document.querySelector('.content');

                                // Get container dimensions
                                var containerWidth = container.clientWidth;
                                var containerHeight = container.clientHeight;
                                
                                // Use known dimensions of the content
                                var contentWidth = ${it.width};
                                var contentHeight = ${it.height};

                                // Determine the scaling factor
                                var scale;
                                if (containerWidth > contentWidth || containerHeight > contentHeight) {
                                    // Container is larger than content, scale up
                                    
                                    // Determine the largest non-zero dimension for the container
                                    var largestContainerDimension = Math.max(
                                        containerWidth || Number.MIN_VALUE,
                                        containerHeight || Number.MIN_VALUE
                                    );
    
                                    // If both dimensions are zero, set a default fallback dimension
                                    if (largestContainerDimension === Number.MIN_VALUE) {
                                        largestContainerDimension = 100; // Fallback value
                                        containerWidth = containerHeight = largestContainerDimension;
                                    }
                                    
                                    // Calculate scaling factors
                                    var scaleX = (containerWidth || largestContainerDimension) / contentWidth;
                                    var scaleY = (containerHeight || largestContainerDimension) / contentHeight;
                                    scale = Math.max(scaleX, scaleY);
                                    
                                } else {
                                    // Content is larger than container, scale down
                                    
                                    // Determine the smallest non-zero dimension for the container
                                    var smallestContainerDimension = Math.min(
                                        containerWidth || Number.MAX_VALUE,
                                        containerHeight || Number.MAX_VALUE
                                    );

                                    // If both dimensions are zero, set a default fallback dimension
                                    if (smallestContainerDimension === Number.MAX_VALUE) {
                                        smallestContainerDimension = 100; // Fallback value
                                    }
                                    
                                    // Calculate scaling factors
                                    var scaleX = (containerWidth || smallestContainerDimension) / contentWidth;
                                    var scaleY = (containerHeight || smallestContainerDimension) / contentHeight;
                                    scale = Math.min(scaleX, scaleY);
                                }

                                // Apply scaling
                                content.style.transform = 'scale(' + scale + ')';
                                content.style.transformOrigin = 'top left';

                                // Calculate scaled content dimensions
                                var scaledContentWidth = contentWidth * scale;
                                var scaledContentHeight = contentHeight * scale;
                    
                                // Set the size of the content div
                                content.style.width = scaledContentWidth + 'px';
                                content.style.height = scaledContentHeight + 'px';
                            }

                            window.onload = function() {
                                resizeContent();
                            };
                            window.onresize = function() {
                                resizeContent();
                            };
                        </script>
                    </body>
                    </html>
                """.trimIndent()

            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    }
}