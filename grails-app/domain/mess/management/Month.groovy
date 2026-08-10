package mess.management

/**
 * A billing month. Uses {@code monthNo} (1..12) rather than "month" to avoid confusion,
 * and the (year, monthNo) pair is unique. When {@code closed} is true, all transactional
 * child records (Meal/Bazar/Expense/Deposit/Rent) reject inserts and updates via their
 * shared {@code month} validator.
 */
class Month {

    Integer year
    Integer monthNo
    boolean closed = false
    Member manager

    static constraints = {
        year range: 2000..2100, unique: 'monthNo'
        monthNo range: 1..12
        manager nullable: true
    }

    static mapping = {
        table 'mess_month'
    }

    String toString() { "$year-${String.format('%02d', monthNo ?: 0)}" }
}
