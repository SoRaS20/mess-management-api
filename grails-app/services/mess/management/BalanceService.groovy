package mess.management

import grails.gorm.transactions.Transactional
import java.math.RoundingMode

@Transactional(readOnly = true)
class BalanceService {

    DashboardService dashboardService
    MealService mealService
    RentService rentService
    DepositService depositService
    MemberService memberService
    BazarService bazarService
    ExpenseService expenseService

    /**
     * Balance for one member in one month:
     *   balance = (bazarContribution + expenseContribution + deposit) − (mealCount × mealRate) − expenseShare − rent
     * A positive balance means the member has credit; negative means they owe.
     */
    Map balanceFor(Long memberId, Long monthId) {
        Member member = Member.get(memberId)
        Map summary = dashboardService.summary(monthId)
        if (!member || !summary) return null
        buildBalance(member, monthId, summary)
    }

    /** Balances for every active member, plus any inactive/banned members who had activity this month. */
    List<Map> balancesForMonth(Long monthId) {
        Map summary = dashboardService.summary(monthId)
        if (!summary) return []
        
        List<Map> result = []
        Member.list(sort: 'name').each { Member member ->
            Map bal = buildBalance(member, monthId, summary)
            boolean hasParticipation = bal.meals > 0 || bal.rent > 0 || bal.deposit > 0 || bal.bazarContribution > 0 || bal.expenseContribution > 0
            if ((member.active && !member.banned) || hasParticipation) {
                // If they are banned/inactive but had 0 meals, they don't pay expense share
                if ((member.banned || !member.active) && bal.meals == 0.0G) {
                    bal.expenseShare = 0.0G
                    bal.foodBalance = (bal.bazarContribution + bal.expenseContribution - bal.mealCost - bal.expenseShare).setScale(2, RoundingMode.HALF_UP)
                    bal.balance = (bal.foodBalance + bal.rentBalance).setScale(2, RoundingMode.HALF_UP)
                }
                result << bal
            }
        }
        return result
    }

    private Map buildBalance(Member member, Long monthId, Map summary) {
        BigDecimal mealRate = summary.mealRate
        BigDecimal meals = mealService.mealCountFor(member.id, monthId)
        BigDecimal mealCost = (meals * mealRate).setScale(2, RoundingMode.HALF_UP)
        BigDecimal expenseShare = summary.expenseSharePerMember
        BigDecimal rent = rentService.rentFor(member.id, monthId)
        BigDecimal deposit = depositService.totalDepositFor(member.id, monthId)
        
        BigDecimal bazarContribution = bazarService.totalBazarFor(member.id, monthId)
        BigDecimal expenseContribution = expenseService.totalExpenseFor(member.id, monthId)

        BigDecimal foodBalance = (bazarContribution + expenseContribution - mealCost - expenseShare).setScale(2, RoundingMode.HALF_UP)
        BigDecimal rentBalance = (deposit - rent).setScale(2, RoundingMode.HALF_UP)
        BigDecimal balance = (foodBalance + rentBalance).setScale(2, RoundingMode.HALF_UP)

        [
                memberId           : member.id,
                memberName         : member.name,
                meals              : meals,
                mealRate           : mealRate,
                mealCost           : mealCost,
                expenseShare       : expenseShare,
                bazarContribution  : bazarContribution,
                expenseContribution: expenseContribution,
                foodBalance        : foodBalance,
                rent               : rent,
                deposit            : deposit,
                rentBalance        : rentBalance,
                balance            : balance
        ]
    }
}
