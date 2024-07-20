package com.example.textfinder

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.textfinder.ui.theme.TextFinderTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream

class Predict : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text1 = intent.getStringExtra("text1") ?: ""
        enableEdgeToEdge()
        setContent {
            TextFinderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    ImagePickerScreen(text1)
                }
            }
        }
    }
}

@Composable
fun ImagePickerScreen(data: String) {
    val context = LocalContext.current
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var text by remember { mutableStateOf<String?>(null) }
    var str by remember { mutableStateOf("Extract Text") }
    val clipboardManager: androidx.compose.ui.platform.ClipboardManager = LocalClipboardManager.current
    if (data!="")
    {
        Box {
            LazyColumn(modifier = Modifier.padding(top = 40.dp, start = 15.dp, end = 10.dp,bottom=10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
                ) {
                item {
                    Text(text = data)
                    Button(onClick = {
                        clipboardManager.setText(AnnotatedString(data)
                            )
                    }) {
                        Text("Copy")
                    }

                }
            }
        }
    }
    else
    {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            imageUri?.let {
                Column {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .clickable { launcher.launch("image/*") }
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 18.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Button(onClick = {
                        if (str == "Extract Text") {
                            val inputStream: InputStream? =
                                context.contentResolver.openInputStream(it)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                            val inputImage = InputImage.fromBitmap(bitmap, 0)

                            recognizer.process(inputImage)
                                .addOnSuccessListener { visionText ->
                                    text = visionText.text
                                }
                                .addOnFailureListener { e ->
                                    text = "Error: ${e.message}"
                                }
                            str = "Re-Select Image"
                        } else {
                            launcher.launch("image/*")
                            str = "Extract Text"
                        }
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(text = str)
                    }
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    text?.let {
                        Box(modifier = Modifier.padding(10.dp)) {
                            LazyColumn {
                                item {
                                    Text(it)
                                    Button(onClick = {
                                        clipboardManager.setText(AnnotatedString(text!!))
                                    }) {
                                        Text("Copy")
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Pick Image")
                }
            }
        }
    }
}