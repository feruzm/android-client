package io.golos.cyber_android.ui.screens.post_edit.fragment.view.post_to_editor_loader

import android.net.Uri
import android.text.SpannableStringBuilder
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.EmbedType
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.LinkInfo
import io.golos.domain.posts_parsing_rendering.post_metadata.post_dto.*
import io.golos.posts_editor.EditorDataLoader
import io.golos.posts_editor.utilities.post.PostStubs
import io.golos.posts_editor.utilities.post.spans.PostSpansFactory
import io.golos.posts_editor.utilities.post.spans.PostSpansTextFactory
import io.golos.utils.helpers.appendText
import io.golos.utils.helpers.setSpan

/**
 * Loads data from post to the editor
 */
object PostToEditorLoader {
    fun load(editor: EditorDataLoader, post: ContentBlock) {
        // To have an opportunity to insert text before embed
        if(post.content.isEmpty() || (post.content.isNotEmpty() && post.content.first() !is ParagraphBlock)) {
            editor.insertEmptyParagraph()
        }

        post.content.forEach { block ->
            when(block) {
                is ParagraphBlock -> editor.insertParagraph(getParagraphText(block))

                is ImageBlock -> {
                    editor.insertEmbed(EmbedType.EXTERNAL_IMAGE, block.content, block.content)
                    return
                }

                is VideoBlock -> {
                    editor.insertEmbed(EmbedType.EXTERNAL_VIDEO, block.content, block.thumbnailUrl ?: Uri.parse(PostStubs.video))
                    return
                }

                is WebsiteBlock -> {
                    editor.insertEmbed(EmbedType.EXTERNAL_WEBSITE, block.content, block.thumbnailUrl ?: Uri.parse(PostStubs.website))
                    return
                }
            }
        }

        post.attachments?.content?.forEach { block ->
            when(block) {
                is ImageBlock -> {
                    editor.insertEmbed(EmbedType.EXTERNAL_IMAGE, block.content, block.content)
                    return
                }

                is VideoBlock -> {
                    editor.insertEmbed(EmbedType.EXTERNAL_VIDEO, block.content, block.thumbnailUrl ?: Uri.parse(PostStubs.video))
                    return
                }

                is WebsiteBlock -> {
                    editor.insertEmbed(EmbedType.EXTERNAL_WEBSITE, block.content, block.thumbnailUrl ?: Uri.parse(PostStubs.website))
                    return
                }
            }
        }
    }

    private fun getParagraphText(paragraph: ParagraphBlock): CharSequence {
        val builder = SpannableStringBuilder()

        paragraph.content.forEach { block ->
            when(block) {
                is TextBlock -> addText(block, builder)

                is TagBlock -> addTag(block, builder)

                is MentionBlock -> addMention(block, builder)

                is LinkBlock -> addLink(block, builder)
            }
        }

        return builder
    }

    private fun addText(block: TextBlock, builder: SpannableStringBuilder) {
        val textInterval = builder.appendText(block.content)

        block.textColor?.let { builder.setSpan(PostSpansFactory.createTextColor(it), textInterval) }
        block.style?.let { builder.setSpan(PostSpansFactory.createTextStyle(it), textInterval) }
    }

    private fun addTag(block: TagBlock, builder: SpannableStringBuilder) {
        val tagText = PostSpansTextFactory.createTagText(block.content)
        val textInterval = builder.appendText(tagText)

        builder.setSpan(PostSpansFactory.createTag(tagText), textInterval)
    }

    private fun addMention(block: MentionBlock, builder: SpannableStringBuilder) {
        val mentionText = PostSpansTextFactory.createMentionText(block.content)
        val textInterval = builder.appendText(mentionText)

        builder.setSpan(PostSpansFactory.createMention(mentionText), textInterval)
    }

    private fun addLink(block: LinkBlock, builder: SpannableStringBuilder) {
        val textInterval = builder.appendText(block.content)

        builder.setSpan(PostSpansFactory.createLink(
            LinkInfo(
                block.content,
                block.url
            )
        ), textInterval)
    }
}