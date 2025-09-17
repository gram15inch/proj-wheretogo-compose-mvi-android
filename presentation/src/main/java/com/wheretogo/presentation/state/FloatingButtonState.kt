package com.wheretogo.presentation.state

import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.model.AdItem
import java.util.EnumSet

data class FloatingButtonState(
    val adItemGroup: List<AdItem> = emptyList(),
    val stateMode: DriveFloatingVisibleMode = DriveFloatingVisibleMode.Default
) {
    companion object {
        val commentVisible: EnumSet<DriveFloatingVisibleMode> =
            EnumSet.of(DriveFloatingVisibleMode.Popup)
        val checkpointAddVisible: EnumSet<DriveFloatingVisibleMode> =
            EnumSet.of(DriveFloatingVisibleMode.Default)
        val infoVisible: EnumSet<DriveFloatingVisibleMode> =
            EnumSet.of(DriveFloatingVisibleMode.Default, DriveFloatingVisibleMode.Popup)
        val exportVisible: EnumSet<DriveFloatingVisibleMode> = EnumSet.of(
            DriveFloatingVisibleMode.Default,
            DriveFloatingVisibleMode.Popup,
            DriveFloatingVisibleMode.ExportExpand
        )
        val backPlateVisible: EnumSet<DriveFloatingVisibleMode> =
            EnumSet.of(DriveFloatingVisibleMode.ExportExpand)
        val foldVisible: EnumSet<DriveFloatingVisibleMode> = EnumSet.of(
            DriveFloatingVisibleMode.Default,
            DriveFloatingVisibleMode.Popup,
            DriveFloatingVisibleMode.ExportExpand
        )
    }
}