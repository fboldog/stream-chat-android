package com.getstream.sdk.chat.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.Chat.Companion.getInstance
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

class AttachmentViewHolder(resId: Int, parent: ViewGroup) :
    BaseAttachmentViewHolder(resId, parent) {

    private val cl_attachment_media: ConstraintLayout =
        itemView.findViewById(R.id.cl_attachment_media)
    private val cl_des: ConstraintLayout =
        itemView.findViewById(R.id.cl_des)
    private val iv_media_thumb: PorterShapeImageView =
        itemView.findViewById(R.id.iv_media_thumb)
    private val lv_attachment_file: ListView =
        itemView.findViewById(R.id.lv_attachment_file)
    private val tv_media_title: TextView =
        itemView.findViewById(R.id.tv_media_title)
    private val tv_media_play: TextView =
        itemView.findViewById(R.id.tv_media_play)
    private val tv_media_des: TextView =
        itemView.findViewById(R.id.tv_media_des)

    private lateinit var context: Context
    private lateinit var messageListItem: MessageItem
    private lateinit var message: Message
    private lateinit var style: MessageListViewStyle
    private lateinit var bubbleHelper: BubbleHelper

    private var clickListener: AttachmentClickListener? = null
    private var longClickListener: MessageLongClickListener? = null

    private lateinit var attachment: Attachment

    override fun bind(
        context: Context,
        messageListItem: MessageItem,
        message: Message,
        attachmentListItem: AttachmentListItem,
        style: MessageListViewStyle,
        bubbleHelper: BubbleHelper,
        clickListener: AttachmentClickListener?,
        longClickListener: MessageLongClickListener?
    ) {
        this.context = context
        this.messageListItem = messageListItem
        this.message = message
        this.style = style
        this.bubbleHelper = bubbleHelper

        this.clickListener = clickListener
        this.longClickListener = longClickListener

        attachment = attachmentListItem.attachment

        applyStyle()
        configAttachment()
    }

    private fun applyStyle() {
        if (messageListItem.isMine) {
            style.attachmentTitleTextMine.apply(tv_media_title)
            style.attachmentDescriptionTextMine.apply(tv_media_des)
        } else {
            style.attachmentTitleTextTheirs.apply(tv_media_title)
            style.attachmentDescriptionTextTheirs.apply(tv_media_des)
        }
    }

    private fun configAttachment() {
        var hasFile = false
        var hasMedia = false
        for (attachment in message.attachments) {
            if (attachment.type != null) {
                if (attachment.type == ModelType.attach_file) {
                    hasFile = true
                } else {
                    hasMedia = true
                }
            }
        }

        if (hasMedia) {
            cl_attachment_media.isVisible = true
            configMediaAttach()
        } else {
            cl_attachment_media.isVisible = false
        }

        if (hasFile) {
            lv_attachment_file.isVisible = true
            configFileAttach()
        } else {
            lv_attachment_file.isVisible = false
        }

        if (!hasMedia && !hasFile) {
            iv_media_thumb.isVisible = false

            if (!attachment.title.isNullOrEmpty()) {
                cl_attachment_media.isVisible = true
                tv_media_title.isVisible = true
                tv_media_title.text = attachment.title
            } else {
                tv_media_title.isVisible = false
            }

            if (!attachment.text.isNullOrEmpty()) {
                cl_attachment_media.isVisible = true
                tv_media_des.isVisible = true
                tv_media_des.text = attachment.text
            } else {
                tv_media_des.isVisible = false
            }
        }
    }

    private fun configMediaAttach() {
        val firstAttachment = message.attachments.first { it.type != ModelType.attach_file }
        val type = firstAttachment.type
        var attachUrl = firstAttachment.imageUrl
        if (type != null) {
            if (ModelType.attach_image == type) {
                attachUrl = firstAttachment.imageUrl
            } else if (ModelType.attach_giphy == type) {
                attachUrl = firstAttachment.thumbUrl
            } else if (ModelType.attach_video == type) {
                attachUrl = firstAttachment.thumbUrl
            } else {
                if (attachUrl == null) attachUrl = firstAttachment.image
            }
        }

        if (attachUrl.isNullOrEmpty()) {
            cl_attachment_media.isVisible = false
            return
        }
        cl_attachment_media.isVisible = true

        configAttachViewBackground(cl_attachment_media)
        configImageThumbBackground()
        configClickListeners()

        if (!attachUrl.contains("https:")) {
            attachUrl = "https:$attachUrl"
        }
        Glide.with(context)
            .load(getInstance().urlSigner().signImageUrl(attachUrl))
            .into(iv_media_thumb)

        if (message.type != ModelType.message_ephemeral) {
            tv_media_title.text = firstAttachment.title
        }
        tv_media_des.text = firstAttachment.text

        if (firstAttachment.text.isNullOrEmpty()) {
            tv_media_des.isVisible = false
        } else {
            tv_media_des.isVisible = true
        }

        if (firstAttachment.title.isNullOrEmpty()) {
            tv_media_title.isVisible = false
        } else {
            tv_media_title.isVisible = true
        }

        if (type == ModelType.attach_video) {
            tv_media_play.isVisible = true
        } else {
            tv_media_play.isVisible = false
        }
    }

    private fun configAttachViewBackground(view: View) {
        val messageBubbleDrawableRes = style.getMessageBubbleDrawable(messageListItem.isMine)
        if (messageBubbleDrawableRes != -1) {
            view.background = context.getDrawable(messageBubbleDrawableRes)
        }
    }

    private fun configImageThumbBackground() {
        var background = bubbleHelper.getDrawableForAttachment(
            message,
            messageListItem.isMine,
            messageListItem.positions,
            attachment
        )
        iv_media_thumb.setShape(context, background)

        if (tv_media_des.isVisible || tv_media_title.isVisible) {
            background = bubbleHelper.getDrawableForAttachmentDescription(
                messageListItem.message,
                messageListItem.isMine,
                messageListItem.positions
            )
            cl_des.background = background
        }
    }

    private fun configClickListeners() {
        cl_attachment_media.setOnClickListener {
            clickListener?.onAttachmentClick(message, attachment)
        }
        cl_attachment_media.setOnLongClickListener {
            longClickListener?.onMessageLongClick(message)
            true
        }
    }

    private fun configFileAttach() {
        configAttachViewBackground(lv_attachment_file)

        val attachments = message.attachments.filter { it.type == ModelType.attach_file }
        lv_attachment_file.adapter = AttachmentListAdapter(
            context,
            LlcMigrationUtils.getMetaAttachments(attachments),
            false,
            false
        )
        lv_attachment_file.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            clickListener?.onAttachmentClick(message, attachment)
        }
        lv_attachment_file.onItemLongClickListener = OnItemLongClickListener { _, _, _, _ ->
            longClickListener?.onMessageLongClick(message)
            true
        }

        lv_attachment_file.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val fileHeight = context.resources.getDimension(R.dimen.stream_attach_file_height)
            height = fileHeight.toInt() * attachments.size
        }
    }
}
