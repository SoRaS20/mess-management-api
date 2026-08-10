package mess.management

import grails.gorm.transactions.Transactional
import java.time.LocalDate

@Transactional
class MealService {

    private static final List<String> SLOTS = ['breakfast', 'lunch', 'dinner']

    Meal get(Long id) {
        Meal.get(id)
    }

    @Transactional(readOnly = true)
    List<Meal> byMonth(Long monthId) {
        Month month = Month.get(monthId)
        month ? Meal.findAllByMonth(month, [sort: 'recordDate']) : []
    }

    @Transactional(readOnly = true)
    List<Meal> byDate(Long monthId, LocalDate date) {
        Month month = Month.get(monthId)
        month ? Meal.findAllByMonthAndRecordDate(month, date) : []
    }

    /**
     * Idempotently create an all-ON meal row for every active member for every day
     * of the month. Rows that already exist (member, recordDate) are skipped, so this
     * is safe to re-run. Returns the number of rows created.
     */
    int generateDefaultMeals(Long monthId) {
        Month month = Month.get(monthId)
        if (!month) return 0

        List<Member> members = Member.findAllByActive(true)
        int created = 0
        eachDayOfMonth(month) { LocalDate date ->
            members.each { Member member ->
                if (!Meal.findByMemberAndRecordDate(member, date)) {
                    new Meal(member: member, month: month, recordDate: date).save(failOnError: true)
                    created++
                }
            }
        }
        created
    }

    /** Create default-ON meal rows for one member from a start date to the month end. */
    int ensureMealsFromDate(Long monthId, Long memberId, LocalDate from) {
        Month month = Month.get(monthId)
        Member member = Member.get(memberId)
        if (!month || !member) return 0

        int created = 0
        eachDayOfMonth(month) { LocalDate date ->
            if (!date.isBefore(from) && !Meal.findByMemberAndRecordDate(member, date)) {
                new Meal(member: member, month: month, recordDate: date).save(failOnError: true)
                created++
            }
        }
        created
    }

    /** Fetch-or-create the meal row for (member, date) and set the requested slot. */
    Meal toggle(Long mealId, String slot, boolean on) {
        Meal meal = Meal.get(mealId)
        if (!meal) return null
        applySlot(meal, slot, on)
        meal.save(failOnError: true)
        meal
    }

    Meal toggleFor(Long memberId, LocalDate date, String slot, boolean on) {
        Member member = Member.get(memberId)
        if (!member) return null

        Meal meal = Meal.findByMemberAndRecordDate(member, date)
        if (!meal) {
            Month month = Month.findByYearAndMonthNo(date.year, date.monthValue)
            meal = new Meal(member: member, month: month, recordDate: date)
        }
        applySlot(meal, slot, on)
        meal.save(failOnError: true)
        meal
    }

    /** Admin correction: set any of breakfastOn/lunchOn/dinnerOn from a flags map. */
    Meal adminCorrect(Long mealId, Map flags) {
        Meal meal = Meal.get(mealId)
        if (!meal) return null
        if (flags.containsKey('breakfastOn')) meal.breakfastOn = flags.breakfastOn as boolean
        if (flags.containsKey('lunchOn')) meal.lunchOn = flags.lunchOn as boolean
        if (flags.containsKey('dinnerOn')) meal.dinnerOn = flags.dinnerOn as boolean
        meal.save(failOnError: true)
        meal
    }

    @Transactional(readOnly = true)
    BigDecimal totalMeals(Long monthId) {
        byMonth(monthId).inject(0.0G) { BigDecimal sum, Meal m -> sum + m.dailyCount }
    }

    @Transactional(readOnly = true)
    BigDecimal mealCountFor(Long memberId, Long monthId) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return 0.0G
        Meal.where { member == theMember && month == theMonth }.list()
                .inject(0.0G) { BigDecimal sum, Meal m -> sum + m.dailyCount }
    }

    // --- helpers ---

    private void applySlot(Meal meal, String slot, boolean on) {
        switch (slot?.toLowerCase()) {
            case 'breakfast': meal.breakfastOn = on; break
            case 'lunch': meal.lunchOn = on; break
            case 'dinner': meal.dinnerOn = on; break
            default: throw new IllegalArgumentException("Unknown meal slot: ${slot}. Expected one of ${SLOTS}.")
        }
    }

    private void eachDayOfMonth(Month month, Closure body) {
        LocalDate cursor = LocalDate.of(month.year, month.monthNo, 1)
        LocalDate end = cursor.plusMonths(1)
        while (cursor.isBefore(end)) {
            body(cursor)
            cursor = cursor.plusDays(1)
        }
    }
}
