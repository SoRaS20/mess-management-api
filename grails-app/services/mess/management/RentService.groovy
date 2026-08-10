package mess.management

import grails.gorm.transactions.Transactional

@Transactional
class RentService {

    List<Rent> byMonth(Long monthId) {
        Month month = Month.get(monthId)
        month ? Rent.findAllByMonth(month) : []
    }

    /** Upsert the rent for a (member, month) pair. */
    Rent setRent(Long memberId, Long monthId, BigDecimal amount) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return null

        Rent rent = Rent.where { member == theMember && month == theMonth }.get() ?:
                new Rent(member: theMember, month: theMonth)
        rent.amount = amount
        rent.save(failOnError: true)
        rent
    }

    @Transactional(readOnly = true)
    BigDecimal rentFor(Long memberId, Long monthId) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return 0.0G
        Rent rent = Rent.where { member == theMember && month == theMonth }.get()
        rent?.amount ?: 0.0G
    }
}
