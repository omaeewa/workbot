package com.example.workbot.bot

import com.example.workbot.domain.model.Application
import com.example.workbot.domain.model.Worker
import com.example.workbot.domain.serivce.WorkerService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.math.roundToInt

@Service
class Scheduler(
    private val workerService: WorkerService
) {
    private val DEVELOPER_APPS_PLAN = 4

//        @Scheduled(initialDelay = 100, fixedDelay = 100000000)
    fun allocateApps() {
        val allWorkers = workerService.getWorkers()
        val developers = allWorkers.filter { it.position == Employee.Developer.name }
        val designers = allWorkers.filter { it.position == Employee.Designer.name }
        var buyerOrders = workerService.getBuyerOrders()
        val ordersPerDeveloper = (buyerOrders.size.toDouble() / developers.size.toDouble()).roundToInt()

        var currentDesigner: Worker? = null

        developers.forEach { worker ->
            repeat(DEVELOPER_APPS_PLAN) {
                val developerId = worker.id
                var buyerId: Long? = null
                var buyerOrder: Long? = null
                currentDesigner = designers.getNextWorker(currentDesigner)

                if (ordersPerDeveloper > it && buyerOrders.isNotEmpty()) {
                    val order = buyerOrders.first()
                    buyerId = order.buyerId
                    buyerOrder = order.messageId
                    buyerOrders = buyerOrders.drop(1)
                    workerService.removeBuyerOrder(order)
                }

                val newApplication = Application(
                    developerId = developerId,
                    buyerId = buyerId,
                    buyerOrder = buyerOrder,
                    designerId = currentDesigner!!.id
                )
                workerService.addApplication(newApplication)
            }
        }
    }

    private fun List<Worker>.getNextWorker(currentWorker: Worker?) =
        if (currentWorker == null) this.first() else this.getOrNull(this.indexOf(currentWorker) + 1) ?: this.first()


}

