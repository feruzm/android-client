package io.golos.posts_editor.components.input

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import io.golos.domain.posts_parsing_rendering.post_metadata.TextStyle
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.LinkInfo
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.ParagraphMetadata
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.getParagraphMetadata
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.calculators.ColorSpansCalculator
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.calculators.CreateSpanOperation
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.calculators.DeleteSpanOperation
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.calculators.StyleSpansCalculator
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.custom.LinkSpan
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.custom.MentionSpan
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.custom.TagSpan
import io.golos.domain.posts_parsing_rendering.post_metadata.spans_worker.SpansWorkerImpl
import io.golos.posts_editor.EditorComponent
import io.golos.posts_editor.EditorCore
import io.golos.posts_editor.R
import io.golos.posts_editor.components.ComponentsWrapper
import io.golos.posts_editor.components.input.edit_text.CustomEditText
import io.golos.posts_editor.components.input.text_tasks.dto.TextSlice
import io.golos.posts_editor.components.input.text_tasks_runner.TextTasksFactoryImpl
import io.golos.posts_editor.components.input.text_tasks_runner.TextTasksRunner
import io.golos.posts_editor.models.*
import io.golos.posts_editor.utilities.MaterialColor
import io.golos.posts_editor.utilities.Utilities
import io.golos.posts_editor.utilities.fromHtml
import io.golos.posts_editor.utilities.post.spans.PostSpansFactory
import io.golos.posts_editor.utilities.toHtml
import org.jsoup.nodes.Element
import java.util.*
import kotlin.reflect.KClass

@Suppress("KDocUnresolvedReference")
class InputExtensions(internal var editorCore: EditorCore) : EditorComponent<ParagraphMetadata>(editorCore) {
    var normalTextSize = 16

    private var lineSpacing = -1f

    private val editor: CustomEditText
        get() = editorCore.activeView as CustomEditText

    private var lastPastedLinkRange: IntRange? = null

    private val taskRunner = TextTasksRunner(TextTasksFactoryImpl()).apply { setOnPasteListener { onPaste(it) } }

    /**
     * @param the value is true if some text is selected, otherwise it's false
     */
    private var onSelectionChangeListener: ((Boolean) -> Unit)? = null

    /**
     * @return null if getting metadata from the view is impossible
     */
    override fun getMetadata(view: View): List<ParagraphMetadata>? =
        (view as? CustomEditText)?.text
            ?.takeIf { it.isNotEmpty() }
            ?.getParagraphMetadata()

    override fun getContent(view: View): Node =
        this.getNodeInstance(view)
            .apply {
                content!!.add((view as EditText).text.toHtml())
            }

    override fun getContentAsHTML(node: Node, content: EditorContent): String = getInputHtml()

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        val text = node.content!![0]
        val view = insertNewParagraph(editorCore.childCount, text)
        applyTextSettings(node, view)
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val text: String
        val count: Int
        val tv: TextView

        when (HtmlTag.valueOf(element.tagName().toLowerCase(Locale.ROOT))) {
            HtmlTag.p, HtmlTag.div -> {
                text = element.html()
                count = editorCore.parentView.childCount
                tv = insertNewParagraph(count, text)
            }

            else -> {}
        }
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

    fun closeTextTasks() = taskRunner.close()

    private fun runTextTasksDelay(processedText: CharSequence?) = taskRunner.runDelay(processedText, editor)

    private fun onPaste(textSlices: List<TextSlice>) {
        textSlices.firstOrNull { it is TextSlice.LinkTextSlice }?.let { editorCore.linkWasPasted(Uri.parse(it.text)) }
    }

    fun setText(textView: TextView, text: CharSequence) {
        //        val toReplace = getSanitizedHtml(text)
        textView.text = text
    }

    fun lastPastedLinkWasValidated(uri: Uri) {
        setSpanToLastPastedLink(PostSpansFactory.createLink(
            LinkInfo(
                uri.toString(),
                uri
            )
        ))
    }

    /**
     * Tries to find a tag under a cursor and gets a value of it
     */
    fun tryGetTextOfTag(): String? = getSpecialSpanData(TagSpan::class) {
        (it as TagSpan).value
    }?.second

    /**
     * Tries to find a mention under a cursor and gets a value of it
     */
    fun tryGetTextOfMention(): String? = getSpecialSpanData(MentionSpan::class) {
        (it as MentionSpan).value
    }?.second

    /**
     * Tries to find a link under a cursor and gets a value of it
     */
    fun tryGetLinkInTextInfo(): LinkInfo? = getSpecialSpanData(
        LinkSpan::class) {
        (it as LinkSpan).value
    }?.second

    @Suppress("UNCHECKED_CAST")
    fun updateTextColor(color: MaterialColor, editText: TextView?) {
        try {
            val editTextLocal = (editText ?: editor) as CustomEditText

            // Process the operation only if a selection area exists
            editTextLocal.selectionArea?.let { selection ->
                val spansWorker =
                    SpansWorkerImpl(
                        editTextLocal.text
                    )

                val calculator =
                    ColorSpansCalculator(
                        spansWorker
                    )
                val spanOperations = calculator.calculate(selection, MaterialColor.toSystemColor(color, editTextLocal.context))

                spanOperations.forEach { operation ->
                    when(operation) {
                        is DeleteSpanOperation -> spansWorker.removeSpan(operation.span)

                        is CreateSpanOperation<*> -> {
                            with((operation as CreateSpanOperation<Int>).spanInfo) {
                                spansWorker.appendSpan(PostSpansFactory.createTextColor(displayValue), area)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun setLineSpacing(textView: TextView, lineHeight: Float) {
        val fontHeight = textView.paint.getFontMetricsInt(null)
        textView.setLineSpacing((Utilities.dpToPx(editorCore.context, lineHeight) - fontHeight).toFloat(), 1f)
    }

    fun setOnSelectionChangeListener(listener: ((Boolean) -> Unit)?) {
        onSelectionChangeListener = listener
    }

    private fun getNewEditTextInst(hint: String?, text: CharSequence?): CustomEditText {
        val editText = CustomEditText(ContextThemeWrapper(this.editorCore.context, R.style.WysiwygEditText))
        addEditableStyling(editText)
        editText.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).
        apply {
            setMargins(Utilities.dpToPx(editText.context,16f),0,Utilities.dpToPx(editText.context,16f),0)
        }
        if (text != null) {
            setText(editText, text)
        }
        editText.setHintTextColor(editorCore.editorTextHintColor)
        editText.setTextColor(editorCore.editorTextColor)

        @Suppress("DEPRECATION")
        editText.setBackgroundDrawable(ContextCompat.getDrawable(this.editorCore.context, R.drawable.invisible_edit_text))

        editText.setOnKeyListener { v, keyCode, event ->
            editorCore.onKey(v, keyCode, event, editText)
        }

        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editText.clearFocus()
                editText.hint = null
            } else {
                editorCore.activeView = v

                if (hint != null) {
                    editText.hint = hint
                }
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // do nothing
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun afterTextChanged(s: Editable) {
                val tag = editText.getTag(R.id.control_tag)

                if (s.isEmpty() && tag != null) {
                    editText.hint = tag.toString()
                }

                runTextTasksDelay(s)
                editorCore.editorListener?.onTextChanged(editText, s)
            }
        })

        editText.customSelectionActionModeCallback = object: android.view.ActionMode.Callback {
            override fun onActionItemClicked(mode: android.view.ActionMode?, item: MenuItem?): Boolean = false

            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                onSelectionChangeListener?.invoke(true)
                return true
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean = false

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {
                onSelectionChangeListener?.invoke(false)
            }
        }

        if (this.lineSpacing != -1f) {
            setLineSpacing(editText, this.lineSpacing)
        }
        return editText
    }

    private fun isLastText(index: Int): Boolean {
        if (index == 0)
            return false
        val view = editorCore.parentView.getChildAt(index - 1)
        val type = editorCore.getControlType(view)
        return type === EditorType.INPUT
    }

    private fun addEditableStyling(editText: TextView) {
        editText.isFocusableInTouchMode = true
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, normalTextSize.toFloat())
        editText.setTextColor( editorCore.editorTextColor)
        editText.setPadding(0, 30, 0, 30)
    }


    fun insertNewParagraph(position: Int, text: CharSequence?): TextView {
        val nextHint = if (isLastText(position)) null else editorCore.placeHolder
        if (editorCore.renderType === RenderType.EDITOR) {

            /**
             * when user press enter from first line without keyin anything, need to remove the placeholder from that line 0...
             */
            if (position == 1) {
                val view = editorCore.parentView.getChildAt(0)
                val type = editorCore.getControlType(view)
                if (type === EditorType.INPUT) {
                    val textView = view as TextView
                    if (TextUtils.isEmpty(textView.text)) {
                        textView.hint = null
                    }
                }
            }

            val view = getNewEditTextInst(nextHint, text)
            editorCore.parentView.addView(view, position)
            editorCore.activeView = view
            val handler = Handler()
            handler.postDelayed({
                setFocus(view)
            }, 0)
            editorCore.activeView = view
            return view
        } else {
            val view = getNewTextView(text)
            editorCore.parentView.addView(view)
            return view
        }
    }

    fun insertTextIntoParagraph(text: CharSequence?) {
        editor.text?.let {
            it.appendln()
            it.append(text)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateTextStyle(style: TextStyle, editText: TextView?) {
        try {
            val editTextLocal = (editText ?: editor) as CustomEditText

            // Process the operation only if a selection area exists
            editTextLocal.selectionArea?.let { selection ->
                val spansWorker =
                    SpansWorkerImpl(
                        editTextLocal.text
                    )

                val calculator =
                    StyleSpansCalculator(
                        spansWorker
                    )
                val spanOperations = calculator.calculate(selection, style)

                spanOperations.forEach { operation ->
                    when(operation) {
                        is DeleteSpanOperation -> spansWorker.removeSpan(operation.span)

                        is CreateSpanOperation<*> -> {
                            with((operation as CreateSpanOperation<TextStyle>).spanInfo) {
                                spansWorker.appendSpan(PostSpansFactory.createTextStyle(displayValue), area)
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun noTrailingWhiteLines(text: CharSequence): CharSequence {
        var editedText = text

        if (editedText.isEmpty()) {
            return editedText
        }

        while (editedText[editedText.length - 1] == '\n') {
            editedText = editedText.subSequence(0, editedText.length - 1)
        }

        return editedText
    }

    fun isEditTextEmpty(editText: EditText): Boolean = editText.text.toString().trim { it <= ' ' }.isEmpty()

    private fun setFocus(view: CustomEditText) {
        if (editorCore.isStateFresh && !editorCore.autoFocus) {
            editorCore.isStateFresh = false
            return
        }
        view.requestFocus()
        val mgr = editorCore.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        view.setSelection(view.text?.length ?: 0)
        editorCore.activeView = view
    }


    fun getEditTextPrevious(startIndex: Int): CustomEditText? {
        var customEditText: CustomEditText? = null
        for (i in 0 until startIndex) {
            val view = editorCore.parentView.getChildAt(i)
            val editorType = editorCore.getControlType(view)
            if (editorType === EditorType.EMBED || editorType === EditorType.MAP)
                continue
            if (editorType === EditorType.INPUT) {
                customEditText = view as CustomEditText
                continue
            }
        }
        return customEditText
    }

    fun setFocusToPrevious(startIndex: Int) {
        for (i in startIndex downTo 1) {
            val view = editorCore.parentView.getChildAt(i)
            val editorType = editorCore.getControlType(view)
            if (editorType === EditorType.EMBED || editorType === EditorType.MAP)
                continue
            if (editorType === EditorType.INPUT) {
                setFocus(view as CustomEditText)
                break
            }
        }
    }

    fun isInputTextAtPosition(position: Int): Boolean {
        return editorCore.getControlType(editorCore.parentView.getChildAt(position)) === EditorType.INPUT
    }

    fun getInputHtml(): String = ""

    @Suppress("UNUSED_PARAMETER")
    fun applyTextSettings(node: Node, view: TextView) {
    }

    fun setLineSpacing(lineSpacing: Float) {
        this.lineSpacing = lineSpacing
    }

    private fun getNewTextView(text: CharSequence?): TextView {
        val textView = TextView(ContextThemeWrapper(this.editorCore.context, R.style.WysiwygEditText))
        addEditableStyling(textView)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.layoutParams = params

        if (!TextUtils.isEmpty(text)) {
            val toReplace = noTrailingWhiteLines(text.toString().fromHtml())
            textView.text = toReplace
            Linkify.addLinks(textView, Linkify.ALL)
        }

        if (this.lineSpacing != -1f) {
            setLineSpacing(textView, this.lineSpacing)
        }
        return textView
    }

    /**
     * Mark last pasted span as link
     */
    private fun setSpanToLastPastedLink(span: CharacterStyle) {
        try {
            editor.text?.let { textArea ->
                lastPastedLinkRange?.let { linkRange ->
                    textArea.insert(linkRange.last, " ")

                    SpansWorkerImpl(textArea)
                        .appendSpan(span, linkRange)
                }
                lastPastedLinkRange = null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Get data of a special span under a cursor
     * @return a pair with display text and value (or null if a span is not found)
     */
    private fun <T>getSpecialSpanData(spanType: KClass<*>, getValueAction: (CharacterStyle) -> T): Pair<String, T>? {
        try {
            if(editor.selectionArea != null) {
                return null
            }

            editor.text?.let { textArea ->
                val spansWorker =
                    SpansWorkerImpl(textArea)

                spansWorker.getSpanUnderPosition(spanType, editor.cursorPosition)
                    ?.let { span ->
                        val spanInterval = spansWorker.getSpanInterval(span)

                        val displayText = textArea.subSequence(spanInterval.first, spanInterval.last).toString()
                        val value = getValueAction(span)

                        return Pair(displayText, value)
                    }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}