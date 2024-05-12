package com.bnyro.clock.domain.model

sealed class NumberKeypadOperation {
    class AddNumber(val number: String) : NumberKeypadOperation()
    object Delete : NumberKeypadOperation()
    object Clear : NumberKeypadOperation()
}
