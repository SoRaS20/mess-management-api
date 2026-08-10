package mess.management

import grails.rest.RestfulController

class DepositController extends RestfulController<Deposit> {

    static responseFormats = ['json']

    DepositController() {
        super(Deposit)
    }

    def byMonth() {
        respond Deposit.findAllByMonth(Month.get(params.long('monthId')), [sort: 'depositDate'])
    }
}
