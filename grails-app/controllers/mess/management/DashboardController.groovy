package mess.management

import grails.web.Controller

/**
 * Dashboard: aggregate figures for a single billing month (meal rate, totals, expense split).
 * Not domain-backed — no RestfulController; resolves views/dashboard/show.gson.
 */
@Controller
class DashboardController {

    static responseFormats = ['json']

    DashboardService dashboardService

    def show() {
        Long monthId = params.long('monthId')
        if (!monthId) {
            render status: 400
            return
        }
        Month month = Month.get(monthId)
        if (!month) {
            render status: 404
            return
        }
        respond dashboardService.summary(monthId)
    }
}
