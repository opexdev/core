package co.nilin.opex.kyc.core.data

import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.web.multipart.MultipartFile

data class UploadDataRequest(var files: Map<String, FilePart>?=null,
                             var filesPath: Map<String, String>?=null) : KycRequest()