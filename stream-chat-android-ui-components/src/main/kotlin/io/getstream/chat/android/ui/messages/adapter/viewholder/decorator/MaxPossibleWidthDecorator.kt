package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder

internal class MaxPossibleWidthDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        (viewHolder.binding.mediaAttachmentsGroupView.layoutParams as ConstraintLayout.LayoutParams).apply {
            matchConstraintPercentWidth = MAX_POSSIBLE_WIDTH_FACTOR
        }
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.messageText.post {
            viewHolder.binding.messageText.layoutParams =
                (viewHolder.binding.messageText.layoutParams as ConstraintLayout.LayoutParams).apply {
                    matchConstraintMaxWidth = maxWidth(viewHolder.binding.root)
                }
        }
    }

    private fun maxWidth(parent: ViewGroup) =
        ((parent.measuredWidth - parent.paddingLeft - parent.paddingRight) * MAX_POSSIBLE_WIDTH_FACTOR).toInt()

    companion object {
        private const val MAX_POSSIBLE_WIDTH_FACTOR = 5f / 7
    }
}
