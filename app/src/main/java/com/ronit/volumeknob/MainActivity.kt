package com.ronit.volumeknob

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ronit.volumeknob.ui.theme.VolumeKnobTheme
import kotlin.math.PI
import kotlin.math.atan2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101010))
            ) {
                val context = LocalContext.current
                val audioManager: AudioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, Color.Green, RoundedCornerShape(10.dp))
                        .padding(30.dp)
                ) {
                    var volume by remember {
                        mutableStateOf(0f)
                    }
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                    val targetVolume = (volume * maxVolume ).toInt()
                    Log.d("TAG", "onCreate: $volume")
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        targetVolume,
                        0
                    )

                    val barCount = 20
                    MusicKnob(
                        modifier = Modifier.size(100.dp)
                    ) {
                        volume = it
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                    VolumeBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        activeBars = (barCount * volume).toInt(),
                        barCount = barCount
                    )
                  }
            }
        }
    }
}

@Composable
fun VolumeBar(
    modifier: Modifier = Modifier,
    activeBars: Int = 0,
    barCount: Int = 10
){
    BoxWithConstraints (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ){
        val barWidth = remember {
            constraints.maxWidth / (2f * barCount)
        }
        Canvas(
            modifier = modifier

        ) {
            for (i in 0 until barCount) {
                drawRoundRect(
                    color = if (i in 0..activeBars) Color.Green else Color.DarkGray,
                    topLeft = Offset(
                        i * barWidth * 2f + barWidth / 2f,
                        0f
                    ),
                    size = Size(
                        width = barWidth,
                        height = constraints.maxHeight.toFloat()
                    ),
                    cornerRadius = CornerRadius(10f)
                )

            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MusicKnob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    onValueChange: (Float) -> Unit,

){
    var rotation by remember {
        mutableFloatStateOf(limitingAngle)
    }
    var touchX by remember {
        mutableFloatStateOf(0f)
    }
    var touchY by remember {
        mutableFloatStateOf(0f)
    }
    var centerX by remember {
        mutableFloatStateOf(0f)
    }
    var centerY by remember {
        mutableFloatStateOf(0f)
    }

    Image(
        painter = painterResource(id = R.drawable.music_knob),
        contentDescription = "Music knob",
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val windowSize = it.boundsInWindow().size
                val windowWidth = windowSize.width
                val windowHeight = windowSize.height
                centerX = windowWidth / 2f
                centerY = windowHeight / 2f
            }
            .pointerInteropFilter { event->
                touchX=event.x
                touchY=event.y
                val angle = -atan2(centerX - touchX, centerY - touchY) * (180f / PI).toFloat()
                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE-> {
                        if (angle !in -limitingAngle..limitingAngle){
                            val fixedAngle= if (angle in -180f..-limitingAngle){
                                360f+angle
                            }else{
                                angle
                            }
                            rotation = fixedAngle
                            val  percentage = (fixedAngle-limitingAngle)/(360f-2*limitingAngle)
                            onValueChange(percentage)
                            true
                        }else false
                    }
                    else -> false
                }
            }
            .rotate(rotation)
    )
}