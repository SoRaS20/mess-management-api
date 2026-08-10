package mess.management

import grails.rest.RestfulController

class MonthController extends RestfulController<Month> {

    static responseFormats = ['json']

    MonthService monthService

    MonthController() {
        super(Month)
    }

    def close() {
        Month month = monthService.close(params.long('id'))
        if (!month) {
            render status: 404
            return
        }
        respond month
    }

    def reopen() {
        Month month = monthService.reopen(params.long('id'))
        if (!month) {
            render status: 404
            return
        }
        respond month
    }

    def setManager() {
        Month month = monthService.setManager(params.long('id'), params.long('memberId'))
        if (!month) {
            render status: 404
            return
        }
        respond month
    }
}
