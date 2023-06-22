package com.bnyro.clock.obj

sealed class NumberKeypadOperation {
    class AddNumber(val number: String) : NumberKeypadOperation()
    object Delete : NumberKeypadOperation()
    object Clear : NumberKeypadOperation()
}
