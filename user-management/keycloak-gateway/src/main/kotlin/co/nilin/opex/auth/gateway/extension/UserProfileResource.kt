package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.auth.gateway.data.KYCStatus
import co.nilin.opex.auth.gateway.data.KYCStatusResponse
import co.nilin.opex.auth.gateway.data.KycRequest
import co.nilin.opex.auth.gateway.data.UserProfileInfo
import co.nilin.opex.auth.gateway.utils.ErrorHandler
import co.nilin.opex.auth.gateway.utils.ResourceAuthenticator
import co.nilin.opex.utility.error.data.OpexError
import org.jboss.resteasy.plugins.providers.multipart.InputPart
import org.keycloak.models.KeycloakSession
import org.keycloak.models.UserModel
import org.keycloak.services.resource.RealmResourceProvider
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import java.io.File
import java.nio.file.Paths
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.streams.toList

class UserProfileResource(private val session: KeycloakSession) : RealmResourceProvider {

    private val logger = LoggerFactory.getLogger(UserProfileResource::class.java)
    private val opexRealm = session.realms().getRealm("opex")

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getAttributes(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()
        val attributes = mutableMapOf<String, String>()
        user.attributes.entries.forEach {
            if (it.value.size == 1)
                attributes[it.key] = it.value[0]
            else if (it.value.size > 1) {
                attributes[it.key] = with(StringBuilder()) {
                    it.value.forEach { v -> append("$v,") }
                    deleteCharAt(length - 1)
                    toString()
                }
            }
        }

        return Response.ok(attributes).build()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateAttributes(request: UserProfileInfo): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm) ?: return ErrorHandler.userNotFound()

        with(request) {
            firstNameEn?.let { user.setSingleAttribute("firstNameEn", it) }
            lastNameEn?.let { user.setSingleAttribute("lastNameEn", it) }
            firstName?.let { user.setSingleAttribute("firstName", it) }
            lastName?.let { user.setSingleAttribute("lastName", it) }
            birthdayJ?.let { user.setSingleAttribute("birthdayJ", it) }
            birthdayG?.let { user.setSingleAttribute("birthdayG", it) }
            nationalId?.let { user.setSingleAttribute("nationalId", it) }
            passportNumber?.let { user.setSingleAttribute("passportNumber", it) }
            mobile?.let { user.setSingleAttribute("mobile", it) }
            telephone?.let { user.setSingleAttribute("telephone", it) }
            postalCode?.let { user.setSingleAttribute("postalCode", it) }
            residence?.let { user.setSingleAttribute("residence", it) }
            nationality?.let { user.setSingleAttribute("nationality", it) }
        }

        return Response.noContent().build()
    }

    @POST
    @Path("kyc")
    @Consumes(MediaType.APPLICATION_JSON)
    fun kycFlow(request: KycRequest): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val userId = auth.getUserId()
        val user = session.users().getUserById(userId, opexRealm) ?: return ErrorHandler.userNotFound()

        if (isInKycGroups(user))
            return ErrorHandler.response(
                Response.Status.BAD_REQUEST,
                OpexError.BadRequest,
                "User is already in kyc groups"
            )

        /*val forms = input.formDataMap

        val selfiePart = createPartContent(forms["selfie"]?.get(0)!!)
        val idPart = createPartContent(forms["idCard"]?.get(0)!!)
        val formPart = createPartContent(forms["acceptForm"]?.get(0)!!)

        val selfiePath = proxy.upload(userId, selfiePart).path
        val idCard = proxy.upload(userId, idPart).path
        val acceptForm = proxy.upload(userId, formPart).path*/


        val kycRequestGroup = session.groups()
            .getGroupsStream(opexRealm)
            .toList()
            .find { it.name == "kyc-requested" }
            ?: return ErrorHandler.response(Response.Status.NOT_FOUND, OpexError.GroupNotFound)

        user.apply {
            joinGroup(kycRequestGroup)
            setSingleAttribute("selfiePath", request.selfiePath)
            setSingleAttribute("idCardPath", request.idCardPath)
            setSingleAttribute("acceptFormPath", request.acceptFormPath)
        }

        return Response.noContent().build()
    }

    @GET
    @Path("kyc/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun kycStatus(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val userId = auth.getUserId()
        val user = session.users().getUserById(userId, opexRealm) ?: return ErrorHandler.userNotFound()
        val status = when (getUserKycGroup(user)) {
            "kyc-accepted" -> KYCStatus.ACCEPTED
            "kyc-rejected" -> KYCStatus.REJECTED
            "kyc-requested" -> KYCStatus.REQUESTED
            else -> KYCStatus.NOT_REQUESTED
        }

        return Response.ok(KYCStatusResponse(status)).build()
    }

    private fun isInKycGroups(user: UserModel): Boolean {
        return user.groupsStream.map { it.name }
            .filter { it == "kyc-accepted" || it == "kyc-rejected" || it == "kyc-requested" }
            .toList()
            .isNotEmpty()
    }

    private fun getUserKycGroup(user: UserModel): String? {
        val kycGroups = user.groupsStream.map { it.name }
            .filter { it == "kyc-accepted" || it == "kyc-rejected" || it == "kyc-requested" }
            .toList()
        return if (kycGroups.isEmpty()) null else kycGroups[0]
    }

    private fun createPartContent(input: InputPart): Flux<DataBuffer> {
        val file = input.getBody(File::class.java, null)
        val factory = DefaultDataBufferFactory()
        return DataBufferUtils.read(Paths.get(file.absolutePath), factory, DEFAULT_BUFFER_SIZE)

//        val fileItem = DiskFileItem(
//            "selfie",
//            Files.probeContentType(file.toPath()),
//            false,
//            file.name,
//            file.length().toInt(),
//            file.parentFile
//        )
//
//        FileInputStream(file).use {
//            it.transferTo(fileItem.outputStream)
//        }
    }

    override fun close() {

    }

    override fun getResource(): Any {
        return this
    }
}