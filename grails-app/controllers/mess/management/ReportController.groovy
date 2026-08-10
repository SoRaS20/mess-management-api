package mess.management

import grails.web.Controller
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Report endpoints: monthly summary, daily meal report, per-member report.
 * Not domain-backed — resolves views/report/{action}.gson.
 */
@Controller
class ReportController {

    static responseFormats = ['json']

    ReportService reportService

    def monthly() {
        Long monthId = params.long('monthId')
        if (!monthId) {
            render status: 400
            return
        }
        Map report = reportService.monthlyReport(monthId)
        if (!report) {
            render status: 404
            return
        }
        respond report
    }

    def daily() {
        Long monthId = params.long('monthId')
        LocalDate date = null
        try {
            date = LocalDate.parse(params.date)
        } catch (DateTimeParseException | NullPointerException ignored) {
            render status: 400, text: 'Invalid or missing date (expected yyyy-MM-dd)'
            return
        }
        Map report = reportService.dailyReport(monthId, date)
        if (!report) {
            render status: 404
            return
        }
        respond report
    }

    def member() {
        Long memberId = params.long('memberId')
        Long monthId = params.long('monthId')
        if (!memberId || !monthId) {
            render status: 400
            return
        }
        Map report = reportService.memberReport(memberId, monthId)
        if (!report) {
            render status: 404
            return
        }
        respond report
    }
}
