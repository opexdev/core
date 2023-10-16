package co.nilin.opex.auth.gateway.model


import javax.persistence.*


@Entity(name = "whitelist")
@Table
class WhiteListModel{
        @Id
        var id :String?=null
        var identifier: String?=null
}
