package com.bnyro.clock.presentation.screens.permission

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.presentation.screens.permission.components.PermissionRequestPage
import com.bnyro.clock.ui.MainActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PermissionScreen(onClose: () -> Unit) {
    val permissionModel: PermissionModel = viewModel()
    val pagerState = rememberPagerState() { permissionModel.requiredPermissions.size }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    HorizontalPager(
        state = pagerState,
    ) { page ->
        with(permissionModel.requiredPermissions[page]) {
            PermissionRequestPage(
                title = stringResource(id = titleRes),
                subtitle = stringResource(id = descriptionRes),
                onClickConfirm = {
                    requestPermission(context as MainActivity)
                    if (page + 1 < permissionModel.requiredPermissions.size) {
                        scope.launch {
                            pagerState.animateScrollToPage(page + 1)
                        }
                    } else {
                        onClose()
                    }
                },
                onClickCancel = {
                    if (page + 1 < permissionModel.requiredPermissions.size) {
                        scope.launch {
                            pagerState.animateScrollToPage(page + 1)
                        }
                    } else {
                        onClose()
                    }
                },
                icon = iconRes
            )
        }
    }
}