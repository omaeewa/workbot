package com.example.workbot.data.repository

import com.example.workbot.data.model.ApplicationEntity
import com.example.workbot.data.model.BuyerOrderEntity
import com.example.workbot.data.model.WorkerEntity
import com.example.workbot.domain.model.BuyerOrder
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface ApplicationRepository : CrudRepository<ApplicationEntity, Long> {
    @Query("SELECT we FROM ApplicationEntity we WHERE we.developerId = :developerId")
    fun getByDeveloper(@Param("developerId") developerId: Long): List<ApplicationEntity>

    @Query("SELECT we FROM ApplicationEntity we WHERE we.designerId = :designerId")
    fun getByDesigner(@Param("designerId") designerId: Long): List<ApplicationEntity>
}