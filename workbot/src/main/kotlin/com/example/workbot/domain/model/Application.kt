package com.example.workbot.domain.model

data class Application(
    var id: Long = 0,
    var developerId: Long,
    var buyerId: Long? = null,
    var buyerOrder: Long? = null,
    var assetsOrder: Long? = null,
    var designerId: Long? = null,
    var assets: Long? = null,
    var screen: Long? = null,
    var design: Long? = null
)