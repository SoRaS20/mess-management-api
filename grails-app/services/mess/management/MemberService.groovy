package mess.management

import grails.gorm.transactions.Transactional
import java.time.LocalDate

@Transactional
class MemberService {

    MealService mealService

    Member get(Long id) {
        Member.get(id)
    }

    List<Member> listAll(Map pagination = [:]) {
        Member.list(pagination)
    }

    List<Member> listActive() {
        Member.findAllByActive(true, [sort: 'name'])
    }

    int activeCount() {
        Member.countByActive(true)
    }

    Member create(Map args) {
        Member member = new Member(args)
        member.save(failOnError: true)

        // If there is an open current month, seed default-ON meals for this member
        // from their join date forward so they are only charged from when they joined.
        Month current = Month.findByYearAndMonthNoAndClosed(
                member.joinDate.year, member.joinDate.monthValue, false)
        if (current) {
            mealService.ensureMealsFromDate(current.id, member.id, member.joinDate)
        }
        member
    }

    Member update(Long id, Map args) {
        Member member = Member.get(id)
        if (!member) return null
        member.properties = args
        member.save(failOnError: true)
        member
    }

    boolean delete(Long id) {
        Member member = Member.get(id)
        if (!member) return false
        member.delete(flush: true)
        true
    }

    Member toggleActive(Long id) {
        Member member = Member.get(id)
        if (!member) return null
        member.active = !member.active
        member.save(failOnError: true)
        member
    }
}
