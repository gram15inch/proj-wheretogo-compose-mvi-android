package com.wheretogo.presentation.handler

import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.domain.handler.GalleryFlowHandler
import com.wheretogo.domain.handler.GalleryFlowMsgEvent
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg

class GalleryFlowHandlerImpl(val errorHandler: ErrorHandler) : GalleryFlowHandler {
    override suspend fun handle(e: Throwable, msgEvent: GalleryFlowMsgEvent?) {
        when(msgEvent){
            GalleryFlowMsgEvent.GALLERY_LOAD_FAIL ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.gallery_load_fail)))
            GalleryFlowMsgEvent.MEDIA_SAVE_FAIL ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.media_save_fail)))
            GalleryFlowMsgEvent.PHOTO_DELETE_FAIL ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.photo_delete_fail)))
            else -> {}
        }
        errorHandler.handle(e)
    }
}