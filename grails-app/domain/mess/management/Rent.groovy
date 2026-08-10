package mess.management

/**
 * Per-member, per-month rent. One rent row per (member, month).
 */
class Rent {

    Member member
    Month month
    BigDecimal amount

    static constraints = {
        member nullable: false, unique: 'month'
        amount min: 0.0G, scale: 2
        month validator: { Month m, obj ->
            if (m?.closed && (obj.ident() == null || obj.isDirty())) return ['month.closed']
        }
    }
}
