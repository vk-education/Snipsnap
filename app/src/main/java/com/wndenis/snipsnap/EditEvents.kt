package com.wndenis.snipsnap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.datetime.datetimepicker
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.ui.theme.*

@ExperimentalComposeUiApi
@Composable
fun EditEvents(event: CalendarEvent, dismissAction: () -> Unit) {
    val colors = listOf(P200, P75, R100, R500, T100, T200, T300, T500, Y100, Y300, Y400)
    val editedEvent by remember { mutableStateOf(event.copy()) }
    var unusedBool by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val updater = {
        unusedBool = !unusedBool
    }

    fun applyChanges() {
        event.mimic(editedEvent)
        dismissAction()
    }

    fun getSelectedColor(): Int {
        val i = colors.indexOf(editedEvent.color)
        if (i >= 0) return i
        return 0
    }


    val date1 = remember { MaterialDialog() }
    date1.build {
        datetimepicker(initialDateTime = editedEvent.startDate,
            is24HourClock = true,
            positiveButtonText = "OK",
            negativeButtonText = "Отмена",
//            datePickerTitle = "Выберите дату",
//            timePickerTitle = "Выберите время",
            onCancel = updater,
            onDateTimeChange = { dt ->
                editedEvent.startDate = dt
                updater()
            }
        )
    }

    val date2 = remember { MaterialDialog() }
    date2.build {
        datetimepicker(initialDateTime = editedEvent.endDate,
            is24HourClock = true,
            positiveButtonText = "OK",
            negativeButtonText = "Отмена",
            datePickerTitle = "Выберите дату",
            timePickerTitle = "Выберите время",
            onCancel = updater,
            onDateTimeChange = { dt ->
                editedEvent.endDate = dt
                updater()
            }
        )
    }

    val colorPicker = remember { MaterialDialog() }
    colorPicker.build {
        colorChooser(
            colors = colors,
            initialSelection = getSelectedColor()
        ) { color ->
            editedEvent.color = color
        }
        buttons {
            negativeButton("Отмена")
            positiveButton("OK", onClick = updater)
        }
//        colorPicker()
//        colorPicker(colors = ColorPalette.Primary)
    }

    Dialog(onDismissRequest = dismissAction) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            if (unusedBool) {
                Spacer(modifier = Modifier.width(0.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                //================= Header
                Row {
                    Column {
                        Text("Редактирование события")
                        Divider(thickness = 1.dp)
                    }
                }

                //================= Delete button
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.Red
                    ),
                    onClick = {
                        event.deleted = true
                        dismissAction()
                    }) {
                    Text("Удалить")
                }

                //================= Edit name
                Row {
                    Divider(thickness = 1.dp)
                }
                Row {
                    var oldName by remember { mutableStateOf(editedEvent.name) }
                    OutlinedTextField(
                        value = oldName,
                        onValueChange = {
                            var newStr = it
                            if (newStr.length > 25)
                                newStr = newStr.slice(0..25)
                            oldName = newStr
                            editedEvent.name = newStr
                        },
                        keyboardActions = KeyboardActions(
                            onAny = { hideKeyboard(context) }),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        label = { Text("Название события") })
                }

                //================= Color picker
                Row {
                    Box(
                        modifier = Modifier
//                            .fillMaxSize()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        Text(
                            "Выберите цвет",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { colorPicker.show() })
                                .background(
                                    editedEvent.color
                                )
                                .padding(10.dp)
                        )
                    }
                }
                Row {
                    Divider(thickness = 1.dp)
                }

                //================= Date picker 1
                Row {
                    Button(
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Gray
                        ),
                        onClick = {
                            date1.show()
                        }) {
                        Text("Начало: " + editedEvent.startDate.conv(), color = Color.Black)
                    }
                }

                //================= Date picker 2
                Row {
                    Button(
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Gray
                        ),
                        onClick = {
                            date2.show()
                        }) {
                        Text("Конец: " + editedEvent.endDate.conv(), color = Color.Black)
                    }
                }

                //================= Cancel/Save
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Yellow
                        ),
                        onClick = {
                            dismissAction()
                        }) {
                        Text("Отмена")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.Green
                        ),
                        onClick = {
                            applyChanges()
                        }) {
                        Text("ОК")
                    }
                }
            }
        }
    }
}