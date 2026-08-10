package mess.management

import java.time.LocalDate

/**
 * One meal record per member per day. All three meals default to ON; members/admins
 * toggle them OFF. {@code dailyCount} is a derived transient (breakfast = 0.5, lunch = 1,
 * dinner = 1) so OFF toggles and admin corrections are always reflected in totals.
 */
class Meal {

    Member member
    Month month
    LocalDate recordDate
    boolean breakfastOn = true
    boolean lunchOn = true
    boolean dinnerOn = true

    static transients = ['dailyCount']

    BigDecimal getDailyCount() {
        (breakfastOn ? 0.5G : 0.0G) + (lunchOn ? 1.0G : 0.0G) + (dinnerOn ? 1.0G : 0.0G)
    }

    def beforeValidate() {
        // Keep month consistent with recordDate when the client omits it.
        if (recordDate && !month) {
            month = Month.findByYearAndMonthNo(recordDate.year, recordDate.monthValue)
        }
    }

    static constraints = {
        member nullable: false, unique: 'recordDate'
        recordDate nullable: false
        month validator: { Month m, obj ->
            if (m?.closed && (obj.ident() == null || obj.isDirty())) return ['month.closed']
        }
    }
}
