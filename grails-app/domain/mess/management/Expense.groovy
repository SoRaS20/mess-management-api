package mess.management

import java.time.LocalDate

/**
 * A shared utility/other expense for the month. Split equally among active members.
 */
class Expense {

    Month month
    BigDecimal amount
    String description
    String category
    LocalDate expenseDate = LocalDate.now()
    Member paidBy
    Date dateCreated

    static constraints = {
        amount min: 0.0G, scale: 2
        description nullable: true, maxSize: 255
        category inList: ['gas', 'electricity', 'water', 'internet', 'other'], nullable: false
        expenseDate nullable: false
        paidBy nullable: true
        dateCreated nullable: true
        month validator: { Month m, obj ->
            if (m?.closed && (obj.ident() == null || obj.isDirty())) return ['month.closed']
        }
    }
}
