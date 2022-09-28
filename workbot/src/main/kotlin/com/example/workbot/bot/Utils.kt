package com.example.workbot.bot

import org.telegram.telegrambots.meta.api.methods.CopyMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

//import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup


//fun makeReplyMarkup(allButtons: List<List<String>>) = KeyboardReplyMarkup.createSimpleKeyboard(
//    keyboard = allButtons,
//    resizeKeyboard = true
//)
//
//fun myButtonsReplyMarkup(allButtons: List<Button>) = KeyboardReplyMarkup.createSimpleKeyboard(
//    keyboard = listOf(allButtons.map { it.title }),
//    resizeKeyboard = true,
//    oneTimeKeyboard = true
//)



fun makeReplyMarkup(buttons: List<List<String>>) =
    ReplyKeyboardMarkup().apply {
        keyboard = buttons.map {
            val row = KeyboardRow()
            it.forEach { buttonText ->
                row.add(
                    KeyboardButton().apply {
                        text = buttonText
                    }
                )
            }
            row
        }
        resizeKeyboard = true
    }

fun makeInlineMurkup(buttons: List<Button>) = InlineKeyboardMarkup().apply {
    keyboard = listOf(buttons.map { button ->
        InlineKeyboardButton().apply {
            text = button.title
            callbackData = button.name
        }
    })
}

fun WorkBot.sendMessage(messageText: String, messageChatId: String, markup: ReplyKeyboard? = null) {
    val message = SendMessage().apply {
        text = messageText
        chatId = messageChatId
        markup?.let {
            replyMarkup = markup
        }
    }
    execute(message)
}

fun WorkBot.removeReplyMarkup(update: Update){
    val action = EditMessageReplyMarkup().apply {
        chatId = update.chatId()
        messageId = update.callbackQuery.message.messageId
    }
    execute(action)
}

fun WorkBot.copyMessage(
    userMessageId: Int,
    messageChatId: String,
    fromMessageChatId: String,
    markup: ReplyKeyboard? = null
) {
    val message = CopyMessage().apply {
        messageId = userMessageId
        chatId = messageChatId
        fromChatId = fromMessageChatId
        replyMarkup = markup
    }
    execute(message)
}

fun Update.userId() =
    if (this.message == null) this.callbackQuery.from.id
    else this.message.from.id

fun Update.chatId() =
    if (this.message == null) this.callbackQuery.message.chatId.toString()
    else this.message.chatId.toString()
