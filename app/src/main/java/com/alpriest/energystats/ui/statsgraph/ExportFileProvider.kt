package com.alpriest.energystats.ui.statsgraph

import androidx.core.content.FileProvider
import com.alpriest.energystats.R

class ExportFileProvider() : FileProvider() {
    init {
        context?.resources?.getXml(R.xml.file_paths)
    }
}