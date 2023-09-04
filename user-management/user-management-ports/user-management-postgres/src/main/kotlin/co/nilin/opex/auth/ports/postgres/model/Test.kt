package co.nilin.opex.auth.ports.postgres.model


import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity(name = "test")
 class Test{
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id :Long?=null
        var email: String?=null

}
