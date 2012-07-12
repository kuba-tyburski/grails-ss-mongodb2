package pl.tyburski

class User extends AbstractDomainClass {

	transient springSecurityService

    static mapWith = "mongo"

	String username
	String password
    String cleanPassword
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static constraints = {
		username blank: false, unique: true
		password blank: false
        enabled()
        accountExpired()
        accountLocked()
        passwordExpired()
        cleanPassword nullable: true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

	def beforeInsert() {
        cleanPassword = password
	}

	def beforeUpdate() {
        if (cleanPassword != password) {
            cleanPassword = password
        }
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
}
