package mess.management

import grails.gorm.transactions.Transactional

@Transactional
class MonthService {

    Month get(Long id) {
        Month.get(id)
    }

    List<Month> listAll() {
        Month.list(sort: 'year', order: 'desc').sort { -(it.year * 100 + it.monthNo) }
    }

    Month create(Integer year, Integer monthNo) {
        Month existing = Month.findByYearAndMonthNo(year, monthNo)
        if (existing) return existing
        Month month = new Month(year: year, monthNo: monthNo)
        month.save(failOnError: true)
        month
    }

    Month close(Long id) {
        Month month = Month.get(id)
        if (!month) return null
        month.closed = true
        month.save(failOnError: true)
        month
    }

    Month reopen(Long id) {
        Month month = Month.get(id)
        if (!month) return null
        month.closed = false
        month.save(failOnError: true)
        month
    }

    Month setManager(Long id, Long memberId) {
        Month month = Month.get(id)
        Member manager = Member.get(memberId)
        if (!month || !manager) return null
        month.manager = manager
        month.save(failOnError: true)
        month
    }
}
