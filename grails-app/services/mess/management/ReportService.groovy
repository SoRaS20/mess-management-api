package mess.management

import grails.gorm.transactions.Transactional
import java.math.RoundingMode
import java.time.LocalDate

@Transactional(readOnly = true)
class ReportService {

    DashboardService dashboardService
    BalanceService balanceService
    MealService mealService
    BazarService bazarService
    ExpenseService expenseService
    DepositService depositService

    /** Monthly summary: dashboard figures + per-member balance rows + rolled-up totals. */
    Map monthlyReport(Long monthId) {
        Month month = Month.get(monthId)
        if (!month) return null

        Map summary = dashboardService.summary(monthId)
        List<Map> members = balanceService.balancesForMonth(monthId)

        Map totals = [
                deposits           : sum(members, 'deposit'),
                mealCost           : sum(members, 'mealCost'),
                expenses           : summary.totalExpenses,
                rent               : sum(members, 'rent'),
                bazarContributions : sum(members, 'bazarContribution'),
                expenseContributions: sum(members, 'expenseContribution'),
                foodBalances       : sum(members, 'foodBalance'),
                rentBalances       : sum(members, 'rentBalance'),
                netBalance         : sum(members, 'balance')
        ]

        [
                month  : [id: month.id, year: month.year, monthNo: month.monthNo, closed: month.closed],
                summary: summary,
                members: members,
                totals : totals
        ]
    }

    /** Daily meal report: each member's flags/count for a date + that day's bazar & expenses. */
    Map dailyReport(Long monthId, LocalDate date) {
        Month month = Month.get(monthId)
        if (!month) return null

        List<Meal> meals = mealService.byDate(monthId, date)
        List<Map> memberRows = meals.collect { Meal m ->
            [
                    memberId   : m.member.id,
                    memberName : m.member.name,
                    breakfastOn: m.breakfastOn,
                    lunchOn    : m.lunchOn,
                    dinnerOn   : m.dinnerOn,
                    dailyCount : m.dailyCount
            ]
        }

        BigDecimal totalMealsThatDay = meals.inject(0.0G) { BigDecimal s, Meal m -> s + m.dailyCount }
        BigDecimal bazarThatDay = bazarService.byMonth(monthId)
                .findAll { it.bazarDate == date }
                .inject(0.0G) { BigDecimal s, Bazar b -> s + (b.amount ?: 0.0G) }
        BigDecimal expensesThatDay = expenseService.byMonth(monthId)
                .findAll { it.expenseDate == date }
                .inject(0.0G) { BigDecimal s, Expense e -> s + (e.amount ?: 0.0G) }

        [
                date     : date,
                monthId  : month.id,
                members  : memberRows,
                dayTotals: [totalMeals: totalMealsThatDay, bazarThatDay: bazarThatDay, expensesThatDay: expensesThatDay]
        ]
    }

    /** One member's full picture for a month: meals by day, deposits, rent, balance breakdown. */
    Map memberReport(Long memberId, Long monthId) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return null

        List<Meal> meals = Meal.where { member == theMember && month == theMonth }.list(sort: 'recordDate')
        List<Map> byDay = meals.collect { Meal m -> [date: m.recordDate, dailyCount: m.dailyCount] }
        BigDecimal totalCount = meals.inject(0.0G) { BigDecimal s, Meal m -> s + m.dailyCount }

        List<Map> deposits = Deposit.where { member == theMember && month == theMonth }.list(sort: 'depositDate')
                .collect { Deposit d -> [date: d.depositDate, amount: d.amount] }

        Map balance = balanceService.balanceFor(memberId, monthId)

        [
                member              : [id: theMember.id, name: theMember.name],
                month               : [id: theMonth.id, year: theMonth.year, monthNo: theMonth.monthNo],
                meals               : [totalCount: totalCount, byDay: byDay],
                deposits            : deposits,
                rent                : balance.rent,
                mealRate            : balance.mealRate,
                mealCost            : balance.mealCost,
                expenseShare        : balance.expenseShare,
                bazarContribution   : balance.bazarContribution,
                expenseContribution : balance.expenseContribution,
                totalDeposit        : balance.deposit,
                foodBalance         : balance.foodBalance,
                rentBalance         : balance.rentBalance,
                balance             : balance.balance
        ]
    }

    private static BigDecimal sum(List<Map> rows, String key) {
        rows.inject(0.0G) { BigDecimal s, Map r -> s + (r[key] ?: 0.0G) }
    }
}
