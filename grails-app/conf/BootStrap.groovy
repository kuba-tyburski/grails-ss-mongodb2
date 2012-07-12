import pl.tyburski.User
import pl.tyburski.UserRole
import pl.tyburski.Role

class BootStrap {
    def springSecurityService

    def init = { servletContext ->
        println "adding roles"
        def adminRole = Role.findByAuthority("ROLE_ADMIN") ?: new Role(authority: 'ROLE_ADMIN').save(flush: true)
        def userRole = Role.findByAuthority("ROLE_USER") ?: new Role(authority: 'ROLE_USER').save(flush: true)
        println "adding admin"
        def admin = User.findByUsername("admin") ?:
            new User(username: "admin",
                    password: /*"admin"*/springSecurityService.encodePassword("admin", "admin"),
                    enabled: true, accountLocked: false,
                    accountExpired: false, passwordExpired: false).save(flush: true)

        println "connecting admin and role"
        UserRole.findByUserAndRole(admin, adminRole) ?: UserRole.create(admin, adminRole, true)

        if (admin.hasErrors()) {
            admin.errors.each { println it }
        }
    }
    def destroy = {
    }
}
