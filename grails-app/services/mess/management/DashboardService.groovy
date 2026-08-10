package mess.management

import grails.gorm.transactions.Transactional
import java.math.RoundingMode

@Transactional(readOnly = true)
class DashboardService {

    MealService mealService
    BazarService bazarService
    ExpenseService expenseService
    DepositService depositService
    MemberService memberService

    /**
     * Aggregate figures for a month. The meal rate is driven by bazar spend:
     * {@code mealRate = totalBazar / totalMeals}. Expenses are split equally
     * among active members. Both divisions are guarded against zero.
     */
    Map summary(Long monthId) {
        Month month = Month.get(monthId)
        if (!month) return null

        BigDecimal totalMeals = mealService.totalMeals(monthId)
        BigDecimal totalBazar = bazarService.totalBazar(monthId)
        BigDecimal totalExpenses = expenseService.totalExpenses(monthId)
        BigDecimal totalDeposits = depositService.totalDeposits(monthId)
        int memberCount = memberService.activeCount()

        BigDecimal mealRate = totalMeals > 0.0G ?
                (totalBazar / totalMeals).setScale(2, RoundingMode.HALF_UP) : 0.0G
        BigDecimal expenseSharePerMember = memberCount > 0 ?
                (totalExpenses / memberCount).setScale(2, RoundingMode.HALF_UP) : 0.0G

        [
                monthId              : month.id,
                year                 : month.year,
                monthNo              : month.monthNo,
                closed               : month.closed,
                memberCount          : memberCount,
                totalMeals           : totalMeals,
                totalBazar           : totalBazar,
                mealRate             : mealRate,
                totalExpenses        : totalExpenses,
                totalDeposits        : totalDeposits,
                expenseSharePerMember: expenseSharePerMember
        ]
    }
}
