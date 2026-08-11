package mess.management

import java.time.LocalDate

/**
 * A grocery/shopping purchase for the month. The sum of bazar amounts drives the meal rate.
 */
class Bazar {

    Member member
    Month month
    BigDecimal amount
    String description
    LocalDate bazarDate = LocalDate.now()
    Date dateCreated

    static constraints = {
        member nullable: false
        amount min: 0.0G, scale: 2
        description nullable: true, maxSize: 255
        bazarDate nullable: false
        dateCreated nullable: true
        month validator: { Month m, obj ->
            if (m?.closed && (obj.ident() == null || obj.isDirty())) return ['month.closed']
        }
    }
}
