package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.auth.gateway.data.KycRequest
import co.nilin.opex.auth.gateway.data.UserProfileInfo
import co.nilin.opex.auth.gateway.utils.ErrorHandler
import co.nilin.opex.auth.gateway.utils.ResourceAuthenticator
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
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
    @Path("profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getAttributes(): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm)
            ?: return ErrorHandler.response(
                Response.Status.NOT_FOUND,
                OpexException(OpexError.NotFound, "User not found")
            )

        return Response.ok(user.attributes).build()
    }

    @POST
    @Path("profile")
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateAttributes(request: UserProfileInfo): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val user = session.users().getUserById(auth.getUserId(), opexRealm)
            ?: return ErrorHandler.response(
                Response.Status.NOT_FOUND,
                OpexException(OpexError.NotFound, "User not found")
            )

        with(request) {
            firstNameEn?.let { user.setSingleAttribute("firstNameEn", it) }
            lastNameEn?.let { user.setSingleAttribute("lastNameEn", it) }
            firstNameFa?.let { user.setSingleAttribute("firstNameFa", it) }
            lastNameFa?.let { user.setSingleAttribute("lastNameFa", it) }
            birthday?.let { user.setSingleAttribute("birthday", it) }
            birthdayAlt?.let { user.setSingleAttribute("birthdayAlt", it) }
            nationalID?.let { user.setSingleAttribute("nationalID", it) }
            passport?.let { user.setSingleAttribute("passport", it) }
            phoneNumber?.let { user.setSingleAttribute("phoneNumber", it) }
            telephone?.let { user.setSingleAttribute("telephone", it) }
            postalCode?.let { user.setSingleAttribute("postalCode", it) }
            residence?.let { user.setSingleAttribute("residence", it) }
            nationality?.let { user.setSingleAttribute("nationality", it) }
        }

        return Response.noContent().build()
    }

    @POST
    @Path("profile/kyc")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun kycFlow(request: KycRequest): Response {
        val auth = ResourceAuthenticator.bearerAuth(session)
        if (!auth.hasScopeAccess("trust"))
            return ErrorHandler.forbidden()

        val userId = auth.getUserId()!!
        val user = session.users().getUserById(userId, opexRealm)
            ?: return ErrorHandler.response(
                Response.Status.NOT_FOUND,
                OpexException(OpexError.NotFound, "User not found")
            )

        if (isInKycGroups(user)) {
            return ErrorHandler.response(
                Response.Status.BAD_REQUEST,
                OpexException(OpexError.BadRequest, "User is already in kyc groups")
            )
        }

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
            ?: return ErrorHandler.response(
                Response.Status.NOT_FOUND,
                OpexException(OpexError.GroupNotFound)
            )

        user.joinGroup(kycRequestGroup)

        user.apply {
            setSingleAttribute("kycSelfiePath", request.selfiePath)
            setSingleAttribute("kycIdCardPath", request.idCardPath)
            setSingleAttribute("kycAcceptFormPath", request.acceptFormPath)
        }

        return Response.noContent().build()
    }

    private fun isInKycGroups(user: UserModel): Boolean {
        return user.groupsStream.map { it.name }
            .filter { it == "kyc-accepted" || it == "kyc-rejected" || it == "kyc-requested" }
            .toList()
            .isNotEmpty()
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