package io.golos.cyber_android.ui.shared.widgets.post_comments

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.dto.ContentId
import io.golos.cyber_android.ui.shared.glide.release
import io.golos.cyber_android.ui.shared.utils.prefetchScreenSize
import io.golos.domain.use_cases.post.post_dto.EmbedBlock
import kotlinx.android.synthetic.main.view_attachment_rich.view.*

class EmbedWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    BlockWidget<EmbedBlock, EmbedWidgetListener> {

    private var onClickProcessor: EmbedWidgetListener? = null

    private var linkUri: Uri? = null

    private var contentId: ContentId? = null

    init {
        inflate(context, R.layout.view_attachment_rich, this)
    }

    fun setContentId(postContentId: ContentId) {
        contentId = postContentId
    }

    override fun setOnClickProcessor(processor: EmbedWidgetListener?) {
        this.onClickProcessor = processor
    }

    override fun render(block: EmbedBlock) {
        linkUri = block.url
        val thumbnailUrl = block.thumbnailUrl?.prefetchScreenSize(context)
        var currentThubnail: ImageView? = null
        if (thumbnailUrl != null) {
            currentThubnail = if(block.thumbnailHeight == null || block.thumbnailWidth == null){
                richImage.visibility = View.GONE
                richImageAspectRatio.visibility = View.VISIBLE
                richImageAspectRatio
            } else{
                richImageAspectRatio.visibility = View.GONE
                richImage.visibility = View.VISIBLE
                richImage
            }
            richDescription.visibility = View.GONE
            Glide.with(context)
                .load(thumbnailUrl)
                .into(currentThubnail)
        } else {
            richImage.visibility = View.GONE
            richImageAspectRatio.visibility = View.GONE
            richDescription.visibility = View.VISIBLE
            richDescription.text = block.description
        }

        val type = Type.getTypeByProviderName(block.providerName.orEmpty())
        richIcon.setImageResource(type.providerIconResId)
        richName.text = block.author
        richUrl.text = block.authorUrl?.authority

        if(onClickProcessor != null) {
            currentThubnail?.setOnClickListener {
                if (contentId != null) {
                    onClickProcessor?.onItemClicked(contentId!!)
                } else {
                    thumbnailUrl?.let { uri ->
                        onClickProcessor?.onImageClicked(uri)
                    }
                }
            }
            llLinkProvider.setOnClickListener {
                linkUri?.let {
                    this.onClickProcessor?.onLinkClicked(it)
                }
            }
        } else {
            llLinkProvider.setOnClickListener(null)
            currentThubnail?.setOnClickListener(null)
        }
    }

    override fun release() {
        richImage.release()
        richImageAspectRatio.release()
    }

    enum class Type(
        val providerName: String,
        @DrawableRes val providerIconResId: Int
    ) {
        OTHER("", R.drawable.ic_rich_other);

        companion object {
            fun getTypeByProviderName(providerName: String): Type {
                return values().find { type -> type.providerName == providerName } ?: OTHER
            }
        }
    }
}