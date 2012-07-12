package pl.tyburski

/**
 * Created with IntelliJ IDEA.
 * User: ive
 * Date: 12.07.12
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
class PasswordEncoder implements org.springframework.security.authentication.encoding.PasswordEncoder {
    String encodePassword(String s, Object o) {
        return s;
    }

    boolean isPasswordValid(String s, String s1, Object o) {
        return s == s1;
    }
}
