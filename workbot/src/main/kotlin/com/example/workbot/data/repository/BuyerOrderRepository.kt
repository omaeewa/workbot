package com.example.workbot.data.repository

import com.example.workbot.data.model.BuyerOrderEntity
import com.example.workbot.data.model.WorkerEntity
import com.example.workbot.domain.model.BuyerOrder
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface BuyerOrderRepository : CrudRepository<BuyerOrderEntity, Long> {
//    @Query("SELECT we FROM WorkerEntity we WHERE we.chatId = :chatId")
//    fun get(@Param("chatId") chatId: String): WorkerEntity?
}