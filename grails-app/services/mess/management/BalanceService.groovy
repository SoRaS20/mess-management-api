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

    /**
     * Balance for one member in one month:
     *   balance = deposit − (mealCount × mealRate) − expenseShare − rent
     * A positive balance means the member has credit; negative means they owe.
     */
    Map balanceFor(Long memberId, Long monthId) {
        Member member = Member.get(memberId)
        Map summary = dashboardService.summary(monthId)
        if (!member || !summary) return null
        buildBalance(member, monthId, summary)
    }

    /** Balances for every active member in the month, sharing one meal rate + expense split. */
    List<Map> balancesForMonth(Long monthId) {
        Map summary = dashboardService.summary(monthId)
        if (!summary) return []
        memberService.listActive().collect { Member member ->
            buildBalance(member, monthId, summary)
        }
    }

    private Map buildBalance(Member member, Long monthId, Map summary) {
        BigDecimal mealRate = summary.mealRate
        BigDecimal meals = mealService.mealCountFor(member.id, monthId)
        BigDecimal mealCost = (meals * mealRate).setScale(2, RoundingMode.HALF_UP)
        BigDecimal expenseShare = summary.expenseSharePerMember
        BigDecimal rent = rentService.rentFor(member.id, monthId)
        BigDecimal deposit = depositService.totalDepositFor(member.id, monthId)
        BigDecimal balance = (deposit - mealCost - expenseShare - rent).setScale(2, RoundingMode.HALF_UP)

        [
                memberId    : member.id,
                memberName  : member.name,
                meals       : meals,
                mealRate    : mealRate,
                mealCost    : mealCost,
                expenseShare: expenseShare,
                rent        : rent,
                deposit     : deposit,
                balance     : balance
        ]
    }
}
