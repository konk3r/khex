package com.casadetasha.tools.khex.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.SwingPanel
import com.casadetasha.tools.khex.file.FileExtensionInfo
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun SelectFileDialog(fileExtension: FileExtensionInfo? = null, onFileSelected: (File?) -> Unit) {
    SwingPanel(
        factory = {
            JPanel().apply {
                val chooser = JFileChooser("""C:\Users\konk3\OneDrive\projects\rpi-bs zelda""")
                fileExtension?.let { chooser.fileFilter = FileNameExtensionFilter(it.description, it.extension) }

                val file = when (chooser.showOpenDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> chooser.selectedFile
                    else -> null
                }
                onFileSelected(file)
            }
        }
    )
}
