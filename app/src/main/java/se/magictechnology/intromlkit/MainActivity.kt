package se.magictechnology.intromlkit
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text as MLText
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import se.magictechnology.intromlkit.ui.theme.IntroMLKitTheme

class MainActivity : ComponentActivity() {

    private val imageResources = listOf(
        R.drawable.image1,
        R.drawable.image2,
        R.drawable.image3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntroMLKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyColumn {
                            itemsIndexed(imageResources) { index, imageResource ->
                                Image(
                                    painter = painterResource(id = imageResource),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(16.dp)
                                )

                                RecognizedText(
                                    index = index,
                                    onRecognizeClick = { recognizedText ->
                                        // Handle the recognized text as needed
                                        // For example, display it in a dialog
                                        // or update a state variable to show it beside the button
//                                        Log.i("MLKITDEBUG", "Recognized Text: $recognizedText")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RecognizedText(index: Int, onRecognizeClick: (String) -> Unit) {
        var recognizedText by remember { mutableStateOf<String?>(null) }

        Button(
            onClick = {
                runTextRecognition(index) { text ->
                    recognizedText = text
                    onRecognizeClick(text)
                }
            }
        ) {
            Text("Process image $index ")
        }

        recognizedText?.let {
            Text(
                text = "Recognized: $it",
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    private fun runTextRecognition(index: Int = 0, onTextRecognized: (String) -> Unit) {
        val selectedImage = BitmapFactory.decodeResource(resources, imageResources[index])
        val image = InputImage.fromBitmap(selectedImage, 0)
        val textRecognizerOptions = TextRecognizerOptions.Builder().build()
        val recognizer = TextRecognition.getClient(textRecognizerOptions)

        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts, onTextRecognized)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun processTextRecognitionResult(texts: MLText, onTextRecognized: (String) -> Unit) {
        val stringBuilder = StringBuilder()
        val blocks: List<MLText.TextBlock> = texts.textBlocks
        for (block in blocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    stringBuilder.append("\n")
                    stringBuilder.append(element.text)
                    stringBuilder.append("(conf: ")
                    stringBuilder.append(element.confidence.toString())
                    stringBuilder.append(")")

                }
            }
        }
        onTextRecognized(stringBuilder.toString())
    }
}