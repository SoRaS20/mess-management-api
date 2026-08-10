package mess.management

import java.time.LocalDate

class Member {

    String name
    String phone
    LocalDate joinDate = LocalDate.now()
    boolean active = true
    User user

    static constraints = {
        name blank: false, size: 2..100
        phone nullable: true, matches: /[0-9+\-\s]{6,20}/
        joinDate nullable: false
        user nullable: true, unique: true
    }
}
