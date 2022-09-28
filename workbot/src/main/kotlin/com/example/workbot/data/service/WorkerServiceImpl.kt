package com.example.workbot.data.service

import com.example.workbot.bot.State
import com.example.workbot.data.model.toDomainModel
import com.example.workbot.data.model.toEntity
import com.example.workbot.data.repository.ApplicationRepository
import com.example.workbot.data.repository.BuyerOrderRepository
import com.example.workbot.data.repository.MessageRepository
import com.example.workbot.data.repository.WorkerRepository
import com.example.workbot.domain.model.Application
import com.example.workbot.domain.model.BuyerOrder
import com.example.workbot.domain.model.Message
import com.example.workbot.domain.model.Worker
import com.example.workbot.domain.serivce.WorkerService
import org.springframework.stereotype.Service

@Service
class WorkerServiceImpl(
    private val workerRepository: WorkerRepository,
    private val buyerOrderRepository: BuyerOrderRepository,
    private val messageRepository: MessageRepository,
    private val applicationRepository: ApplicationRepository
) : WorkerService {

    override fun getWorkers(): List<Worker> = workerRepository.findAll().toDomainModel()

    override fun addWorker(worker: Worker) {
        val existedWorker = workerRepository.get(worker.chatId)

        if (existedWorker == null)
            workerRepository.save(worker.toEntity())
    }

    override fun updateWorkerState(chatId: String, newState: String) {
        val existedWorker = workerRepository.get(chatId)?.toDomainModel()

        if (existedWorker != null)
            workerRepository.save(existedWorker.copy(state = newState).toEntity())
    }

    override fun makeBuyerOrder(worker: Worker, messageId: Int) {
        val newMessage = Message(fromChatId = worker.chatId, messageId = messageId)
        val createdMessageId = messageRepository.save(newMessage.toEntity()).id
        val newBuyerOrder = BuyerOrder(messageId = createdMessageId, buyerId = worker.id)

        buyerOrderRepository.save(newBuyerOrder.toEntity())
    }

    override fun getBuyerOrders(): List<BuyerOrder> = buyerOrderRepository.findAll().toDomainModel()
    override fun removeBuyerOrder(buyerOrder: BuyerOrder) {
        buyerOrderRepository.delete(buyerOrder.toEntity())
    }

    override fun addApplication(application: Application) {
        applicationRepository.save(application.toEntity())
    }

    override fun receiveDeveloperTask(chatId: String): Pair<State, String> {
        val appsWithoutAssetsOrder = getAppsWithoutAssetsOrder(chatId)
        if (appsWithoutAssetsOrder.isNotEmpty()) return State.AssetsOrder to "Тебе осталось сделать ${appsWithoutAssetsOrder.size} заказов ассетов. Можешь приступить прямо сейчас"

        val appsWithoutScreens = getAppsWithoutScreens(chatId)
        if (appsWithoutScreens.isNotEmpty()) return State.ScreenSending to "Тебе осталось отправить ${appsWithoutScreens.size} скринов приложений. Можешь приступить прямо сейчас"

        return State.WaitCommand to "Ты все сделал"
    }

    override fun receiveDesignerTask(chatId: String): Pair<State, String> {
        val appsWithoutAssets = getAppsWithoutAssets(chatId)
        if (appsWithoutAssets.isNotEmpty()) return State.Assets to "Тебе осталось сделать ${appsWithoutAssets.size} ассетов. Можешь приступить прямо сейчас"

        val appsWithoutDesigns = getAppsWithoutDesigns(chatId)
        if (appsWithoutDesigns.isNotEmpty()) return State.Designs to "Тебе осталось сделать ${appsWithoutDesigns.size} дизайнов. Можешь приступить прямо сейчас"

        return State.WaitCommand to "Ты все сделал"
    }


    override fun getAppsWithoutAssetsOrder(chatId: String): List<Application> {
        val developer = workerRepository.get(chatId)!!
        val apps = applicationRepository.getByDeveloper(developer.id)

        return apps.filter { it.assetsOrder == null }.toDomainModel()
    }

    override fun getAppsWithoutAssets(chatId: String): List<Application> {
        val designer = workerRepository.get(chatId)!!
        val apps = applicationRepository.getByDesigner(designer.id)

        return apps.filter { it.assetsOrder != null && it.assets == null }.toDomainModel()
    }

    override fun getAppsWithoutScreens(chatId: String): List<Application> {
        val developer = workerRepository.get(chatId)!!
        val apps = applicationRepository.getByDeveloper(developer.id)

        return apps.filter { it.assetsOrder != null && it.assets != null && it.screen == null }.toDomainModel()
    }

    override fun getAppsWithoutDesigns(chatId: String): List<Application> {
        val designer = workerRepository.get(chatId)!!
        val apps = applicationRepository.getByDesigner(designer.id)

        return apps.filter { it.assetsOrder != null && it.assets != null && it.screen != null && it.design == null }
            .toDomainModel()
    }


    override fun makeAssetsOrder(worker: Worker, messageId: Int) {
        val app = getAppsWithoutAssetsOrder(worker.chatId).first()
        val assetsOrderMessage = Message(fromChatId = worker.chatId, messageId = messageId)
        val assetsOrderMessageId = messageRepository.save(assetsOrderMessage.toEntity()).id

        val updatedApp = app.copy(assetsOrder = assetsOrderMessageId)
        applicationRepository.save(updatedApp.toEntity())
    }

    override fun attachAssets(worker: Worker, messageId: Int) {
        val app = getAppsWithoutAssets(worker.chatId).first()
        val assetsMessage = Message(fromChatId = worker.chatId, messageId = messageId)
        val assetsMessageId = messageRepository.save(assetsMessage.toEntity()).id

        val updatedApp = app.copy(assets = assetsMessageId)
        applicationRepository.save(updatedApp.toEntity())
    }

    override fun attachScreens(worker: Worker, messageId: Int) {
        val app = getAppsWithoutScreens(worker.chatId).first()
        val screensMessage = Message(fromChatId = worker.chatId, messageId = messageId)
        val screensMessageId = messageRepository.save(screensMessage.toEntity()).id

        val updatedApp = app.copy(screen = screensMessageId)
        applicationRepository.save(updatedApp.toEntity())
    }

    override fun attachDesigns(worker: Worker, messageId: Int) {
        val app = getAppsWithoutDesigns(worker.chatId).first()
        val designsMessage = Message(fromChatId = worker.chatId, messageId = messageId)
        val designsMessageId = messageRepository.save(designsMessage.toEntity()).id

        val updatedApp = app.copy(design = designsMessageId)
        applicationRepository.save(updatedApp.toEntity())
    }

    override fun getMessage(id: Long): Message =
        messageRepository.findById(id).get().toDomainModel()


}