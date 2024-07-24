package co.thingthing.fleksyapps.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import co.thingthing.fleksyapps.base.databinding.LayoutBaseFrameBinding
import co.thingthing.fleksyapps.base.databinding.LayoutConnectionErrorBinding
import co.thingthing.fleksyapps.base.databinding.LayoutEmptyErrorBinding
import co.thingthing.fleksyapps.base.databinding.LayoutFullViewBinding
import co.thingthing.fleksyapps.base.databinding.LayoutGeneralErrorBinding
import co.thingthing.fleksyapps.base.utils.empty
import co.thingthing.fleksyapps.base.utils.getInstallationUniqueId
import co.thingthing.fleksyapps.core.AppConfiguration
import co.thingthing.fleksyapps.core.AppInputState
import co.thingthing.fleksyapps.core.AppListener
import co.thingthing.fleksyapps.core.AppMedia
import co.thingthing.fleksyapps.core.AppMediaSource
import co.thingthing.fleksyapps.core.AppTheme
import co.thingthing.fleksyapps.core.KeyboardApp
import co.thingthing.fleksyapps.core.KeyboardAppViewMode
import co.thingthing.fleksyapps.core.TopBarMode
import com.facebook.drawee.backends.pipeline.Fresco
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.io.IOException

@SuppressLint("CheckResult")
abstract class BaseKeyboardApp : KeyboardApp {
    var listener: AppListener? = null
    private var frameView: BaseAppView? = null
    private var fullView: View? = null
    private var pagination = Pagination()
    private var contentSubscription = Disposables.disposed()
    private var categoriesSubscription = Disposables.disposed()
    private var autocompleteSubscription = Disposables.disposed()
    private var appConfiguration: AppConfiguration? = null
    private var isContentLoading = false
    private var isCategoriesLoading = false
    private var isItemLoading = false
    private var nextLoader: ((Pagination) -> Single<List<BaseResult>>)? = null

    lateinit var theme: AppTheme
    lateinit var inputState: AppInputState

    private lateinit var frameViewBinding: LayoutBaseFrameBinding
    private lateinit var frameConnectionErrorBinding: LayoutConnectionErrorBinding
    private lateinit var frameGeneralErrorBinding: LayoutGeneralErrorBinding
    private lateinit var frameEmptyErrorBinding: LayoutEmptyErrorBinding

    private lateinit var fullViewBinding: LayoutFullViewBinding
    private lateinit var fullConnectionErrorBinding: LayoutConnectionErrorBinding
    private lateinit var fullGeneralErrorBinding: LayoutGeneralErrorBinding
    private lateinit var fullEmptyErrorBinding: LayoutEmptyErrorBinding

    val locale get() = appConfiguration?.currentLocale
    val context get() = frameView?.context ?: fullView?.context

    abstract val configuration: BaseConfiguration

    open val customTypefaces: Typefaces? = null

    /**
     * The app name used in the app search hint
     */
    abstract val appName: String

    /**
     * The default list of results when no query or category selected
     */
    abstract fun default(pagination: Pagination): Single<List<BaseResult>>

    /**
     * A list of results for a category search
     */
    abstract fun category(category: BaseCategory, pagination: Pagination): Single<List<BaseResult>>

    /**
     * A list of results for a query search
     */
    abstract fun query(query: String, pagination: Pagination): Single<List<BaseResult>>

    /**
     * Show suggestions bar when in frame view mode.
     */
    open var topBarMode = TopBarMode.HIDDEN

    /**
     * An optional extended item from an existing list result before preview
     */
    open fun preview(baseResult: BaseResult): Single<BaseResult> = Single.just(baseResult)

    /**
     * A list of categories
     */
    abstract fun categories(): Single<List<BaseCategory>>

    abstract val defaultCategory: String

    override fun initialize(listener: AppListener, configuration: AppConfiguration) {
        this.listener = listener
        this.appConfiguration = configuration
    }

    override fun open(
        context: Context,
        theme: AppTheme,
        state: AppInputState,
        mode: KeyboardAppViewMode
    ): View {
        return when (mode) {
            is KeyboardAppViewMode.FrameView -> {
                fullView = null
                openFrameView(context, theme, state).also {
                    frameViewBinding.appInputContainer.requestFocus()
                }

            }

            is KeyboardAppViewMode.FullView -> {
                frameView = null
                openFullView(context, theme, state)
            }
        }
    }

    private fun openFrameView(context: Context, theme: AppTheme, state: AppInputState): View =
        createFrameView(context).apply {
            frameView = this
            nextLoader = null

            if (!Fresco.hasBeenInitialized()) {
                Fresco.initialize(context)
            }

            updateLoader(contentLoading = true, categoriesLoading = true, itemLoading = false)

            frameViewBinding.apply {
                appIcon.apply {
                    setImageDrawable(appIcon(context))
                    setOnClickListener { listener?.hide() }
                }

                appSearchClose.apply {
                    setOnClickListener { onSearchCloseClicked() }
                }

                zoomViewButton.apply {
                    setOnClickListener {
                        listener?.show(mode = KeyboardAppViewMode.FullView)
                    }
                }

                appInput.apply {
                    clearInput(this)

                    setOnFocusChangeListener(::onInputFocusChanged)
                    doOnTextChanged { text, _, _, _ -> onInputTextChanged(text) }
                    setOnEditorActionListener { v, actionId, _ ->
                        (actionId == EditorInfo.IME_ACTION_SEARCH).also {
                            if (it) performSearch(v.text.toString())
                        }
                    }

                    accessibilityDelegate = object : View.AccessibilityDelegate() {
                        override fun sendAccessibilityEvent(host: View, eventType: Int) {
                            when (eventType) {
                                AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                                    listener?.onSelectionChanged(
                                        appInput.selectionStart,
                                        appInput.selectionEnd
                                    )
                                }
                            }
                        }
                    }
                }

                appItems.apply {
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            loadMoreIfNeeded(3)
                        }
                    })
                }

                appInputHint.text = context.getString(configuration.searchAppHint, appName)
                appInputContainer.setOnClickListener { frameView?.let { appInput.requestFocus() } }

                appAutocomplete.setListener(autocompleteListener)

                inputState = state
                onHideGesture = { listener?.hide() }
            }


            onThemeChanged(theme)
            if (permissionsGranted()) {
                onAppStart()
            } else {
                requestPermissions(this)
            }

        }

    private fun openFullView(context: Context, theme: AppTheme, state: AppInputState): View =
        createFullView(context).apply {
            fullView = this
            nextLoader = null

            fullViewBinding.apply {
                fullViewAppIcon.apply {
                    setImageDrawable(appIcon(context))
                    setOnClickListener { listener?.hide() }
                }

                if (!Fresco.hasBeenInitialized()) {
                    Fresco.initialize(context)
                }

                updateLoader(contentLoading = true, categoriesLoading = true, itemLoading = false)

                fullViewAppInputContainer.apply {
                    setOnClickListener {
                        listener?.show(mode = KeyboardAppViewMode.FrameView(topBarMode))
                    }
                }

                fullViewAppItems.apply {
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            loadMoreIfNeeded(3)
                        }
                    })
                }

                fullViewAppInputHint.text =
                    StringBuilder()
                        .append(context.getString(configuration.searchHint))
                        .append(context.getString(configuration.searchAppHint, appName))
                        .toString()

                fullViewAppClose.apply {
                    setOnClickListener {
                        listener?.hide()
                    }
                }
            }

            inputState = state

            onThemeChanged(theme)
            if (permissionsGranted()) {
                onAppStart()
            } else {
                requestPermissions(this)
            }
        }

    open fun permissionsGranted() = true

    open fun requestPermissions(baseAppView: BaseAppView) {}

    fun onPermissionsGranted() {
        onAppStart()
    }

    fun onPermissionsRejected() {
        listener?.hide()
    }

    override fun onConfigurationChanged(configuration: AppConfiguration) {
        this.appConfiguration = configuration
    }

    override fun onThemeChanged(theme: AppTheme) {
        this.theme = theme

        applyTheme()

        resultAdapter?.onThemeChanged(theme)
        categoryAdapter?.onThemeChanged(theme)
    }

    private fun applyTheme() {
        frameView?.apply {
            setBackgroundColor(theme.background)
            frameViewBinding.apply {
                appInput.setTextColor(theme.foreground)

                hintColor(theme.foreground).also {
                    appInput.setHintTextColor(it)
                    appInputHint.setTextColor(it)
                }

                ImageViewCompat.setImageTintList(
                    appSearchClose,
                    ColorStateList.valueOf(theme.foreground)
                )

                contentLoader.indeterminateDrawable.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        theme.accent,
                        BlendModeCompat.SRC_IN
                    )

                if (colorizeIcon) appIcon.setColorFilter(theme.foreground, PorterDuff.Mode.SRC_IN)

                val primaryButtonBackground = GradientDrawable().apply {
                    setColor(theme.foreground)
                    cornerRadius = 100.0f
                }
                val secondaryButtonBackground = GradientDrawable().apply {
                    setStroke(2, theme.foreground)
                    alpha = 70
                    cornerRadius = 100.0f
                }

                frameEmptyErrorBinding.apply {
                    root.setBackgroundColor(theme.background)
                    errorEmptyLabel.typeface = customTypefaces?.bold
                    errorEmptyLabel.apply {
                        setTextColor(theme.foreground)
                        typeface = customTypefaces?.bold
                    }
                }
                frameConnectionErrorBinding.apply {
                    root.setBackgroundColor(theme.background)
                    errorConnectionRetryButton.apply {
                        setTextColor(theme.background)
                        background = primaryButtonBackground
                    }
                    errorConnectionCancelButton.apply {
                        setTextColor(theme.foreground)
                        background = secondaryButtonBackground
                    }
                    errorConnectionLabel.apply {
                        setTextColor(theme.foreground)
                        typeface = customTypefaces?.bold
                    }
                }

                frameGeneralErrorBinding.apply {
                    root.setBackgroundColor(theme.background)
                    errorGeneralRetryButton.apply {
                        setTextColor(theme.background)
                        background = primaryButtonBackground
                    }
                    errorGeneralCancelButton.apply {
                        setTextColor(theme.foreground)
                        background = secondaryButtonBackground
                    }
                    errorGeneralLabel.apply {
                        setTextColor(theme.foreground)
                        typeface = customTypefaces?.bold
                    }
                }
            }
        }

        fullView?.apply {
            setBackgroundColor(theme.background)

            fullViewBinding.apply {
                fullViewAppInput.setTextColor(theme.foreground)

                hintColor(theme.foreground).also {
                    fullViewAppInput.setHintTextColor(it)
                    fullViewAppInputHint.setTextColor(it)
                }

                fullViewContentLoader.indeterminateDrawable.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        theme.accent,
                        BlendModeCompat.SRC_IN
                    )

                val primaryButtonBackground = GradientDrawable().apply {
                    setColor(theme.foreground)
                    cornerRadius = 100.0f
                }
                val secondaryButtonBackground = GradientDrawable().apply {
                    setStroke(2, theme.foreground)
                    alpha = 70
                    cornerRadius = 100.0f
                }

                if (colorizeIcon) fullViewAppIcon.setColorFilter(
                    theme.foreground,
                    PorterDuff.Mode.SRC_IN
                )

                fullEmptyErrorBinding.apply {
                    root.setBackgroundColor(theme.background)
                    errorEmptyLabel.typeface = customTypefaces?.bold
                    errorEmptyLabel.apply {
                        setTextColor(theme.foreground)
                        typeface = customTypefaces?.bold
                    }
                }

                fullConnectionErrorBinding.apply {
                    root.setBackgroundColor(theme.background)
                    errorConnectionRetryButton.apply {
                        setTextColor(theme.background)
                        background = primaryButtonBackground
                    }
                    errorConnectionCancelButton.apply {
                        setTextColor(theme.foreground)
                        background = secondaryButtonBackground
                    }
                    errorConnectionLabel.apply {
                        setTextColor(theme.foreground)
                        typeface = customTypefaces?.bold
                    }
                }

                fullGeneralErrorBinding.apply {
                    root.setBackgroundColor(theme.background)
                    errorGeneralRetryButton.apply {
                        setTextColor(theme.background)
                        background = primaryButtonBackground
                    }
                    errorGeneralCancelButton.apply {
                        setTextColor(theme.foreground)
                        background = secondaryButtonBackground
                    }
                    errorGeneralLabel.apply {
                        setTextColor(theme.foreground)
                        typeface = customTypefaces?.bold
                    }
                }
            }
        }
    }

    private fun hintColor(@ColorInt color: Int) =
        Color.argb(configuration.hintAlpha, color.red, color.green, color.blue)

    private fun loadMoreIfNeeded(pages: Int) {
        val recyclerView = if (fullView != null) {
            fullViewBinding.fullViewAppItems
        } else {
            frameViewBinding.appItems
        }
        if (!isContentLoading) {
            val offset = recyclerView.computeHorizontalScrollOffset()
            val extent = recyclerView.computeHorizontalScrollExtent()
            val range = recyclerView.computeHorizontalScrollRange()

            if (offset + extent * pages > range) {
                loadNextPage()
            }
        }
    }

    override fun onInputStateChanged(state: AppInputState) {
        inputState = state
    }

    override fun close() {
        clear()
    }

    override fun dispose() {
        clear()
        listener = null
    }

    private fun loadNextPage() {
        nextLoader?.also { loader ->
            isContentLoading = true
            pagination.offset += pagination.limit
            pagination.page += 1
            performAppend(loader.invoke(pagination))
        }
    }

    private fun onAppStart() {
        performDefault()
        loadCategories()
    }

    private var resultAdapter: BaseResultAdapter? = null
    private var categoryAdapter: BaseCategoryAdapter? = null

    private fun loadCategories() {
        updateLoader(categoriesLoading = true)

        categoriesSubscription.dispose()
        categoriesSubscription = categories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onCategoriesLoaded, ::onCategoriesError)
    }

    private fun onCategoriesError(err: Throwable?) {
        Log.e("Fleksy", "Error processing categories", err)
        updateLoader(categoriesLoading = false)
    }


    private fun onCategoriesLoaded(items: List<BaseCategory>) {
        categoryAdapter = BaseCategoryAdapter().apply {
            addAll(items)
            clickSubject.subscribe {
                categoryAdapter?.onItemSelected(it)
                if ((it.value ?: it.label) == defaultCategory) {
                    performDefault()
                } else {
                    performCategory(it)
                }
            }
        }
        if (fullView != null) {
            fullViewBinding.fullViewAppCategories.also {
                it.adapter = categoryAdapter
                it.layoutManager = LinearLayoutManager(frameView?.context, HORIZONTAL, false)
            }
        } else {
            frameViewBinding.appCategories.also {
                it.adapter = categoryAdapter
                it.layoutManager = LinearLayoutManager(frameView?.context, HORIZONTAL, false)
            }
        }
        updateLoader(categoriesLoading = false)
        selectCategory(defaultCategory)
    }

    private fun performDefault() {
        selectCategory(defaultCategory)
        nextLoader = { newPagination -> default(newPagination) }
        pagination = Pagination(limit = configuration.requestLimit)
        perform(default(pagination))
    }

    private fun selectCategory(defaultCategory: String) {
        categoryAdapter?.onItemSelected(defaultCategory)
    }

    private fun performCategory(category: BaseCategory) {
        nextLoader = { newPagination -> category(category, newPagination) }
        pagination = Pagination(limit = configuration.requestLimit)
        perform(category(category, pagination))
    }

    private fun performSearch(query: String) {
        frameView?.let {
            frameViewBinding.appAutocomplete.removeAutocompletes()
        }
        categoryAdapter?.clearSelected()
        nextLoader = { newPagination -> query(query, newPagination) }
        pagination = Pagination(limit = configuration.requestLimit)
        perform(query(query, pagination))
    }

    private fun perform(request: Single<List<BaseResult>>) {
        resultAdapter = BaseResultAdapter().apply {
            clickSubject.subscribe { onItemSelected(it) }
        }

        if (fullView != null) {
            fullViewBinding.fullViewAppItems.apply {
                adapter = resultAdapter
                layoutManager = buildHorizontalLayoutManager()
            }
        } else {
            frameViewBinding.appItems.apply {
                adapter = resultAdapter
                layoutManager = buildHorizontalLayoutManager()
            }
        }

        contentSubscription.dispose()
        updateLoader(contentLoading = true)
        performAppend(request)
    }

    open fun onItemSelected(result: BaseResult) {
        resultToAppMedia(result = result)?.also { media ->
            updateLoader(itemLoading = true)
            listener?.sendMedia(
                media = media,
                fallbackToUrl = true,
                ignoreContentType = false
            ) { success ->
                updateLoader(itemLoading = false)
                showResultAnimation(success)
            }
        }
    }

    fun showResultAnimation(success: Boolean) {
        frameView?.let {
            frameViewBinding.appShareCheck.also { view ->
                view.setImageResource(
                    if (success) R.drawable.ic_success
                    else R.drawable.ic_fail
                )

                view.visibility = View.VISIBLE
                view.startAnimation(AnimationUtils
                    .loadAnimation(context, R.anim.check_animation).apply {
                        setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationEnd(p0: Animation?) {
                                view.visibility = View.GONE
                            }

                            override fun onAnimationRepeat(p0: Animation?) {}
                            override fun onAnimationStart(p0: Animation?) {}
                        })
                    })
            }
        }
    }

    open fun getInstallationId() = frameView?.context?.getInstallationUniqueId() ?: String.empty()

    open val autocompleteEnabled = false

    open fun autocomplete(currentInput: String): Single<List<BaseAutocomplete>> =
        Single.just(listOf())

    private fun loadAutocomplete(currentInput: String) {
        autocompleteSubscription.dispose()
        autocompleteSubscription = autocomplete(currentInput)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onAutocompleteLoaded, ::onAutocompleteError)
    }

    private fun onAutocompleteLoaded(autocompletes: List<BaseAutocomplete>) {
        frameView?.let {
            frameViewBinding.appAutocomplete.onReceiveAutocompletes(theme, autocompletes)
        }
    }

    private fun onAutocompleteError(err: Throwable?) {
        Log.e("Fleksy", "Error processing autocompletes", err)
    }

    private val autocompleteListener = object : AutocompletesView.AutocompleteListener {
        override fun onClickAutocomplete(autocomplete: BaseAutocomplete) {
            performSearch(autocomplete.value)
        }
    }

    private fun resultToAppMedia(result: BaseResult) = when (result) {
        is BaseResult.Image -> {
            result.preferredImageFor(inputState.contentTypes)?.let {
                AppMedia(
                    media = AppMediaSource.RemoteUrl(
                        url = it.url,
                        contentType = it.contentType
                    ),
                    label = result.label,
                    url = result.link,
                    sourceQuery = result.sourceQuery
                )
            }
        }

        is BaseResult.Video -> {
            result.video.firstOrNull()?.let {
                AppMedia(
                    media = AppMediaSource.RemoteUrl(
                        url = it.url,
                        contentType = it.contentType
                    ),
                    label = result.label,
                    url = result.link,
                    sourceQuery = result.sourceQuery
                )
            }
        }

        is BaseResult.Card -> {
            AppMedia(
                media = AppMediaSource.RemoteUrl(result.url),
                url = result.url,
                sourceQuery = result.sourceQuery,
            )
        }
    }

    private fun buildHorizontalLayoutManager(): RecyclerView.LayoutManager =
        when (val listMode = configuration.listMode) {
            is ListMode.VariableSize ->
                StaggeredGridLayoutManager(listMode.rows, HORIZONTAL)

            is ListMode.FixedSize ->
                LinearLayoutManager(frameView?.context, HORIZONTAL, false)
        }


    private fun performAppend(request: Single<List<BaseResult>>) {
        contentSubscription = request
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onResults) { onResultError(it, request) }
    }

    private fun onResultError(throwable: Throwable, request: Single<List<BaseResult>>) {
        Log.e("Fleksy", "Error processing request: ${throwable.message}", throwable)
        updateLoader(contentLoading = false)
        when (throwable) {
            is IOException -> showConnectionError(request)
            else -> showGeneralError(request)
        }
    }

    private fun showEmptyResultError() {
        if (fullView != null) {
            fullEmptyErrorBinding.root.visibility = View.VISIBLE
        } else {
            frameEmptyErrorBinding.root.visibility = View.VISIBLE
        }
    }

    private fun showGeneralError(request: Single<List<BaseResult>>) {
        if (fullView != null) {
            fullGeneralErrorBinding.apply {
                root.visibility = View.VISIBLE
                errorGeneralCancelButton.setOnClickListener { listener?.hide() }
                errorGeneralRetryButton.setOnClickListener {
                    root.visibility = View.GONE
                    performAppend(request)
                }
            }
        } else {
            frameGeneralErrorBinding.apply {
                root.visibility = View.VISIBLE
                errorGeneralCancelButton.setOnClickListener { listener?.hide() }
                errorGeneralRetryButton.setOnClickListener {
                    root.visibility = View.GONE
                    performAppend(request)
                }
            }
        }

    }

    private fun showConnectionError(request: Single<List<BaseResult>>) {
        if (fullView != null) {
            fullConnectionErrorBinding.apply {
                root.visibility = View.VISIBLE
                errorConnectionCancelButton.setOnClickListener { listener?.hide() }
                errorConnectionRetryButton.setOnClickListener {
                    root.visibility = View.GONE
                    performAppend(request)
                }
            }
        } else {
            frameConnectionErrorBinding.apply {
                root.visibility = View.VISIBLE
                errorConnectionCancelButton.setOnClickListener { listener?.hide() }
                errorConnectionRetryButton.setOnClickListener {
                    root.visibility = View.GONE
                    performAppend(request)
                }
            }
        }
    }

    private fun onResults(results: List<BaseResult>) {
        updateLoader(contentLoading = false)

        if (results.isEmpty()) {
            if (pagination.page == 0) showEmptyResultError()
            nextLoader = null
        } else {
            if (fullView != null) {
                fullEmptyErrorBinding.root.visibility = View.GONE
            } else {
                frameEmptyErrorBinding.root.visibility = View.GONE
            }
            resultAdapter?.addAll(results)
            loadMoreIfNeeded(2)
        }
    }

    private fun updateLoader(
        contentLoading: Boolean = isContentLoading,
        categoriesLoading: Boolean = isCategoriesLoading,
        itemLoading: Boolean = isItemLoading
    ) {
        isContentLoading = contentLoading
        isCategoriesLoading = categoriesLoading
        isItemLoading = itemLoading

        if (contentLoading || categoriesLoading || itemLoading) {
            frameView?.let {
                frameViewBinding.contentLoader.show()
            }
            fullView?.let {
                fullViewBinding.fullViewContentLoader.show()
            }
        } else {
            frameView?.let {
                frameViewBinding.contentLoader.hide()
            }
            fullView?.let {
                fullViewBinding.fullViewContentLoader.hide()
            }
        }
    }

    private fun createFrameView(context: Context): BaseAppView {
        frameViewBinding = LayoutBaseFrameBinding.inflate(LayoutInflater.from(context))

        frameConnectionErrorBinding =
            LayoutConnectionErrorBinding.bind(frameViewBinding.connectionErrorLayout.root)
        frameEmptyErrorBinding =
            LayoutEmptyErrorBinding.bind(frameViewBinding.emptyErrorLayout.root)
        frameGeneralErrorBinding =
            LayoutGeneralErrorBinding.bind(frameViewBinding.generalErrorLayout.root)

        return frameViewBinding.root.also {
            frameView = it
        }
    }

    private fun createFullView(context: Context): BaseAppView {
        fullViewBinding = LayoutFullViewBinding.inflate(LayoutInflater.from(context))

        fullConnectionErrorBinding =
            LayoutConnectionErrorBinding.bind(fullViewBinding.fullViewConnectionErrorLayout.root)
        fullEmptyErrorBinding =
            LayoutEmptyErrorBinding.bind(fullViewBinding.fullViewEmptyErrorLayout.root)
        fullGeneralErrorBinding =
            LayoutGeneralErrorBinding.bind(fullViewBinding.fullViewGeneralErrorLayout.root)

        return fullViewBinding.root.also {
            fullView = it
        }
    }

    private fun onSearchCloseClicked() {
        if (frameViewBinding.appInput.text.isEmpty()) {
            listener?.hide()
        } else {
            clearAppInput()
        }
    }

    private fun onInputFocusChanged(editor: View, hasFocus: Boolean) {
        listener?.apply {
            if (hasFocus) {
                val editorInfo = buildSearchEditorInfo()
                attachInputConnection(editor.onCreateInputConnection(editorInfo), editorInfo)
            } else {
                releaseInputConnection()
            }
        }
    }

    private fun buildSearchEditorInfo() =
        EditorInfo().apply {
            actionId = EditorInfo.IME_ACTION_SEARCH
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            packageName = frameView?.context?.packageName
        }

    private fun onInputTextChanged(text: CharSequence?) {
        extractText()

        if (text.isNullOrBlank()) {
            // todo: change x to close
            frameView?.let {
                frameViewBinding.appInput.setHint(configuration.searchHint)
            }
            performDefault()
        } else {
            // todo: change close to x
            frameView?.let {
                frameViewBinding.appInput.hint = null
            }
            if (autocompleteEnabled) loadAutocomplete(text.toString())
        }
    }

    private fun extractText() {
        val extractedText =
            ExtractedText().also { extractedText ->
                frameView?.let {
                    frameViewBinding.appInput.extractText(ExtractedTextRequest(), extractedText)
                }
            }
        listener?.onTextExtracted(extractedText)
    }

    private fun clearAppInput() {
        frameView?.let {
            frameViewBinding.appInput.also { clearInput(it) }
        }
        performDefault()
    }

    private fun clearInput(view: EditText) {
        view.setText("")
        view.setHint(configuration.searchHint)
        frameView?.let {
            if (frameViewBinding.appInput.hasFocus()) {
                onInputFocusChanged(view, true)
            }
        }

    }

    private fun clear() {
        contentSubscription.dispose()
        categoriesSubscription.dispose()
        autocompleteSubscription.dispose()
        frameView = null
        nextLoader = null
        appConfiguration = null
    }
}
