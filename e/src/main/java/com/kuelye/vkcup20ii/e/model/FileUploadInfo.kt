package com.kuelye.vkcup20ii.e.model

import org.json.JSONObject

data class FileUploadInfo(
    val server: String,
    val photo: String,
    val hash: String
) {

    companion object {
        fun parse(jo: JSONObject): FileUploadInfo {
            return FileUploadInfo(
                server = jo.getString("server"),
                photo = jo.getString("photo"),
                hash = jo.getString("hash")
            )
        }
    }

}