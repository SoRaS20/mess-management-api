package mess.management

import grails.rest.RestfulController

class RentController extends RestfulController<Rent> {

    static responseFormats = ['json']

    RentController() {
        super(Rent)
    }

    def byMonth() {
        respond Rent.findAllByMonth(Month.get(params.long('monthId')))
    }
}
