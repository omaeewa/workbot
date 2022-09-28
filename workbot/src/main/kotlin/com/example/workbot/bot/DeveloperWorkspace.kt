package com.example.workbot.bot

import com.example.workbot.domain.model.Worker
import org.telegram.telegrambots.meta.api.objects.Update

fun WorkBot.setupDeveloperWorkspace(update: Update, worker: Worker) {
    val developerWorkState = State.values().find { it.name == worker.state }!!
    val curChatId = update.chatId()

    when (developerWorkState) {
        State.Home -> {
            sendMessage("Hello developer!", curChatId, getDeveloperButtonsMarkup())
            updateEmployeeState(update.chatId(), State.WaitCommand.name)
        }
        State.WaitCommand -> {
            when (update.message.text) {
                Button.ShoeMyTasks.title -> {
                    val (newState, taskTitle) = workerService.receiveDeveloperTask(curChatId)
                    val replyKeyboardMarkup = if (newState == State.WaitCommand)
                        getDeveloperButtonsMarkup()
                    else
                        makeReplyMarkup(listOf(listOf(Button.Start.title, Button.Exit.title)))

                    sendMessage(taskTitle, curChatId, replyKeyboardMarkup)
                    updateEmployeeState(update.chatId(), newState.name)
                }
                else -> sendMessage("I don understand you, please choose some command", curChatId)
            }
        }
        State.AssetsOrder -> {
            if (workerWantExit(update)) return

            when (update.message.text) {
                Button.Start.title -> {
                    val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
                    val app = workerService.getAppsWithoutAssetsOrder(curChatId).first()
                    val orderMessage = if (app.buyerOrder != null) workerService.getMessage(app.buyerOrder!!) else null

                    if (orderMessage == null) {
                        sendMessage("Заказа баера нет. Напиши свой заказ ассетов ниже", curChatId, replyKeyboardMarkup)
                    } else {
                        sendMessage("Заказ баера:", curChatId, replyKeyboardMarkup)
                        copyMessage(orderMessage.messageId, curChatId, orderMessage.fromChatId)
                        sendMessage("Напиши свой заказ ассетов ниже:", curChatId)
                    }
                    updateEmployeeState(curChatId, State.WaitAssetsOrderText.name)
                }
            }
        }
        State.WaitAssetsOrderText -> {
            if (workerWantExit(update)) return

            val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
            val inlineMarkup = makeInlineMurkup(listOf(Button.ConfirmOrder, Button.Again))
            sendMessage("Твой заказ ассетов:", curChatId, replyKeyboardMarkup)
            copyMessage(update.message.messageId, curChatId, curChatId, inlineMarkup)
            updateEmployeeState(update.chatId(), State.WaitConfirmAssetsOrder.name)
        }
        State.WaitConfirmAssetsOrder -> {
            when (update.callbackQuery?.data) {
                Button.ConfirmOrder.name -> {
                    workerService.makeAssetsOrder(worker, update.callbackQuery.message.messageId)

                    sendMessage("Заказ сделан", curChatId)
                    val assetsOrdersLeft = workerService.getAppsWithoutAssetsOrder(curChatId).size

                    val text =
                        if (assetsOrdersLeft > 0) "Осталось сделать $assetsOrdersLeft заказов ассетов. Продолжить далее?" else "Ты сделал все заказы"
                    val replyKeyboardMarkup =
                        if (assetsOrdersLeft > 0) makeReplyMarkup(
                            listOf(listOf(Button.Start.title, Button.Exit.title))
                        ) else getDeveloperButtonsMarkup()

                    sendMessage(text, curChatId, replyKeyboardMarkup)
                    val newState = workerService.receiveDeveloperTask(curChatId).first
                    updateEmployeeState(update.chatId(), newState.name)
                    removeReplyMarkup(update)
                }
                Button.Again.name -> {
                    sendMessage("Make order again!", curChatId)
                    removeReplyMarkup(update)
                    updateEmployeeState(update.chatId(), State.WaitAssetsOrderText.name)
                }
            }
            when (update.message?.text) {
                Button.Exit.title -> workerWantExit(update)
            }
        }
        State.ScreenSending -> {
            if (workerWantExit(update)) return

            when (update.message.text) {
                Button.Start.title -> {
                    val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
                    val app = workerService.getAppsWithoutScreens(curChatId).first()
                    app.buyerOrder?.let {
                        val buyerOrderMessage = workerService.getMessage(app.buyerOrder!!)
                        sendMessage("Заказ баера:", curChatId, replyKeyboardMarkup)
                        copyMessage(buyerOrderMessage.messageId, curChatId, buyerOrderMessage.fromChatId)
                    }

                    val assetsMessage = workerService.getMessage(app.assets!!)
                    sendMessage("Aссеты:", curChatId, replyKeyboardMarkup)
                    copyMessage(assetsMessage.messageId, curChatId, assetsMessage.fromChatId)

                    sendMessage("Вышли сообщение со скринами ниже:", curChatId)

                    updateEmployeeState(curChatId, State.WaitScreens.name)
                }
            }
        }
        State.WaitScreens -> {
            if (workerWantExit(update)) return

            val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
            val inlineMarkup = makeInlineMurkup(listOf(Button.ConfirmOrder, Button.Again))
            sendMessage("Твои скрины:", curChatId, replyKeyboardMarkup)
            copyMessage(update.message.messageId, curChatId, curChatId, inlineMarkup)
            updateEmployeeState(update.chatId(), State.WaitScreensConfirm.name)
        }
        State.WaitScreensConfirm -> {
            when (update.callbackQuery?.data) {
                Button.ConfirmOrder.name -> {
                    workerService.attachScreens(worker, update.callbackQuery.message.messageId)

                    sendMessage("Скрины отправлены", curChatId)
                    val assetsOrdersLeft = workerService.getAppsWithoutScreens(curChatId).size

                    val text =
                        if (assetsOrdersLeft > 0) "Осталось отправить $assetsOrdersLeft скринов. Продолжить далее?" else "Ты сделал все заказы"
                    val replyKeyboardMarkup =
                        if (assetsOrdersLeft > 0) makeReplyMarkup(
                            listOf(listOf(Button.Start.title, Button.Exit.title))
                        ) else getDeveloperButtonsMarkup()

                    sendMessage(text, curChatId, replyKeyboardMarkup)
                    val newState = workerService.receiveDeveloperTask(curChatId).first
                    updateEmployeeState(update.chatId(), newState.name)
                    removeReplyMarkup(update)
                }
                Button.Again.name -> {
                    sendMessage("Make order again!", curChatId)
                    removeReplyMarkup(update)
                    updateEmployeeState(update.chatId(), State.WaitAssetsOrderText.name)
                }
            }
            when (update.message?.text) {
                Button.Exit.title -> workerWantExit(update)
            }
        }
    }
}

private fun getDeveloperButtonsMarkup() = makeReplyMarkup(listOf(Employee.Developer.buttons.map { it.title }))

private fun WorkBot.workerWantExit(update: Update) =
    if (update.message.text == Button.Exit.title) {
        updateEmployeeState(update.chatId(), State.WaitCommand.name)
        sendMessage("Choose what you want to do", update.chatId(), getDeveloperButtonsMarkup())
        true
    } else false