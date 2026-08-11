package mess.management

import grails.gorm.transactions.Transactional

@Transactional
class BazarService {

    Bazar get(Long id) {
        Bazar.get(id)
    }

    List<Bazar> byMonth(Long monthId) {
        Month month = Month.get(monthId)
        month ? Bazar.findAllByMonth(month, [sort: 'bazarDate']) : []
    }

    Bazar create(Map args) {
        Bazar bazar = new Bazar(args)
        bazar.save(failOnError: true)
        bazar
    }

    Bazar update(Long id, Map args) {
        Bazar bazar = Bazar.get(id)
        if (!bazar) return null
        bazar.properties = args
        bazar.save(failOnError: true)
        bazar
    }

    boolean delete(Long id) {
        Bazar bazar = Bazar.get(id)
        if (!bazar) return false
        bazar.delete(flush: true)
        true
    }

    @Transactional(readOnly = true)
    BigDecimal totalBazar(Long monthId) {
        byMonth(monthId).inject(0.0G) { BigDecimal sum, Bazar b -> sum + (b.amount ?: 0.0G) }
    }

    @Transactional(readOnly = true)
    BigDecimal totalBazarFor(Long memberId, Long monthId) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return 0.0G
        Bazar.where { member == theMember && month == theMonth }.list()
                .inject(0.0G) { BigDecimal sum, Bazar b -> sum + (b.amount ?: 0.0G) }
    }
}
