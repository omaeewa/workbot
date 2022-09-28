package com.example.workbot.bot

import com.example.workbot.domain.model.Worker
import org.telegram.telegrambots.meta.api.objects.Update

fun WorkBot.setupDesignerWorkspace(update: Update, worker: Worker) {
    val designerWorkState = State.values().find { it.name == worker.state }!!
    val curChatId = update.chatId()

    when (designerWorkState) {
        State.Home -> {
            sendMessage("Hello designer!", curChatId, getDesignerButtonsMarkup())
            updateEmployeeState(update.chatId(), State.WaitCommand.name)
        }
        State.WaitCommand -> {
            when (update.message.text) {
                Button.ShoeMyTasks.title -> {
                    val (newState, taskTitle) = workerService.receiveDesignerTask(curChatId)
                    println("designerTask -> $taskTitle")
                    val replyKeyboardMarkup = if (newState == State.WaitCommand)
                        getDesignerButtonsMarkup()
                    else
                        makeReplyMarkup(listOf(listOf(Button.Start.title, Button.Exit.title)))

                    sendMessage(taskTitle, curChatId, replyKeyboardMarkup)
                    updateEmployeeState(update.chatId(), newState.name)
                }
                else -> sendMessage("I don understand you, please choose some command", curChatId)
            }
        }
        State.Assets -> {
            if (workerWantExit(update)) return
            when (update.message.text) {
                Button.Start.title -> {
                    val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
                    val app = workerService.getAppsWithoutAssets(curChatId).first()
                    app.buyerOrder?.let {
                        val buyerOrderMessage = workerService.getMessage(app.buyerOrder!!)
                        sendMessage("Заказ баера:", curChatId, replyKeyboardMarkup)
                        copyMessage(buyerOrderMessage.messageId, curChatId, buyerOrderMessage.fromChatId)
                    }

                    val assetOrderMessage = workerService.getMessage(app.assetsOrder!!)
                    sendMessage("Заказ разработчика по ассетам:", curChatId, replyKeyboardMarkup)
                    copyMessage(assetOrderMessage.messageId, curChatId, assetOrderMessage.fromChatId)

                    sendMessage("Вышли сообщение с ассетами ниже:", curChatId)

                    updateEmployeeState(curChatId, State.WaitAssetsFile.name)
                }
            }
        }
        State.WaitAssetsFile -> {
            if (workerWantExit(update)) return
            val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
            val inlineMarkup = makeInlineMurkup(listOf(Button.ConfirmOrder, Button.Again))
            sendMessage("Твои ассеты:", curChatId, replyKeyboardMarkup)
            copyMessage(update.message.messageId, curChatId, curChatId, inlineMarkup)
            updateEmployeeState(update.chatId(), State.WaitConfirmAssetsFile.name)
        }

        State.WaitConfirmAssetsFile -> {
            when (update.callbackQuery?.data) {
                Button.ConfirmOrder.name -> {
                    workerService.attachAssets(worker, update.callbackQuery.message.messageId)
                    sendMessage("Ассеты отправлены", curChatId)
                    val assetsOrdersLeft = workerService.getAppsWithoutAssets(curChatId).size

                    val text =
                        if (assetsOrdersLeft > 0) "Осталось сделать $assetsOrdersLeft ассетов. Продолжить далее?" else "Ты сделал все заказы"
                    val replyKeyboardMarkup =
                        if (assetsOrdersLeft > 0) makeReplyMarkup(
                            listOf(listOf(Button.Start.title, Button.Exit.title))
                        ) else getDesignerButtonsMarkup()

                    sendMessage(text, curChatId, replyKeyboardMarkup)
                    val newState = workerService.receiveDesignerTask(curChatId).first
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
        State.Designs -> {
            if (workerWantExit(update)) return
            when (update.message.text) {
                Button.Start.title -> {
                    val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
                    val app = workerService.getAppsWithoutDesigns(curChatId).first()

                    app.buyerOrder?.let {
                        val buyerOrderMessage = workerService.getMessage(app.buyerOrder!!)
                        sendMessage("Заказ баера:", curChatId, replyKeyboardMarkup)
                        copyMessage(buyerOrderMessage.messageId, curChatId, buyerOrderMessage.fromChatId)
                    }


                    val assetMessage = workerService.getMessage(app.assets!!)
                    sendMessage("Ассеты:", curChatId, replyKeyboardMarkup)
                    copyMessage(assetMessage.messageId, curChatId, assetMessage.fromChatId)

                    sendMessage("Вышли сообщение с дизайнами ниже:", curChatId)

                    updateEmployeeState(curChatId, State.WaitDesigns.name)
                }
            }
        }
        State.WaitDesigns -> {
            if (workerWantExit(update)) return
            val replyKeyboardMarkup = makeReplyMarkup(listOf(listOf(Button.Exit.title)))
            val inlineMarkup = makeInlineMurkup(listOf(Button.ConfirmOrder, Button.Again))
            sendMessage("Твои дизайны:", curChatId, replyKeyboardMarkup)
            copyMessage(update.message.messageId, curChatId, curChatId, inlineMarkup)
            updateEmployeeState(update.chatId(), State.WaitDesignsConfirm.name)
        }
        State.WaitDesignsConfirm -> {
            when (update.callbackQuery?.data) {
                Button.ConfirmOrder.name -> {
                    val app = workerService.getAppsWithoutDesigns(worker.chatId).first()
                    //Send to developer
                    val developer = workerService.getWorkers().find { it.id == app.developerId }!!
                    sendMessage("Тебе пришел дизайн", developer.chatId)
                    copyMessage(update.callbackQuery.message.messageId, developer.chatId, curChatId)

                    workerService.attachDesigns(worker, update.callbackQuery.message.messageId)
                    sendMessage("Дизайны отправлены", curChatId)
                    val assetsOrdersLeft = workerService.getAppsWithoutDesigns(curChatId).size

                    val text =
                        if (assetsOrdersLeft > 0) "Осталось сделать $assetsOrdersLeft дизайнов. Продолжить далее?" else "Ты сделал все заказы"
                    val replyKeyboardMarkup =
                        if (assetsOrdersLeft > 0) makeReplyMarkup(
                            listOf(listOf(Button.Start.title, Button.Exit.title))
                        ) else getDesignerButtonsMarkup()

                    sendMessage(text, curChatId, replyKeyboardMarkup)
                    val newState = workerService.receiveDesignerTask(curChatId).first
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


private fun getDesignerButtonsMarkup() = makeReplyMarkup(listOf(Employee.Designer.buttons.map { it.title }))
private fun WorkBot.workerWantExit(update: Update) =
    if (update.message.text == Button.Exit.title) {
        updateEmployeeState(update.chatId(), State.WaitCommand.name)
        sendMessage("Choose what you want to do", update.chatId(), getDesignerButtonsMarkup())
        true
    } else false