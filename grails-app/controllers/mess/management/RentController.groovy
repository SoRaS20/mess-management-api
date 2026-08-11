package mess.management

import grails.rest.RestfulController
import grails.gorm.transactions.Transactional

class RentController extends RestfulController<Rent> {

    static responseFormats = ['json']

    RentController() {
        super(Rent)
    }

    def byMonth() {
        respond Rent.findAllByMonth(Month.get(params.long('monthId')))
    }

    /**
     * Upsert: a rent is unique per (member, month), so if one already exists for
     * the payload's member+month, update its amount instead of failing with 422.
     */
    @Transactional
    @Override
    def save() {
        def json = request.JSON
        if (!json?.member?.id || !json?.month?.id) {
            render status: 422, text: '{"message":"Both member.id and month.id are required"}'
            return
        }

        Long memberId = json.member.id as Long
        Long monthId = json.month.id as Long
        BigDecimal amount = json.amount as BigDecimal

        Rent existing = Rent.where { member.id == memberId && month.id == monthId }.get()
        if (existing) {
            existing.amount = amount
            if (existing.validate()) {
                existing.save(flush: true)
                respond existing
            } else {
                respond existing.errors, [status: 422]
            }
            return
        }

        Rent rent = new Rent(
            member: Member.load(memberId),
            month: Month.load(monthId),
            amount: amount
        )
        if (rent.validate()) {
            rent.save(flush: true)
            respond rent, status: 201
        } else {
            respond rent.errors, [status: 422]
        }
    }
}
