package com.dhkim139.wheretogo

import com.dhkim139.wheretogo.viewmodel.drive.BottomSheetTest
import com.dhkim139.wheretogo.viewmodel.drive.CommonTest
import com.dhkim139.wheretogo.viewmodel.drive.DriveListTest
import com.dhkim139.wheretogo.viewmodel.drive.FloatingButtonTest
import com.dhkim139.wheretogo.viewmodel.drive.PopUpTest
import com.dhkim139.wheretogo.viewmodel.drive.SearchBarTest
import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(
    value = [
        BottomSheetTest::class,
        CommonTest::class,
        DriveListTest::class,
        FloatingButtonTest::class,
        PopUpTest::class,
        SearchBarTest::class,
    ]
)
class DriveViewModelTests