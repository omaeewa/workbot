package com.example.workbot.bot

import com.example.workbot.domain.model.Worker
import com.example.workbot.domain.serivce.WorkerService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class WorkBot(
    val workerService: WorkerService
) : TelegramLongPollingBot() {

    override fun getBotToken() = "5644768209:AAHqhIImcVoIK9vF9t877-4TcdtYfY9hvzs"

    override fun getBotUsername() = "@some_serious_work_bot"

    override fun onUpdateReceived(update: Update) {
        val currentChatId = update.chatId()
        val worker = getWorker(currentChatId)

        if (worker == null) {
            if (tryRegisterWorker(update)) return
            val markup = makeReplyMarkup(listOf(Employee.values().map { it.title }))
            val message = SendMessage().apply {
                text = "You need to register, please choose your position below"
                chatId = currentChatId
                replyMarkup = markup
            }
            execute(message)
        } else {
            setupWorkspaces(worker, update)
        }
    }

    fun tryRegisterWorker(update: Update): Boolean {
        val position = getPositionByTitle(update.message.text)
        position?.let {
            val newWorker = Worker(
                chatId = update.chatId(),
                username = update.message.from.userName,
                position = it.name,
                state = State.Home.name
            )
            workerService.addWorker(newWorker)
            val message = SendMessage().apply {
                text = "You registered like a ${it.title}"
                chatId = update.chatId()
            }
            execute(message)

            setupWorkspaces(newWorker, update)
            return true
        }
        return false
    }

    fun setupWorkspaces(worker: Worker, update: Update) {
        val employee = getPosition(worker.position)
        when (employee) {
            Employee.Buyer -> setupBuyerWorkspace(update, worker)
            Employee.Designer -> setupDesignerWorkspace(update, worker)
            Employee.Developer -> setupDeveloperWorkspace(update, worker)
        }
    }


    private fun getPosition(positionName: String) = Employee.values().find { it.name == positionName }
    private fun getPositionByTitle(title: String) = Employee.values().find { it.title == title }

    fun updateEmployeeState(chatId: String, newState: String) {
        workerService.updateWorkerState(chatId, newState)
    }

    private fun getWorker(chatId: String): Worker? =
        workerService.getWorkers().find { it.chatId == chatId }

}

enum class Employee(val title: String, val buttons: List<Button> = emptyList()) {
    Designer("Дизайнер", buttons = listOf(Button.ShoeMyTasks)),
    Developer("Разработчик", buttons = listOf(Button.ShoeMyTasks)),
    Buyer("Баер", buttons = listOf(Button.MakeOrder))
}

enum class Button(val title: String) {
    MakeOrder("makeOrder"),
    ShoeMyTasks("Мои задачи"),
    ConfirmOrder("Все верно"),
    Again("Написать заново"),
    Exit("Выйти"),
    Start("Начать"),
}

enum class State {
    Home, WaitCommand, WaitingBuyerOrderText, WaitConfirmBuyerOrder, AssetsOrder, WaitAssetsOrderText, WaitConfirmAssetsOrder,
    Assets, WaitAssetsFile, WaitConfirmAssetsFile, ScreenSending, WaitScreens, WaitScreensConfirm, Designs, WaitDesigns, WaitDesignsConfirm
}
