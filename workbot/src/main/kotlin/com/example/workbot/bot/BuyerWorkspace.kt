package com.example.workbot.bot

import com.example.workbot.domain.model.Worker
import org.telegram.telegrambots.meta.api.objects.Update


fun WorkBot.setupBuyerWorkspace(update: Update, worker: Worker) {
    val buyerWorkState = State.values().find { it.name == worker.state }!!

    val curChatId = update.chatId()
    when (buyerWorkState) {
        State.Home -> {
            sendMessage("Hello buyer!", curChatId, getBuyerButtonsMarkup())
            updateEmployeeState(update.chatId(), State.WaitCommand.name)
        }
        State.WaitCommand -> {
            when (update.message.text) {
                Button.MakeOrder.title -> {
                    val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
                    sendMessage("Please, write your order below", curChatId, replyKeyboardMarkup)
                    updateEmployeeState(update.chatId(), State.WaitingBuyerOrderText.name)
                }
                else -> sendMessage("I don understand you, please choose some command", curChatId)
            }
        }
        State.WaitingBuyerOrderText -> {
            if (workerWantExit(update)) return

            val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
            val inlineMarkup = makeInlineMurkup(listOf(Button.ConfirmOrder, Button.Again))
            sendMessage("Твой заказ:", curChatId, replyKeyboardMarkup)
            copyMessage(update.message.messageId, curChatId, curChatId, inlineMarkup)
            updateEmployeeState(update.chatId(), State.WaitConfirmBuyerOrder.name)
        }
        State.WaitConfirmBuyerOrder -> {

            when(update.callbackQuery?.data){
                Button.ConfirmOrder.name -> {
                    workerService.makeBuyerOrder(worker, update.callbackQuery.message.messageId)
                    sendMessage("Заказ успешно сделан", curChatId)

                    sendMessage("Что будем делать дальше?", curChatId, getBuyerButtonsMarkup())
                    removeReplyMarkup(update)
                    updateEmployeeState(update.chatId(), State.WaitCommand.name)
                }
                Button.Again.name -> {
                    sendMessage("Make order again!", curChatId)
                    removeReplyMarkup(update)
                    updateEmployeeState(update.chatId(), State.WaitingBuyerOrderText.name)
                }
            }
            when (update.message?.text) {
                Button.Exit.title -> workerWantExit(update)
            }
        }
    }
}


private fun getBuyerButtonsMarkup() = makeReplyMarkup(listOf(Employee.Buyer.buttons.map { it.title }))
private fun getBuyerInlineMarkup() = makeInlineMurkup(Employee.Buyer.buttons)

private fun WorkBot.workerWantExit(update: Update) =
    if (update.message.text == Button.Exit.title) {
        updateEmployeeState(update.chatId(), State.WaitCommand.name)
        sendMessage("Choose what you want to do", update.chatId(), getBuyerButtonsMarkup())
        true
    } else false




