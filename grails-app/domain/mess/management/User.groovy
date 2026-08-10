package mess.management

/**
 * Application user. Authentication/authorization are out of scope for the MVP,
 * so {@code password} is stored as a plain field for now — it is never rendered in
 * any GSON view and must never be logged. Hash it before real auth is introduced.
 *
 * Mapped to table {@code app_user} because {@code user} is a reserved word in PostgreSQL.
 */
class User {

    String username
    String password
    String role = 'MEMBER'

    static constraints = {
        username blank: false, unique: true, size: 3..50
        password blank: false, size: 4..255
        role inList: ['ADMIN', 'MEMBER']
    }

    static mapping = {
        table 'app_user'
        password column: 'user_password'
    }
}
