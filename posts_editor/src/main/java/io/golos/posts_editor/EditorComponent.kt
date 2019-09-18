package io.golos.posts_editor

import android.view.View

import io.golos.posts_editor.components.ComponentsWrapper
import io.golos.posts_editor.models.EditorContent
import io.golos.posts_editor.models.EditorType
import io.golos.posts_editor.models.Node
import org.jsoup.nodes.Element

abstract class EditorComponent(private val editorCore: EditorCore) {
    protected var componentsWrapper: ComponentsWrapper? = null

    protected fun getNodeInstance(view: View): Node {
        val node = Node()
        val type = editorCore.getControlType(view)
        node.type = type
        node.content = mutableListOf()
        return node
    }

    protected fun getNodeInstance(type: EditorType): Node {
        val node = Node()
        node.type = type
        node.content = mutableListOf()
        return node
    }

    abstract fun getContent(view: View): Node

    abstract fun getContentAsHTML(node: Node, content: EditorContent): String

    abstract fun renderEditorFromState(node: Node, content: EditorContent)

    abstract fun buildNodeFromHTML(element: Element): Node?

    abstract fun init(componentsWrapper: ComponentsWrapper)
}