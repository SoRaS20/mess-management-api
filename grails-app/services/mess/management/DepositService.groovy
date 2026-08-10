package mess.management

import grails.gorm.transactions.Transactional

@Transactional
class DepositService {

    Deposit get(Long id) {
        Deposit.get(id)
    }

    List<Deposit> byMonth(Long monthId) {
        Month month = Month.get(monthId)
        month ? Deposit.findAllByMonth(month, [sort: 'depositDate']) : []
    }

    Deposit create(Map args) {
        Deposit deposit = new Deposit(args)
        deposit.save(failOnError: true)
        deposit
    }

    Deposit update(Long id, Map args) {
        Deposit deposit = Deposit.get(id)
        if (!deposit) return null
        deposit.properties = args
        deposit.save(failOnError: true)
        deposit
    }

    boolean delete(Long id) {
        Deposit deposit = Deposit.get(id)
        if (!deposit) return false
        deposit.delete(flush: true)
        true
    }

    @Transactional(readOnly = true)
    BigDecimal totalDeposits(Long monthId) {
        byMonth(monthId).inject(0.0G) { BigDecimal sum, Deposit d -> sum + (d.amount ?: 0.0G) }
    }

    @Transactional(readOnly = true)
    BigDecimal totalDepositFor(Long memberId, Long monthId) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return 0.0G
        Deposit.where { member == theMember && month == theMonth }.list()
                .inject(0.0G) { BigDecimal sum, Deposit d -> sum + (d.amount ?: 0.0G) }
    }
}
