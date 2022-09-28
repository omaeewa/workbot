package com.example.workbot.data.model

import com.example.workbot.domain.model.Application
import com.example.workbot.domain.model.Message
import javax.persistence.*

@Entity
@Table(name = "application")
class ApplicationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "developer_id")
    var developerId: Long,
    @Column(name = "buyer_id")
    var buyerId: Long? = null,
    @Column(name = "buyer_order")
    var buyerOrder: Long? = null,
    @Column(name = "assets_order")
    var assetsOrder: Long? = null,
    @Column(name = "designer_id")
    var designerId: Long? = null,
    var assets: Long? = null,
    var screen: Long? = null,
    var design: Long? = null
)

fun ApplicationEntity.toDomainModel() =
    Application(id, developerId, buyerId, buyerOrder, assetsOrder, designerId, assets, screen, design)

fun Iterable<ApplicationEntity>.toDomainModel() = this.map { it.toDomainModel() }

fun Application.toEntity() =
    ApplicationEntity(id, developerId, buyerId, buyerOrder, assetsOrder, designerId, assets, screen, design)