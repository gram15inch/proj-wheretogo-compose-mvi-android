package com.wheretogo.domain.handler


interface GalleryFlowHandler{
    suspend fun handle(e: Throwable, msgEvent: GalleryFlowMsgEvent?=null)
}
