package co.nilin.opex.core.data

import org.springframework.http.codec.multipart.FilePart

data class UploadDataRequest(var files: Map<String, FilePart>,
                             var filesPath: Map<String, String>) : KycRequest()