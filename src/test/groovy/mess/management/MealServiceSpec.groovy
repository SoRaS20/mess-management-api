package mess.management

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import java.time.LocalDate

class MealServiceSpec extends Specification implements ServiceUnitTest<MealService>, DataTest {

    Class[] getDomainClassesToMock() {
        [User, Member, Month, Meal] as Class[]
    }

    private Month month
    private Member alice, bob

    def setup() {
        month = new Month(year: 2026, monthNo: 8).save(failOnError: true)
        alice = new Member(name: 'Alice', joinDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)
        bob = new Member(name: 'Bob', joinDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)
    }

    void "dailyCount reflects which meals are on"() {
        expect:
        new Meal(breakfastOn: b, lunchOn: l, dinnerOn: d).dailyCount == expected

        where:
        b     | l     | d     || expected
        true  | true  | true  || 2.5G
        false | true  | true  || 2.0G
        false | true  | false || 1.0G
        false | false | false || 0.0G
        true  | false | false || 0.5G
    }

    void "generateDefaultMeals creates one all-ON row per active member per day and is idempotent"() {
        given: "August has 31 days and 2 active members"
        int daysInAugust = 31

        when:
        int created = service.generateDefaultMeals(month.id)

        then:
        created == daysInAugust * 2
        Meal.count() == daysInAugust * 2
        Meal.list().every { it.breakfastOn && it.lunchOn && it.dinnerOn }

        when: "run again"
        int createdAgain = service.generateDefaultMeals(month.id)

        then: "no duplicates"
        createdAgain == 0
        Meal.count() == daysInAugust * 2
    }

    void "toggle flips the requested slot and persists"() {
        given:
        Meal meal = new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 3)).save(failOnError: true)

        when:
        service.toggle(meal.id, 'dinner', false)

        then:
        Meal.get(meal.id).dinnerOn == false
        Meal.get(meal.id).breakfastOn == true
        Meal.get(meal.id).dailyCount == 1.5G
    }

    void "toggle with an unknown slot throws"() {
        given:
        Meal meal = new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 4)).save(failOnError: true)

        when:
        service.toggle(meal.id, 'brunch', false)

        then:
        thrown(IllegalArgumentException)
    }

    void "totalMeals and mealCountFor sum dailyCount across rows after OFF toggles"() {
        given: "Alice: 2 full days (2.5 each) + 1 day with dinner off (1.5) = 6.5"
        new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)
        new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 2)).save(failOnError: true)
        new Meal(member: alice, month: month, recordDate: LocalDate.of(2026, 8, 3), dinnerOn: false).save(failOnError: true)
        and: "Bob: 1 full day = 2.5"
        new Meal(member: bob, month: month, recordDate: LocalDate.of(2026, 8, 1)).save(failOnError: true)

        expect:
        service.mealCountFor(alice.id, month.id) == 6.5G
        service.mealCountFor(bob.id, month.id) == 2.5G
        service.totalMeals(month.id) == 9.0G
    }
}
