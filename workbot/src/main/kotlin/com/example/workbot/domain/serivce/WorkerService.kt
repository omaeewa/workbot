package com.example.workbot.domain.serivce

import com.example.workbot.bot.State
import com.example.workbot.domain.model.Application
import com.example.workbot.domain.model.BuyerOrder
import com.example.workbot.domain.model.Message
import com.example.workbot.domain.model.Worker

interface WorkerService {

    fun getWorkers(): List<Worker>
    fun addWorker(worker: Worker)
    fun updateWorkerState(chatId: String, newState: String)

    fun makeBuyerOrder(worker: Worker, messageId: Int)
    fun getBuyerOrders(): List<BuyerOrder>
    fun removeBuyerOrder(buyerOrder: BuyerOrder)

    fun addApplication(application: Application)

    fun receiveDeveloperTask(chatId: String): Pair<State, String>
    fun receiveDesignerTask(chatId: String): Pair<State, String>

    fun getAppsWithoutAssetsOrder(chatId: String): List<Application>
    fun getAppsWithoutAssets(chatId: String): List<Application>
    fun getAppsWithoutScreens(chatId: String): List<Application>
    fun getAppsWithoutDesigns(chatId: String): List<Application>

    fun makeAssetsOrder(worker: Worker, messageId: Int)
    fun attachAssets(worker: Worker, messageId: Int)
    fun attachScreens(worker: Worker, messageId: Int)
    fun attachDesigns(worker: Worker, messageId: Int)

    fun getMessage(id: Long): Message
}