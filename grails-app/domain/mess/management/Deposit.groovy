package mess.management

import java.time.LocalDate

/**
 * Money a member pays into the mess. Balance = deposits − meal cost − expense share − rent.
 */
class Deposit {

    Member member
    Month month
    BigDecimal amount
    LocalDate depositDate = LocalDate.now()
    String description
    Date dateCreated

    static constraints = {
        member nullable: false
        amount min: 0.0G, scale: 2
        depositDate nullable: false
        description nullable: true, maxSize: 255
        dateCreated nullable: true
        month validator: { Month m, obj ->
            if (m?.closed && (obj.ident() == null || obj.isDirty())) return ['month.closed']
        }
    }
}
