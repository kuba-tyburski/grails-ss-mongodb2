package pl.tyburski

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.apache.commons.logging.LogFactory

class MyUserController extends grails.plugins.springsecurity.ui.UserController {

    private static final log = LogFactory.getLog(this)

    def userSearch = {

        boolean useOffset = params.containsKey('offset')
        setIfMissing 'max', 10, 100
        setIfMissing 'offset', 0

        def hql = new StringBuilder('FROM ').append(lookupUserClassName()).append(' u WHERE 1=1 ')
        def queryParams = [:]

        def userLookup = SpringSecurityUtils.securityConfig.userLookup
        String usernameFieldName = userLookup.usernamePropertyName

        def criteria = new DetachedCriteria(lookupUserClass()).build() {}

        for (name in [username: usernameFieldName]) {
            if (params[name.key]) {
//                hql.append " AND LOWER(u.${name.value}) LIKE :${name.key}"
//                queryParams[name.key] = params[name.key].toLowerCase() + '%'
                criteria = criteria.build() {
                    ilike name.value, params[name.key]
                }
            }
        }

        String enabledPropertyName = userLookup.enabledPropertyName
        String accountExpiredPropertyName = userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = userLookup.passwordExpiredPropertyName

        for (name in [enabled: enabledPropertyName,
                accountExpired: accountExpiredPropertyName,
                accountLocked: accountLockedPropertyName,
                passwordExpired: passwordExpiredPropertyName]) {
            Integer value = params.int(name.key)
            if (value) {
//                hql.append " AND u.${name.value}=:${name.key}"
//                queryParams[name.key] = value == 1

                criteria = criteria.build() {
                    eq name.value, value
                }
            }
        }

//        int totalCount = lookupUserClass().executeQuery("SELECT COUNT(DISTINCT u) $hql", queryParams)[0]

        int totalCount = criteria.list().size()

        Integer max = params.int('max')
        Integer offset = params.int('offset')

//        String orderBy = ''
        if (params.sort) {
//            orderBy = " ORDER BY u.$params.sort ${params.order ?: 'ASC'}"
            criteria = criteria.build() {
                order params.sort, params.order ?: 'ASC'
            }
        }

        def results = criteria.list(max: max, offset: offset)

//        def results = lookupUserClass().executeQuery(
//                "SELECT DISTINCT u $hql $orderBy",
//                queryParams, [max: max, offset: offset])
        def model = [results: results, totalCount: totalCount, searched: true]

        // add query params to model for paging
        for (name in ['username', 'enabled', 'accountExpired', 'accountLocked',
                'passwordExpired', 'sort', 'order']) {
            model[name] = params[name]
        }

        render view: 'search', model: model
    }

    def ajaxUserSearch = {

        def jsonData = []

        if (params.term?.length() > 2) {
            String username = params.term
            String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

            setIfMissing 'max', 10, 100

            def c = lookupUserClass().createCriteria()
            def results = c.list {
                ilike(usernameFieldName, username)
                maxResults(params.max)
                order(usernameFieldName, "asc")
            }
//            def results = lookupUserClass().executeQuery(
//                    "SELECT DISTINCT u.$usernameFieldName " +
//                            "FROM ${lookupUserClassName()} u " +
//                            "WHERE LOWER(u.$usernameFieldName) LIKE :name " +
//                            "ORDER BY u.$usernameFieldName",
//                    [name: "${username.toLowerCase()}%"],
//                    [max: params.max])

            for (result in results) {
                jsonData << [value: result]
            }
        }

        render text: jsonData as JSON, contentType: 'text/plain'
    }

    def update = {
        String passwordFieldName = SpringSecurityUtils.securityConfig.userLookup.passwordPropertyName

        def user = findById()
        if (!user) return
        if (!versionCheck('user.label', 'User', user, [user: user])) {
            return
        }

        def oldPassword = user."$passwordFieldName"
        log.info(oldPassword)
        if(!params.password) {
            log.info("password null")
            params.password = oldPassword
        }
        user.properties = params
        if (params.password && !params.password.equals(oldPassword)) {
            log.info("generating new password hash with salt")
            String salt = saltSource instanceof NullSaltSource ? null : params.username
            user."$passwordFieldName" = springSecurityUiService.encodePassword(params.password, salt)
        }

        if (!user.save(flush: true)) {
            log.info("save fals")
            render view: 'edit', model: buildUserModel(user)
            return
        }

        String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

        lookupUserRoleClass().removeAll user
        addRoles user
        userCache.removeUserFromCache user[usernameFieldName]
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
        redirect action: edit, id: user.id
    }
}
