package mess.management

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import java.time.LocalDate

class BalanceServiceSpec extends Specification implements ServiceUnitTest<BalanceService>, DataTest {

    Class[] getDomainClassesToMock() {
        [User, Member, Month, Meal, Bazar, Expense, Deposit, Rent] as Class[]
    }

    private Month month
    private Member alice

    def setup() {
        month = new Month(year: 2026, monthNo: 8).save(failOnError: true)
        alice = new Member(name: 'Alice', joinDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)

        service.mealService = new MealService()
        service.rentService = new RentService()
        service.depositService = new DepositService()
        service.memberService = Stub(MemberService) {
            listActive() >> [alice]
        }
        service.dashboardService = Stub(DashboardService)
    }

    void "balance is deposit minus mealCost minus expenseShare minus rent"() {
        given: "rate 25, 40 meal-units, expense share 500, rent 1500, deposit 3000 => balance 0"
        service.dashboardService.summary(month.id) >> [
                mealRate: 25.00G, expenseSharePerMember: 500.00G
        ]
        // 40 meal-units for Alice = 16 full days (2.5 each)
        16.times { i ->
            new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 1).plusDays(i)).save(failOnError: true)
        }
        new Rent(member: alice, month: month, amount: 1500).save(failOnError: true)
        new Deposit(member: alice, month: month, amount: 3000, depositDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)

        when:
        Map b = service.balanceFor(alice.id, month.id)

        then:
        b.meals == 40.0G
        b.mealCost == 1000.00G
        b.expenseShare == 500.00G
        b.rent == 1500.0G
        b.deposit == 3000.0G
        b.balance == 0.00G
    }

    void "balance is negative when deposits are less than charges"() {
        given:
        service.dashboardService.summary(month.id) >> [
                mealRate: 25.00G, expenseSharePerMember: 500.00G
        ]
        16.times { i ->
            new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 1).plusDays(i)).save(failOnError: true)
        }
        new Rent(member: alice, month: month, amount: 1500).save(failOnError: true)
        new Deposit(member: alice, month: month, amount: 1000, depositDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)

        when:
        Map b = service.balanceFor(alice.id, month.id)

        then: "1000 - 1000 - 500 - 1500 = -2000"
        b.balance == -2000.00G
    }

    void "balancesForMonth returns a row per active member without throwing when counts are zero"() {
        given:
        service.dashboardService.summary(month.id) >> [
                mealRate: 0.0G, expenseSharePerMember: 0.0G
        ]

        when:
        List<Map> rows = service.balancesForMonth(month.id)

        then:
        rows.size() == 1
        rows[0].memberName == 'Alice'
        rows[0].balance == 0.00G
    }
}
