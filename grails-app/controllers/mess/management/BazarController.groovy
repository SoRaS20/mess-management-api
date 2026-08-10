package mess.management

import grails.rest.RestfulController

class BazarController extends RestfulController<Bazar> {

    static responseFormats = ['json']

    BazarController() {
        super(Bazar)
    }

    def byMonth() {
        respond Bazar.findAllByMonth(Month.get(params.long('monthId')), [sort: 'bazarDate'])
    }
}
