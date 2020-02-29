package com.kuelye.vkcup20ii.core.model.misc

import org.json.JSONObject

data class FileUploadInfo(
    val server: String,
    val file: String,
    val hash: String
) {
    companion object {
        fun parse(jo: JSONObject, fileKey: String): FileUploadInfo {
            return FileUploadInfo(
                server = jo.getString("server"),
                file = jo.getString(fileKey),
                hash = jo.getString("hash")
            )
        }
    }
}