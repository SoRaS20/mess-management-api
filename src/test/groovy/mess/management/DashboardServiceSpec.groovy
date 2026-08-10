package mess.management

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class DashboardServiceSpec extends Specification implements ServiceUnitTest<DashboardService>, DataTest {

    Class[] getDomainClassesToMock() {
        [User, Member, Month, Meal, Bazar, Expense, Deposit] as Class[]
    }

    def setup() {
        service.mealService = new MealService()
        service.bazarService = new BazarService()
        service.expenseService = new ExpenseService()
        service.depositService = new DepositService()
        service.memberService = Stub(MemberService)
    }

    void "mealRate is totalBazar divided by totalMeals"() {
        given:
        Month month = new Month(year: 2026, monthNo: 8).save(failOnError: true)
        Member m = new Member(name: 'Alice').save(failOnError: true)
        Member m2 = new Member(name: 'Bob').save(failOnError: true)
        service.memberService.activeCount() >> 4

        and: "bazar totals 3000"
        new Bazar(member: m, month: month, amount: 2000).save(failOnError: true)
        new Bazar(member: m, month: month, amount: 1000).save(failOnError: true)

        and: "meals total 120: two members x 24 full days (2.5 each)"
        24.times { i ->
            java.time.LocalDate d = java.time.LocalDate.of(2026, 8, 1).plusDays(i)
            new Meal(member: m, month: month, recordDate: d).save(failOnError: true)
            new Meal(member: m2, month: month, recordDate: d).save(failOnError: true)
        }

        when:
        Map summary = service.summary(month.id)

        then:
        summary.totalBazar == 3000.0G
        summary.totalMeals == 120.0G
        summary.mealRate == 25.00G
    }

    void "mealRate is zero when there are no meals (no divide-by-zero)"() {
        given:
        Month month = new Month(year: 2026, monthNo: 9).save(failOnError: true)
        Member m = new Member(name: 'Alice').save(failOnError: true)
        service.memberService.activeCount() >> 3
        new Bazar(member: m, month: month, amount: 500).save(failOnError: true)

        when:
        Map summary = service.summary(month.id)

        then:
        summary.totalMeals == 0.0G
        summary.mealRate == 0.0G
    }

    void "expenseSharePerMember is totalExpenses divided by active member count"() {
        given:
        Month month = new Month(year: 2026, monthNo: 10).save(failOnError: true)
        service.memberService.activeCount() >> 4
        new Expense(month: month, amount: 3000, category: 'gas').save(failOnError: true)
        new Expense(month: month, amount: 1000, category: 'water').save(failOnError: true)

        when:
        Map summary = service.summary(month.id)

        then:
        summary.totalExpenses == 4000.0G
        summary.expenseSharePerMember == 1000.00G
    }

    void "expenseSharePerMember is zero when there are no active members"() {
        given:
        Month month = new Month(year: 2026, monthNo: 11).save(failOnError: true)
        service.memberService.activeCount() >> 0

        when:
        Map summary = service.summary(month.id)

        then:
        summary.expenseSharePerMember == 0.0G
    }
}
