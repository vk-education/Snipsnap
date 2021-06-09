package com.wndenis.snipsnap.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.wndenis.snipsnap.calendar.components.DayDividers
import com.wndenis.snipsnap.calendar.components.DaysRow
import com.wndenis.snipsnap.calendar.components.HoursRow
import com.wndenis.snipsnap.calendar.components.MonthDividers
import com.wndenis.snipsnap.calendar.components.MonthRow
import com.wndenis.snipsnap.calendar.components.YearDividers
import com.wndenis.snipsnap.calendar.components.YearRow
import com.wndenis.snipsnap.data.CalendarAdapter
import com.wndenis.snipsnap.data.CalendarEvent
import com.wndenis.snipsnap.data.CalendarSection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@ExperimentalAnimationApi
@Composable
fun ScheduleCalendar(
    state: ScheduleCalendarState,
    modifier: Modifier = Modifier,
    viewSpan: Long = 48 * 3600L, // in seconds
    updater: () -> Unit,
    adapter: CalendarAdapter
) =
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            // .pointerInteropFilter {
            //     val singlePointer = it.pointerCount <= 1
            //     Log.i("Changes", "${it.pointerCount} $singlePointer")
            //     // state.canScroll.value = singlePointer
            //     !singlePointer
            // }
            // .pointerInput(Unit) {
            //     awaitPointerEventScope {
            //         val event = awaitPointerEvent(PointerEventPass.Main)
            //         Log.i("Changes",
            //             event.changes
            //                 .count()
            //                 .toString()
            //         )
            //         state.canScroll.value = event.changes.count() == 1
            //         // state.canScroll.value = this.currentEvent.changes.count() == 1
            //     }
            // }
            .scrollable(
                state.scrollableState,
                Orientation.Horizontal,
                flingBehavior = state.scrollFlingBehavior,
                // enabled = state.canScroll.value
            )

    ) {
        val now = LocalDateTime.now()
        state.updateView(viewSpan, constraints.maxWidth)
        // DIVIDERS =============================================

        DayDividers(state = state, modifier = Modifier.matchParentSize())
        MonthDividers(state = state, modifier = Modifier.matchParentSize())
        YearDividers(
            state = state,
            modifier = Modifier.matchParentSize()
        )
        // CONTENT =============================================
        Column {
            // HEADERS =============================================
            YearRow(state = state)
            MonthRow(state = state)
            DaysRow(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            HoursRow(state)
            // EVENTS =============================================
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    adapter.sections.forEach {
                        CalendarSectionRow(
                            section = it,
                            state = state,
                            eventTimesVisible = true,
                            updater = updater,
                            editor = state.sectionEditor
                        )
                        Divider()
                    }
                }
                // hour dividers
                Canvas(modifier = Modifier.matchParentSize()) {
                    state.visibleHours.forEach { localDateTime ->
                        val offsetPercent = state.offsetFraction(localDateTime)
                        drawLine(
                            color = Color.Gray,
                            strokeWidth = 2f,
                            start = Offset(offsetPercent * size.width, 0f),
                            end = Offset(offsetPercent * size.width, size.height),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(10f, 20f),
                                phase = 5f
                            )
                        )
                    }
                }
            }
        }
        // "now" indicator =============================================
        NowIndicator(modifier, state, now)
    }

@Composable
fun NowIndicator(modifier: Modifier, state: ScheduleCalendarState, now: LocalDateTime) {
    val nowColor = MaterialTheme.colors.primary
    Canvas(modifier = modifier) {
        val offsetPercent = state.offsetFraction(now)
        drawLine(
            color = nowColor,
            strokeWidth = 6f,
            start = Offset(offsetPercent * size.width, 0f),
            end = Offset(offsetPercent * size.width, size.height)
        )
        drawCircle(
            nowColor,
            center = Offset(offsetPercent * size.width, 12f),
            radius = 12f
        )
    }
}

fun getTapEvent(
    width: Int,
    offset: Offset,
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime
): CalendarEvent {
    val pressDateRatio = offset.x / width

    val minStart = startDateTime.toEpochSecond(ZoneOffset.UTC)
    val maxEnd = endDateTime.toEpochSecond(ZoneOffset.UTC)

    val spanDuration = maxEnd - minStart
    val eventHalfDuration = spanDuration / 6f

    val pressMid = (maxEnd - minStart) * pressDateRatio
    val newStart = (minStart + pressMid - eventHalfDuration).toLong()
    val newEnd = (minStart + pressMid + eventHalfDuration).toLong()

    val newStartTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(newStart),
        ZoneId.systemDefault()
    )
    val newEndTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(newEnd),
        ZoneId.systemDefault()
    )

    val newEvent = CalendarEvent(
        startDate = newStartTime,
        endDate = newEndTime,
        name = "Новое событие",
        color = ColorPalette.Primary.random()
    )
    return newEvent
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarSectionRow(
    section: CalendarSection,
    state: ScheduleCalendarState,
    eventTimesVisible: Boolean,
    updater: () -> Unit,
    editor: (CalendarEvent) -> Unit
) {
    section.gc()
    val interSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        val newEvent = getTapEvent(
                            this.size.width,
                            offset,
                            state.startDateTime,
                            state.endDateTime
                        )
                        section.events.add(newEvent)

                        // ripple effect
                        GlobalScope.launch {
                            val press = PressInteraction.Press(offset)
                            interSource.emit(press)
                            interSource.emit(PressInteraction.Release(press))
                        }
                        updater()
                    }
                )
            }
            .indication(
                interactionSource = interSource,
                indication = rememberRipple()
            )
    ) {
        val eventMap = section.events.map { event ->
            Triple(
                event,
                event.startDate.isAfter(state.startDateTime) &&
                    event.startDate.isBefore(state.endDateTime),
                event.endDate.isAfter(state.startDateTime) &&
                    event.endDate.isBefore(state.endDateTime),
            )
        }.filter { (event, startHit, endHit) ->
            startHit || endHit || (
                event.startDate.isBefore(state.startDateTime) && event.endDate.isAfter(
                    state.endDateTime
                )
                )
        }

        if (eventMap.isNotEmpty()) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                eventMap.forEach { (event, startHit, endHit) ->
                    val (width, offsetX) = state.widthAndOffsetForEvent(
                        start = event.startDate,
                        end = event.endDate,
                        totalWidth = constraints.maxWidth
                    )

                    val shape = when {
                        startHit && endHit -> RoundedCornerShape(4.dp)
                        startHit -> RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp
                        )
                        endHit -> RoundedCornerShape(
                            topEnd = 4.dp,
                            bottomEnd = 4.dp
                        )
                        else -> RoundedCornerShape(4.dp)
                    }

                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .width(with(LocalDensity.current) { width.toDp() })
                            .offset { IntOffset(offsetX, 0) }
                            .background(event.color, shape = shape)
                            .clip(shape)
                            .clickable {
                                editor(event)
                                updater()
                            }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AnimatedVisibility(visible = eventTimesVisible) {
                            Text(
                                text = event.startDate
                                    .format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                                    event.endDate
                                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "",
                fontSize = 40.sp,
                fontWeight = W500,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}